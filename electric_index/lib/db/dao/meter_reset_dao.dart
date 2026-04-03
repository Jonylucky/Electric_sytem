import 'package:sqflite/sqflite.dart';
import 'package:electric_index/db/dao/database_helper.dart';

class MeterResetReadingService {
  Future<void> upsertOfficialReadingForMonth({
    required DatabaseExecutor db,
    required String meterId,
    required String month, // YYYY-MM
    required int indexLastMonth, // user nhập: physical index
    String? imageReading,
    DateTime? createdAt,
    String readingType = 'official',
  }) async {
    final now = (createdAt ?? DateTime.now()).toIso8601String();

    // ✅ last của tháng trước đã lưu trong readings
    // meter thường => là physical
    // meter reset => là logical
    final prevLast = await getStoredLastByMonth(
      meterId: meterId,
      month: previousMonth(month),
      db: db,
      readingType: readingType,
    );

    // ✅ offset áp dụng cho tháng hiện tại
    final offset = await getOffsetBeforeOrAtMonth(
      meterId: meterId,
      month: month,
      db: db,
    );

    // ✅ currentLast sẽ là giá trị lưu thật vào readings.index_last_month
    // Nếu không reset => offset = 0 => currentLast = physical
    // Nếu reset => currentLast = physical + offset = logical
    final currentLast = indexLastMonth + offset;

    final currentPrev = prevLast ?? 0;
    final consumption = prevLast == null ? 0 : currentLast - currentPrev;

    if (consumption < 0) {
      throw Exception(
        'Consumption âm bất thường: meter=$meterId, month=$month, currentLast=$currentLast, prevLast=$currentPrev',
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

    final payload = {
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
        {
          'index_prev_month': currentPrev,
          'index_last_month': currentLast,
          'index_consumption': consumption,
          'image_reading': imageReading,
          'reading_type': readingType,
          'created_at': now,
        },
        where: 'reading_id = ?',
        whereArgs: [readingId],
      );
    }

    // ✅ Với schema hiện tại không lưu physical riêng,
    // nên KHÔNG recalc tháng sau tự động được an toàn.
    // Vì tháng sau đang lưu giá trị đã chuẩn hóa rồi, không còn raw physical để tính lại.
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

    // ✅ lấy offset của các reset TRƯỚC tháng này, không lấy chính tháng này
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

    final payload = {
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

    // ✅ Không recalc reading ở đây.
    // Với schema hiện tại không có raw physical lưu riêng,
    // recalc tháng reset / tháng sau có thể sai.
    // Hãy tạo reset trước, rồi khi user ghi số tháng đó/tháng sau
    // thì upsertOfficialReadingForMonth() sẽ tự áp offset đúng.
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

    final v = res.first['index_last_month'];
    if (v is int) return v;
    if (v is num) return v.toInt();
    return int.tryParse('$v');
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

    final v = res.first['offset_after_reset'];
    if (v is int) return v;
    if (v is num) return v.toInt();
    return int.tryParse('$v') ?? 0;
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

    final v = res.first['offset_after_reset'];
    if (v is int) return v;
    if (v is num) return v.toInt();
    return int.tryParse('$v') ?? 0;
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
