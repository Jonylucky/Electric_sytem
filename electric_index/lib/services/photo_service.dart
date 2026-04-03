import 'dart:io';
import 'dart:typed_data';
import 'dart:ui' as ui;

import 'package:flutter/services.dart';
import 'package:intl/intl.dart';
import 'package:path_provider/path_provider.dart';
import 'package:image/image.dart' as img;
import 'package:geolocator/geolocator.dart';
import 'package:geocoding/geocoding.dart';
import 'package:flutter/foundation.dart'; // for compute

class PhotoService {
  /// Format time kiểu: 09:48 • 11/02/2026
  String formatDateTime(DateTime dt) {
    return DateFormat('HH:mm • dd/MM/yyyy').format(dt);
  }

  /// Public API:
  /// - Nhận ảnh gốc (File)
  /// - Tự lấy GPS + địa chỉ (nếu được)
  /// - Vẽ watermark xuống ảnh và trả về file mới (PNG)
  ///
  /// addressOverride: nếu mày muốn truyền địa chỉ có sẵn (khỏi gọi GPS)

  Future<File> addWatermark({
    required File originalImage,
    String? addressOverride,
    DateTime? timeOverride,
    bool includeLatLng = true,

    // ✅ giới hạn chiều rộng để nhẹ + nhanh
    int maxWidth = 960,

    // ✅ output options
    int jpgQuality = 85, // 75..90
    String prefix = 'wm',
  }) async {
    ui.Image? uiImage;
    ui.Image? outImage;

    try {
      final bytes = await originalImage.readAsBytes();

      // ✅ decode + resize ngay lúc decode (nhanh + ít RAM hơn)
      uiImage = await _decodeUiImage(bytes, targetWidth: maxWidth);

      final dt = timeOverride ?? DateTime.now();
      final timeText = formatDateTime(dt);

      final location = (addressOverride != null)
          ? _LocationInfo(address: addressOverride, latLng: '')
          : await _getLocationInfoSafe(includeLatLng: includeLatLng);

      final watermarkText = _buildWatermarkText(
        timeText: timeText,
        latLng: location.latLng,
        address: location.address,
        includeLatLng: includeLatLng,
      );

      outImage = await _drawWatermarkOnImage(
        uiImage: uiImage,
        text: watermarkText,
      );

      // ✅ save
      final outFile = await _saveUiImageToFile(
        outImage,
        jpgQuality: jpgQuality,
        prefix: prefix,
      );

      return outFile;
    } finally {
      // ✅ dù throw error vẫn dọn RAM
      uiImage?.dispose();
      outImage?.dispose();
    }
  }
  // =========================
  // LOCATION
  // =========================

  Future<_LocationInfo> _getLocationInfoSafe({
    required bool includeLatLng,
  }) async {
    try {
      final serviceEnabled = await Geolocator.isLocationServiceEnabled();
      if (!serviceEnabled) return const _LocationInfo(address: '', latLng: '');

      var perm = await Geolocator.checkPermission();
      if (perm == LocationPermission.denied) {
        perm = await Geolocator.requestPermission();
      }
      if (perm == LocationPermission.denied ||
          perm == LocationPermission.deniedForever) {
        return const _LocationInfo(address: '', latLng: '');
      }

      final pos = await Geolocator.getCurrentPosition(
        desiredAccuracy: LocationAccuracy.high,
      );

      final latLng = includeLatLng
          ? '${pos.latitude.toStringAsFixed(6)}, ${pos.longitude.toStringAsFixed(6)}'
          : '';

      String address = '';
      try {
        final places = await placemarkFromCoordinates(
          pos.latitude,
          pos.longitude,
        );
        if (places.isNotEmpty) {
          final p = places.first;
          final parts = <String>[
            if ((p.street ?? '').trim().isNotEmpty) p.street!.trim(),
            if ((p.subAdministrativeArea ?? '').trim().isNotEmpty)
              p.subAdministrativeArea!.trim(),
            if ((p.administrativeArea ?? '').trim().isNotEmpty)
              p.administrativeArea!.trim(),
          ];
          address = parts.join(', ');
        }
      } catch (_) {
        // reverse geocode fail -> vẫn ok, chỉ dùng latLng
      }

      return _LocationInfo(address: address, latLng: latLng);
    } catch (_) {
      return const _LocationInfo(address: '', latLng: '');
    }
  }

  String _buildWatermarkText({
    required String timeText,
    required String latLng,
    required String address,
    required bool includeLatLng,
  }) {
    final buf = StringBuffer()..writeln(timeText);

    if (address.trim().isNotEmpty) {
      buf.writeln(address.trim());
    }

    return buf.toString().trim();
  }

  // =========================
  // UI IMAGE + CANVAS
  // =========================

  Future<ui.Image> _decodeUiImage(Uint8List bytes, {int? targetWidth}) async {
    final codec = await ui.instantiateImageCodec(
      bytes,
      targetWidth: targetWidth,
    );
    final frame = await codec.getNextFrame();
    return frame.image;
  }

