import 'dart:typed_data';
import 'dart:ui' as ui;
import 'package:qr_flutter/qr_flutter.dart';

/// Chuỗi QR ngắn gọn để scan nhanh
String buildQrData({required String companyId, required String meterId}) {
  return 'CID=$companyId;MID=$meterId';
}

/// Tạo ảnh QR dạng PNG bytes (để nhét vào Excel)
Future<Uint8List> qrPngBytes(String data, {double size = 220}) async {
  final painter = QrPainter(
    data: data,
    version: QrVersions.auto,
    errorCorrectionLevel: QrErrorCorrectLevel.M,
    gapless: true,
  );

  final ui.Image image = await painter.toImage(size);
  final byteData = await image.toByteData(format: ui.ImageByteFormat.png);
  return byteData!.buffer.asUint8List();
}
