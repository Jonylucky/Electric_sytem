import 'package:sqflite/sqflite.dart';
import 'package:electric_index/db/dao/database_helper.dart';

class MeterResetReadingService {
  Future<void> upsertOfficialReadingForMonth({
    required DatabaseExecutor db,
    required String meterId,
    required String month, // YYYY-MM
    required int indexLastMonth, // physical index user nhập
    String? imageReading,
    DateTime? createdAt,
    String readingType = 'official',
  }) async {
    final now = (createdAt ?? DateTime.now()).toIso8601String();

    // 1) Lấy reading official của tháng trước
    final prevLast = await getStoredLastByMonth(
      meterId: meterId,
      month: previousMonth(month),
      db: db,
      readingType: readingType,
    );

    // 2) Lấy offset reset áp dụng cho tháng hiện tại
    final offset = await getOffsetBeforeOrAtMonth(
      meterId: meterId,
      month: month,
      db: db,
    );

    // 3) Chuẩn hóa last hiện tại:
    // - meter thường: currentLast = physical
    // - meter reset:  currentLast = physical + offset
    final currentLast = indexLastMonth + offset;

    // 4) Xác định baseline nếu tháng trước chưa có reading
    final baseline = await getBaselineForMonth(
      db: db,
      meterId: meterId,
      month: month,
    );

    // 5) prev ưu tiên theo reading tháng trước, nếu không có thì dùng baseline
    final currentPrev = prevLast ?? baseline;

    final consumption = currentLast - currentPrev;

    if (consumption < 0) {
      throw Exception(
        'Consumption âm bất thường: meter=$meterId, month=$month, '
        'currentLast=$currentLast, currentPrev=$currentPrev',
      );
    }

    final existed = await db.rawQuery(
      '''
      SELECT reading_id
      FROM readings
      WHERE meter_id = ?
        AND month = ?
        AND reading_type = ?
      LIMIT 1
      ''',
      [meterId, month, readingType],
    );

    final payload = <String, Object?>{
      'meter_id': meterId,
      'month': month,
      'index_prev_month': currentPrev,
      'index_last_month': currentLast,
      'index_consumption': consumption,
      'image_reading': imageReading,
      'reading_type': readingType,
      'created_at': now,
    };

    if (existed.isEmpty) {
      await db.insert('readings', payload);
    } else {
      final rawId = existed.first['reading_id'];
      final readingId = rawId is int ? rawId : int.parse('$rawId');

      await db.update(
        'readings',
        payload,
        where: 'reading_id = ?',
        whereArgs: [readingId],
      );
    }
  }

  /// Baseline cho tháng hiện tại:
  /// - nếu là first_billing_month => dùng initial_index
  /// - nếu không => fallback 0
  ///
  /// Lưu ý:
  /// prev của tháng bình thường sẽ lấy từ reading tháng trước.
  /// Hàm này chỉ dùng khi KHÔNG có reading tháng trước.
  Future<int> getBaselineForMonth({
    required DatabaseExecutor db,
    required String meterId,
    required String month,
  }) async {
    final meterRows = await db.query(
      'meters',
      columns: ['initial_index', 'first_billing_month'],
      where: 'meter_id = ?',
      whereArgs: [meterId],
      limit: 1,
    );

    if (meterRows.isEmpty) return 0;

    final row = meterRows.first;
    final initialIndex = _toInt(row['initial_index']) ?? 0;
    final firstBillingMonth = row['first_billing_month']?.toString();

    // ✅ tháng đầu billing của meter/company mới
    if (firstBillingMonth != null && firstBillingMonth == month) {
      return initialIndex;
    }

    return 0;
  }

  Future<void> createResetAtMonth({
    required DatabaseExecutor db,
    required String meterId,
    required DateTime resetDate,
    required int lastIndexBeforeReset,
    String? note,
  }) async {
    final resetMonth =
        '${resetDate.year}-${resetDate.month.toString().padLeft(2, '0')}';

    // Lấy offset của các reset trước tháng này, không lấy chính tháng này
    final previousOffset = await getOffsetBeforeMonth(
      meterId: meterId,
      month: resetMonth,
      db: db,
    );

    final offsetAfterReset = previousOffset + lastIndexBeforeReset;

    final existed = await db.rawQuery(
      '''
      SELECT id
      FROM meter_resets
      WHERE meter_id = ?
        AND reset_month = ?
      ORDER BY id DESC
      LIMIT 1
      ''',
      [meterId, resetMonth],
    );

    final payload = <String, Object?>{
      'meter_id': meterId,
      'reset_date': resetDate.toIso8601String(),
      'reset_month': resetMonth,
      'last_index_before_reset': lastIndexBeforeReset,
      'offset_after_reset': offsetAfterReset,
      'note': note,
    };

    if (existed.isEmpty) {
      await db.insert('meter_resets', payload);
    } else {
      final rawId = existed.first['id'];
      final id = rawId is int ? rawId : int.parse('$rawId');

      await db.update(
        'meter_resets',
        payload,
        where: 'id = ?',
        whereArgs: [id],
      );
    }
  }

  Future<int?> getStoredLastByMonth({
    required String meterId,
    required String month,
    DatabaseExecutor? db,
    String readingType = 'official',
  }) async {
    final executor = db ?? await DatabaseHelper.instance.database;

    final res = await executor.rawQuery(
      '''
      SELECT index_last_month
      FROM readings
      WHERE meter_id = ?
        AND month = ?
        AND reading_type = ?
      LIMIT 1
      ''',
      [meterId, month, readingType],
    );

    if (res.isEmpty) return null;

    return _toInt(res.first['index_last_month']);
  }

  Future<int> getOffsetBeforeOrAtMonth({
    required String meterId,
    required String month,
    DatabaseExecutor? db,
  }) async {
    final executor = db ?? await DatabaseHelper.instance.database;

    final res = await executor.rawQuery(
      '''
      SELECT offset_after_reset
      FROM meter_resets
      WHERE meter_id = ?
        AND reset_month <= ?
      ORDER BY reset_month DESC, id DESC
      LIMIT 1
      ''',
      [meterId, month],
    );

    if (res.isEmpty) return 0;

    return _toInt(res.first['offset_after_reset']) ?? 0;
  }

  Future<int> getOffsetBeforeMonth({
    required String meterId,
    required String month,
    DatabaseExecutor? db,
  }) async {
    final executor = db ?? await DatabaseHelper.instance.database;

    final res = await executor.rawQuery(
      '''
      SELECT offset_after_reset
      FROM meter_resets
      WHERE meter_id = ?
        AND reset_month < ?
      ORDER BY reset_month DESC, id DESC
      LIMIT 1
      ''',
      [meterId, month],
    );

    if (res.isEmpty) return 0;

    return _toInt(res.first['offset_after_reset']) ?? 0;
  }

  int? _toInt(dynamic v) {
    if (v == null) return null;
    if (v is int) return v;
    if (v is num) return v.toInt();
    return int.tryParse(v.toString());
  }

  String previousMonth(String month) {
    final parts = month.split('-');
    final year = int.parse(parts[0]);
    final m = int.parse(parts[1]);

    final dt = DateTime(year, m - 1, 1);
    final mm = dt.month.toString().padLeft(2, '0');
    return '${dt.year}-$mm';
  }

  String nextMonth(String month) {
    final parts = month.split('-');
    final year = int.parse(parts[0]);
    final m = int.parse(parts[1]);

    final dt = DateTime(year, m + 1, 1);
    final mm = dt.month.toString().padLeft(2, '0');
    return '${dt.year}-$mm';
  }
}