String buildImageKey(String meterId, String month) {
  final raw = '${meterId}_$month';
  return raw.replaceAll(RegExp(r'[^a-zA-Z0-9_-]'), '_');
}
