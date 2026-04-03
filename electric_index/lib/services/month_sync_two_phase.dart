import 'dart:io';
import 'package:path_provider/path_provider.dart';

import 'readings_api.dart';
import '../models/sync_payload.dart';
import '../models/sync_month_result.dart';
import '../utils/image_key.dart';
import '../utils/split_by_size.dart';
import 'image_compress_service.dart';
import 'zip_service.dart';

class MonthSyncTwoPhase {
  final ReadingsApi api;
  final ImageCompressService compress;
  final ZipService zip;

  MonthSyncTwoPhase({
    required this.api,
    required this.compress,
    required this.zip,
  });

  Future<SyncMonthResult> sync({
    required String month,
    required List<SyncPayload> items,
    maxZipBytes = 5 * 1024 * 1024,
    int compressQuality = 80,
    int compressMinSide = 1600,
  }) async {
    final r1 = await api.syncMonthData(items);
    if (!r1.success) return r1;

    final okKeys = <String>{};
    for (final row in r1.results) {
      if (row.success == true && row.meterId != null && row.month != null) {
        okKeys.add(buildImageKey(row.meterId!, row.month!));
      }
    }

    final compressedFiles = <File>[];
    final entryNames = <String>[];

    for (final it in items) {
      final p = it.imagePath;
      if (p == null || p.isEmpty) continue;

      final f = File(p);
      if (!await f.exists()) continue;

      final key = buildImageKey(it.meterId, it.month);
      if (!okKeys.contains(key)) continue;

      final cf = await compress.compressToJpg(
        f,
        quality: compressQuality,
        minSide: compressMinSide,
      );

      if (!await cf.exists()) {
        throw Exception('File nén không tồn tại: ${cf.path}');
      }

      final cfSize = await cf.length();
      if (cfSize <= 0) {
        throw Exception('File nén rỗng: ${cf.path}');
      }

      compressedFiles.add(cf);
      entryNames.add('$key.jpg');
    }

    if (compressedFiles.isEmpty) return r1;

    final nameByPath = <String, String>{};
    for (int i = 0; i < compressedFiles.length; i++) {
      nameByPath[compressedFiles[i].path] = entryNames[i];
    }

    final batches = splitFilesByTotalBytes(compressedFiles, maxZipBytes);
    final tmpDir = await getTemporaryDirectory();

    for (int b = 0; b < batches.length; b++) {
      final files = batches[b];
      final names = files
          .map((f) => nameByPath[f.path] ?? f.uri.pathSegments.last)
          .toList();

      final zipPath = '${tmpDir.path}/images_part_${b + 1}.zip';

      final zipFile = await zip.createZip(
        files: files,
        entryNames: names,
        outPath: zipPath,
      );

      // ✅ check zip file thật sự tồn tại
      if (!await zipFile.exists()) {
        throw Exception('Zip file không tồn tại: ${zipFile.path}');
      }

      final zipSize = await zipFile.length();
      if (zipSize <= 0) {
        throw Exception('Zip file rỗng: ${zipFile.path}');
      }

      print('ZIP[$b] path=${zipFile.path} size=$zipSize files=${files.length}');

      final r2 = await api.uploadImagesZip(zipFile: zipFile, batch: b + 1);

      final ok =
          r2.success &&
          r2.results.isNotEmpty &&
          r2.results.every((x) => x.success == true);

      // ✅ xóa zip temp sau khi upload xong
      try {
        if (await zipFile.exists()) {
          await zipFile.delete();
        }
      } catch (_) {}

      if (!ok) {
        return SyncMonthResult(
          success: false,
          count: r2.count,
          results: r2.results,
        );
      }
    }

    // ✅ dọn file nén temp
    for (final f in compressedFiles) {
      try {
        if (await f.exists()) {
          await f.delete();
        }
      } catch (_) {}
    }

    return r1;
  }
}
