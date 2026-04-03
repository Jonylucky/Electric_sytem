class ReadingPullItem {
  final String meterId;
  final String month;
  final int indexPrevMonth;
  final int indexLastMonth;
  final int indexConsumption;
  final String? createdAt;

  ReadingPullItem({
    required this.meterId,
    required this.month,
    required this.indexPrevMonth,
    required this.indexLastMonth,
    required this.indexConsumption,
    this.createdAt,
  });

  factory ReadingPullItem.fromJson(Map<String, dynamic> j) {
    return ReadingPullItem(
      meterId: j['meterId']?.toString() ?? '',
      month: j['month']?.toString() ?? '',
      indexPrevMonth: (j['indexPrevMonth'] as num?)?.toInt() ?? 0,
      indexLastMonth: (j['indexLastMonth'] as num?)?.toInt() ?? 0,
      indexConsumption: (j['indexConsumption'] as num?)?.toInt() ?? 0,
      createdAt: j['createdAt']?.toString(),
    );
  }
}
