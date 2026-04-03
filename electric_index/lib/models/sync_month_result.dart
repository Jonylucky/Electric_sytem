class SyncMonthResultRow {
  final int i;
  final String? meterId;
  final String? month;
  final bool success;
  final String? status; // CREATED/UPDATED
  final int? readingId;
  final String? error;

  SyncMonthResultRow({
    required this.i,
    required this.success,
    this.meterId,
    this.month,
    this.status,
    this.readingId,
    this.error,
  });

  factory SyncMonthResultRow.fromJson(Map<String, dynamic> j) {
    return SyncMonthResultRow(
      i: (j['i'] as num?)?.toInt() ?? 0,
      meterId: j['meterId']?.toString(),
      month: j['month']?.toString(),
      success: (j['success'] as bool?) ?? false,
      status: j['status']?.toString(),
      readingId: (j['readingId'] as num?)?.toInt(),
      error: j['error']?.toString(),
    );
  }
}

class SyncMonthResult {
  final bool success;
  final int count;
  final List<SyncMonthResultRow> results;

  SyncMonthResult({
    required this.success,
    required this.count,
    required this.results,
  });

  factory SyncMonthResult.fromJson(Map<String, dynamic> j) {
    final raw = (j['results'] as List?) ?? [];
    return SyncMonthResult(
      success: (j['success'] as bool?) ?? false,
      count: (j['count'] as num?)?.toInt() ?? 0,
      results: raw
          .map((e) => SyncMonthResultRow.fromJson(Map<String, dynamic>.from(e)))
          .toList(),
    );
  }
}

class SyncResultItem {
  final bool success;
  final String? meterId;
  final String? message;

  SyncResultItem({required this.success, this.meterId, this.message});

  factory SyncResultItem.fromJson(Map<String, dynamic> json) {
    return SyncResultItem(
      success: json['success'] == true,
      meterId: json['meterId'],
      message: json['message'],
    );
  }
}
