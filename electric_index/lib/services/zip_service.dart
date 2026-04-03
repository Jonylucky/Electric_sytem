import 'dart:io';
import 'package:archive/archive_io.dart';
import 'package:path/path.dart' as p;

class ZipService {
  /// Zip folder bằng cách thêm từng file một (giảm RAM khi nhiều ảnh).
  Future<File> zipExportFolder({
    required Directory folder,
    String? zipName,
  }) async {
    final parent = Directory(p.dirname(folder.path));
    final name = zipName ?? '${p.basename(folder.path)}.zip';
    final zipPath = p.join(parent.path, name);

    final encoder = ZipFileEncoder();
    encoder.create(zipPath);

    final basePath = folder.path;
    await for (final entity in folder.list(
      recursive: true,
      followLinks: false,
    )) {
      if (entity is File) {
        final rel = p.relative(entity.path, from: basePath);
        await encoder.addFile(entity, rel);
        await Future.delayed(Duration.zero);
      }
    }

    encoder.close();
    return File(zipPath);
  }

  Future<File> createZip({
    required List<File> files,
    required List<String> entryNames,
    required String outPath,
  }) async {
    final archive = Archive();

    for (int i = 0; i < files.length; i++) {
      final bytes = await files[i].readAsBytes();
      archive.addFile(ArchiveFile(entryNames[i], bytes.length, bytes));
    }

    final zipData = ZipEncoder().encode(archive);
    if (zipData == null) throw Exception('ZipEncoder returned null');

    final zipFile = File(outPath);
    await zipFile.writeAsBytes(zipData, flush: true);
    return zipFile;
  }
}
