String previousMonth(String month) {
  final parts = month.split('-');
  final year = int.parse(parts[0]);
  final m = int.parse(parts[1]);

  final dt = DateTime(year, m - 1, 1);
  final mm = dt.month.toString().padLeft(2, '0');
  return '${dt.year}-$mm';
}
