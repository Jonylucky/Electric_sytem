import 'reading_pull_item.dart';

class PullMonthResponse {
  final bool success;
  final String month;
  final int count;
  final List<ReadingPullItem> items;

  PullMonthResponse({
    required this.success,
    required this.month,
    required this.count,
    required this.items,
  });

  factory PullMonthResponse.fromJson(Map<String, dynamic> j) {
    final rawItems = (j['items'] as List<dynamic>? ?? []);
    return PullMonthResponse(
      success: j['success'] == true,
      month: j['month']?.toString() ?? '',
      count: (j['count'] as num?)?.toInt() ?? 0,
      items: rawItems
          .map((e) => ReadingPullItem.fromJson(e as Map<String, dynamic>))
          .toList(),
    );
  }
}
