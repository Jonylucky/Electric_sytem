import 'dart:io';

/// Split list by total bytes (rough: based on file length).
List<List<File>> splitFilesByTotalBytes(List<File> files, int maxBytes) {
  final out = <List<File>>[];
  var cur = <File>[];
  var curBytes = 0;

  for (final f in files) {
    final sz = f.lengthSync();
    if (cur.isNotEmpty && curBytes + sz > maxBytes) {
      out.add(cur);
      cur = <File>[];
      curBytes = 0;
    }
    cur.add(f);
    curBytes += sz;
  }

  if (cur.isNotEmpty) out.add(cur);
  return out;
}
