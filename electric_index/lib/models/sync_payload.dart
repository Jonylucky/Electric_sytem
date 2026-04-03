class SyncPayload {
  final String meterId;
  final String month; // YYYY-MM
  final int indexLastMonth;
  final int indexPrevMonth;
  final int indexConsumption;
  final DateTime? capturedAt;

  // local image path (KHÔNG gửi lên server)
  final String? imagePath;

  SyncPayload({
    required this.meterId,
    required this.month,
    required this.indexLastMonth,
    required this.indexPrevMonth,
    required this.indexConsumption,
    this.capturedAt,
    this.imagePath,
  });

  Map<String, dynamic> toJson() {
    return {
      'meterId': meterId,
      'month': month,
      'indexLastMonth': indexLastMonth,
      'indexPrevMonth': indexPrevMonth,
      'indexConsumption': indexConsumption,
      if (capturedAt != null) 'capturedAt': capturedAt!.toIso8601String(),
    };
  }
}
