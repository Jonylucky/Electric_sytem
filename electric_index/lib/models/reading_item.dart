class ReadingItem {
  final int readingId;
  final String meterId;
  final String month;
  final int indexPrevMonth;
  final int indexLastMonth;
  final int indexConsumption;
  final String? imageUrl;
  final DateTime? capturedAt;
  final String readingType;

  ReadingItem({
    required this.readingId,
    required this.meterId,
    required this.month,
    required this.indexPrevMonth,
    required this.indexLastMonth,
    required this.indexConsumption,
    this.imageUrl,
    this.capturedAt,
    required this.readingType,
  });

  factory ReadingItem.fromJson(Map<String, dynamic> j) {
    DateTime? _dt(dynamic v) =>
        v == null ? null : DateTime.tryParse(v.toString());

    return ReadingItem(
      readingId: (j['readingId'] as num?)?.toInt() ?? 0,
      meterId: (j['meterId'] ?? '').toString(),
      month: (j['month'] ?? '').toString(),
      indexPrevMonth: (j['indexPrevMonth'] as num?)?.toInt() ?? 0,
      indexLastMonth: (j['indexLastMonth'] as num?)?.toInt() ?? 0,
      indexConsumption: (j['indexConsumption'] as num?)?.toInt() ?? 0,
      imageUrl: j['imageUrl']?.toString(),
      capturedAt: _dt(j['capturedAt']),
      readingType: (j['readingType'] ?? 'OFFICIAL').toString(),
    );
  }
}
