import 'dart:io';
import 'package:flutter_image_compress/flutter_image_compress.dart';
import 'package:path_provider/path_provider.dart';

class ImageCompressService {
  /// Compress to JPG ~200-800KB (tuỳ ảnh).
  Future<File> compressToJpg(
    File input, {
    int quality = 80,
    int minSide = 1600,
  }) async {
    final dir = await getTemporaryDirectory();
    final out = File(
      '${dir.path}/cmp_${DateTime.now().millisecondsSinceEpoch}.jpg',
    );

    final bytes = await FlutterImageCompress.compressWithFile(
      input.path,
      minWidth: minSide,
      minHeight: minSide,
      quality: quality,
      format: CompressFormat.jpeg,
    );

    if (bytes == null) return input;
    await out.writeAsBytes(bytes, flush: true);
    return out;
  }
}