  // edit
  Future<ui.Image> _drawWatermarkOnImage({
    required ui.Image uiImage,
    required String text,
  }) async {
    final w = uiImage.width.toDouble();
    final h = uiImage.height.toDouble();

    final recorder = ui.PictureRecorder();
    final canvas = ui.Canvas(recorder);

    // 1) draw original
    canvas.drawImage(uiImage, ui.Offset.zero, ui.Paint());

    // 2) box watermark (1/3 ảnh)
    final boxH = h / 3;
    final rectTop = h - boxH;
    final padding = w * 0.05;

    // 3) nền đen
    canvas.drawRect(
      ui.Rect.fromLTWH(0, rectTop, w, boxH),
      ui.Paint()..color = const ui.Color(0xAA000000),
    );

    // 4) sanitize + limit text (giảm cost layout)
    final safeText = _sanitizeWatermarkText(
      text,
      maxChars: 220, // tuỳ chỉnh: 180..260
      maxLines: 6, // tuỳ chỉnh: 4..7
    );

    // 5) 1 paragraph duy nhất
    final baseSize = boxH * 0.13; // scale theo box
    final pb =
        ui.ParagraphBuilder(
            ui.ParagraphStyle(
              textDirection: ui.TextDirection.ltr,
              fontSize: baseSize,
              height: 1.12,
              maxLines: 6,
              ellipsis: '…',
            ),
          )
          ..pushStyle(
            ui.TextStyle(
              color: const ui.Color(0xFFFFFFFF),
              fontSize: baseSize,
              fontWeight: ui.FontWeight.w600,
            ),
          )
          ..addText(safeText);

    final p = pb.build();
    p.layout(ui.ParagraphConstraints(width: w - padding * 2));

    canvas.drawParagraph(p, ui.Offset(padding, rectTop + padding));

    final picture = recorder.endRecording();
    return picture.toImage(uiImage.width, uiImage.height);
  }

  /// ✅ cắt bớt chữ + giới hạn dòng để layout nhanh hơn
  String _sanitizeWatermarkText(
    String input, {
    required int maxChars,
    required int maxLines,
  }) {
    // normalize line breaks + trim
    var s = input.replaceAll('\r\n', '\n').replaceAll('\r', '\n').trim();

    // bỏ dòng trống liên tiếp
    s = s.replaceAll(RegExp(r'\n{3,}'), '\n\n');

    // limit lines
    final lines = s.split('\n').where((e) => e.trim().isNotEmpty).toList();
    if (lines.length > maxLines) {
      s = lines.take(maxLines).join('\n');
    } else {
      s = lines.join('\n');
    }

    // limit chars
    if (s.length > maxChars) {
      s = '${s.substring(0, maxChars)}…';
    }

    return s;
  }

  Future<File> _saveUiImageToFile(
    ui.Image image, {
    int jpgQuality = 85, // 75..90 khuyên dùng
    String prefix = 'wm',
  }) async {
    // ✅ Clamp quality tránh truyền giá trị sai
    final int quality = jpgQuality.clamp(0, 100).toInt();

    // 1️⃣ Lấy raw RGBA pixels
    final byteData = await image.toByteData(format: ui.ImageByteFormat.rawRgba);

    if (byteData == null) {
      throw Exception('toByteData(rawRgba) returned null');
    }

    // 2️⃣ Tạo image object cho encoder (dùng ByteBuffer)
    final im = img.Image.fromBytes(
      width: image.width,
      height: image.height,
      bytes: byteData.buffer,
      order: img.ChannelOrder.rgba,
    );

    // 3️⃣ Encode JPG (nhẹ hơn PNG nhiều)
    final jpgBytes = Uint8List.fromList(img.encodeJpg(im, quality: quality));

    // 4️⃣ Save file
    final dir = await getTemporaryDirectory();
    final filePath =
        '${dir.path}/${prefix}_${DateTime.now().millisecondsSinceEpoch}.jpg';

    final file = File(filePath);
    await file.writeAsBytes(jpgBytes, flush: true);

    return file;
  }
}

class _LocationInfo {
  final String address;
  final String latLng;
  const _LocationInfo({required this.address, required this.latLng});
}

// --- helper types for isolate version ------------------------------------------------
class _WatermarkParams {
  final Uint8List bytes;
  final int maxWidth;
  final String timeText;
  final int jpgQuality;

  _WatermarkParams({
    required this.bytes,
    required this.maxWidth,
    required this.timeText,
    required this.jpgQuality,
  });
}

/// chạy trong isolate để tránh dùng heap chính
Uint8List _watermarkIsolate(_WatermarkParams p) {
  img.Image? im = img.decodeImage(p.bytes);
  if (im == null) throw StateError('decode failed');

  if (im.width > p.maxWidth) {
    im = img.copyResize(im, width: p.maxWidth);
  }

  // vẽ watermark đơn giản bằng thư viện `image`
  const int margin = 10;
  final color = img.ColorRgba8(255, 255, 255, 255);
  img.drawString(
    im,
    p.timeText,
    font: img.arial14,
    x: margin,
    y: im.height - 40,
    color: color,
  );

  return Uint8List.fromList(img.encodeJpg(im, quality: p.jpgQuality));
}
