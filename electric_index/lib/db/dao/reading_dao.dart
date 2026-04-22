import 'package:sqflite/sqflite.dart';
import 'package:electric_index/db/dao/database_helper.dart';
import 'dart:io';
import 'package:electric_index/db/dao/meter_reset_dao.dart';
import 'package:electric_index/models/sync_payload.dart';

class ReadingDao {
  final meterResetReadingService = MeterResetReadingService();

  static String _billingMonthFromDate(DateTime d, {int cutoffDay = 20}) {
    final ref = (d.day < cutoffDay)
        ? DateTime(d.year, d.month - 1, 1)
        : DateTime(d.year, d.month, 1);
    final mm = ref.month.toString().padLeft(2, '0');
    return '${ref.year}-$mm';
  }

  int? _toInt(dynamic value) {
    if (value == null) return null;
    if (value is int) return value;
    if (value is num) return value.toInt();
    return int.tryParse(value.toString());
  }

  /// Insert reading (1 meter / 1 month)
  /// Save chính thức đi qua MeterResetReadingService
  Future<void> insertReading({
    required String meterId,
    required String month,
    required int indexLast,
    String? imageReading,
    DateTime? createdAt,
    int cutoffDay = 20,
  }) async {
    final db = await DatabaseHelper.instance.database;

    await db.transaction((txn) async {
      await meterResetReadingService.upsertOfficialReadingForMonth(
        db: txn,
        meterId: meterId,
        month: month,
        indexLastMonth: indexLast,
        imageReading: imageReading,
        createdAt: createdAt,
        readingType: 'official',
      );
    });
  }

  /// Get all readings of a company (export Excel)
  Future<List<Map<String, dynamic>>> getReadingsByCompany(int companyId) async {
    final db = await DatabaseHelper.instance.database;

    return db.rawQuery(
      '''
      SELECT 
        c.company_name,
        m.meter_id,
        m.meter_name,
        r.month,
        r.index_prev_month,
        r.index_last_month,
        r.index_consumption
      FROM companies c
      JOIN meters m ON m.company_id = c.company_id
      JOIN readings r ON r.meter_id = m.meter_id
      WHERE c.company_id = ?
      ORDER BY m.meter_id, r.month
    ''',
      [companyId],
    );
  }

  /// Get latest reading of a meter + company info
  Future<Map<String, dynamic>?> getLatestReadingWithCompany(
    String meterId,
  ) async {
    final db = await DatabaseHelper.instance.database;
    final res = await db.rawQuery(
      '''
      SELECT r.*, m.meter_name, m.company_id, c.company_code, c.company_name
      FROM readings r
      JOIN meters m ON m.meter_id = r.meter_id
      JOIN companies c ON c.company_id = m.company_id
      WHERE r.meter_id = ?
      ORDER BY r.created_at DESC, r.reading_id DESC
      LIMIT 1
      ''',
      [meterId],
    );
    return res.isNotEmpty ? res.first : null;
  }

  /// Prev auto cho UI:
  /// 1) Ưu tiên reading tháng trước
  /// 2) Nếu không có, chỉ dùng initial_index khi đúng first_billing_month
  /// 3) Nếu meter có reset vật lý trước hoặc trong tháng này, cộng thêm offset
  Future<int> getBaselinePrevFromPrevMonth({
    required String meterId,
    required String currentMonth, // YYYY-MM
  }) async {
    final db = await DatabaseHelper.instance.database;

    final prevMonth = meterResetReadingService.previousMonth(currentMonth);

    final prevLast = await meterResetReadingService.getStoredLastByMonth(
      meterId: meterId,
      month: prevMonth,
      db: db,
      readingType: 'official',
    );

    if (prevLast != null) {
      return prevLast;
    }

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

    // ✅ Tháng đầu billing của meter/company mới
    if (firstBillingMonth != null && firstBillingMonth == currentMonth) {
      final offset = await meterResetReadingService.getOffsetBeforeOrAtMonth(
        meterId: meterId,
        month: currentMonth,
        db: db,
      );
      return initialIndex + offset;
    }

    return 0;
  }

  Future<List<Map<String, dynamic>>> getReadingsByCompanyMonth({
    required int companyId,
    required String month,
    required String conpanyName,
  }) async {
    final db = await DatabaseHelper.instance.database;

    return db.rawQuery(
      '''
      SELECT
        r.reading_id,
        r.meter_id,
        r.month,
        r.index_prev_month,
        r.index_last_month,
        r.index_consumption,
        r.image_reading,
        r.created_at,
        m.meter_name,
        l.floor,
        l.location_name
      FROM readings r
      JOIN meters m ON m.meter_id = r.meter_id
      LEFT JOIN locations l ON l.location_id = m.location_id
      WHERE m.company_id = ? AND r.month = ?
      ORDER BY m.meter_id ASC;
    ''',
      [companyId, month],
    );
  }

  /// all_company: readings của tất cả company trong 1 tháng
  Future<List<Map<String, dynamic>>> getAllCompaniesMonthReadings({
    required String month,
  }) async {
    final db = await DatabaseHelper.instance.database;

    return db.rawQuery(
      '''
      SELECT
        c.company_id,
        c.company_name,
        m.meter_id,
        m.meter_name,
        l.floor,
        l.location_name,
        r.month,
        r.index_prev_month,
        r.index_last_month,
        r.index_consumption,
        r.image_reading,
        r.created_at
      FROM readings r
      JOIN meters m ON m.meter_id = r.meter_id
      JOIN companies c ON c.company_id = m.company_id
      LEFT JOIN locations l ON l.location_id = m.location_id
      WHERE r.month = ?
      ORDER BY c.company_name ASC, m.meter_id ASC;
    ''',
      [month],
    );
  }

  /// DEPRECATED:
  /// Không nên dùng nữa.
  /// Logic baseline chuẩn hiện nằm ở MeterResetReadingService
  /// + getBaselinePrevFromPrevMonth() của ReadingDao.
  static Future<Map<String, dynamic>?> getFirstReadingOfMonth(
    DatabaseExecutor db,
    String meterId,
    String month,
  ) async {
    final rows = await db.query(
      'readings',
      where: 'meter_id = ? AND month = ?',
      whereArgs: [meterId, month],
      orderBy: "COALESCE(created_at, '9999-12-31') ASC, reading_id ASC",
      limit: 1,
    );
    return rows.isEmpty ? null : rows.first;
  }

  /// DEPRECATED:
  /// Không nên dùng nữa.
  /// Hàm cũ này trả 0 khi không có tháng trước, dễ sai với first_billing_month.
  static Future<int> getBaselineFromPrevMonthFirstReading(
    DatabaseExecutor db,
    String meterId,
    String month,
  ) async {
    final prevMonth = _prevBillingMonth(month);
    final first = await getFirstReadingOfMonth(db, meterId, prevMonth);
    if (first == null) return 0;
    final v = first['index_last_month'];
    if (v is int) return v;
    if (v is num) return v.toInt();
    return int.tryParse('$v') ?? 0;
  }

  static String _prevBillingMonth(String yyyyMm) {
    final y = int.parse(yyyyMm.substring(0, 4));
    final m = int.parse(yyyyMm.substring(5, 7));
    final d = DateTime(y, m, 1);
    final p = DateTime(d.year, d.month - 1, 1);
    return '${p.year}-${p.month.toString().padLeft(2, '0')}';
  }

  /// DEPRECATED:
  /// Save official reading thật nên đi qua MeterResetReadingService
  /// để xử lý reset vật lý + first_billing_month đúng.
  static Future<void> upsertOfficialReadingForMonth({
    required DatabaseExecutor db,
    required String meterId,
    required String month,
    required int indexLastMonth,
    String? imageReading,
    DateTime? createdAt,
    String readingType = 'official',
  }) async {
    final nowIso = (createdAt ?? DateTime.now()).toIso8601String();
    final first = await getFirstReadingOfMonth(db, meterId, month);
    final prevVal = await getBaselineFromPrevMonthFirstReading(
      db,
      meterId,
      month,
    );
    final consumption = indexLastMonth - prevVal;

    final data = <String, Object?>{
      'meter_id': meterId,
      'month': month,
      'index_prev_month': prevVal,
      'index_last_month': indexLastMonth,
      'index_consumption': consumption,
      'image_reading': imageReading,
      'created_at': nowIso,
      'reading_type': readingType,
    };

    if (first == null) {
      await db.insert('readings', data);
    } else {
      final id = first['reading_id'];
      if (id != null) {
        await db.update(
          'readings',
          data,
          where: 'reading_id = ?',
          whereArgs: [id],
        );
      }
    }
  }

  Future<void> deleteReadingAndImage(int readingId) async {
    final db = await DatabaseHelper.instance.database;

    final rows = await db.query(
      'readings',
      columns: ['image_reading'],
      where: 'reading_id = ?',
      whereArgs: [readingId],
      limit: 1,
    );

    final imgPath = rows.isNotEmpty
        ? (rows.first['image_reading'] as String?)
        : null;

    await db.delete(
      'readings',
      where: 'reading_id = ?',
      whereArgs: [readingId],
    );

    if (imgPath != null && imgPath.isNotEmpty) {
      final f = File(imgPath);
      if (await f.exists()) {
        await f.delete();
      }
    }
  }

  /// Lấy tất cả bản ghi của 1 tháng để sync lên server
  Future<List<SyncPayload>> getReadingsToSyncByMonth(String month) async {
    final db = await DatabaseHelper.instance.database;

    final rows = await db.query(
      'readings',
      columns: [
        'meter_id',
        'month',
        'index_last_month',
        'index_prev_month',
        'index_consumption',
        'image_reading',
        'created_at',
        'reading_type',
      ],
      where: 'month = ?',
      whereArgs: [month],
      orderBy: 'meter_id ASC',
    );

    return rows.map((r) {
      final meterId = (r['meter_id'] ?? '').toString();
      final m = (r['month'] ?? '').toString();
      final last = (r['index_last_month'] as int?) ?? 0;
      final prev = (r['index_prev_month'] as int?) ?? 0;
      final consumption = (r['index_consumption'] as int?) ?? 0;
      final createdAtStr = r['created_at']?.toString();
      final capturedAt = (createdAtStr == null || createdAtStr.isEmpty)
          ? null
          : DateTime.tryParse(createdAtStr.replaceFirst(' ', 'T'));

      final imagePath = r['image_reading']?.toString();

      return SyncPayload(
        meterId: meterId,
        month: m,
        indexLastMonth: last,
        capturedAt: capturedAt,
        imagePath: imagePath,
        indexPrevMonth: prev,
        indexConsumption: consumption,
      );
    }).toList();
  }

  /// Mark local sau khi sync thành công
  Future<void> markMonthSyncedOfficial(String month) async {
    final db = await DatabaseHelper.instance.database;
    await db.update(
      'readings',
      {'reading_type': 'official'},
      where: 'month = ?',
      whereArgs: [month],
    );
  }

  Future<void> upsertServerReading({
    required String meterId,
    required String month,
    required int indexPrevMonth,
    required int indexLastMonth,
    required int indexConsumption,
    String? createdAt,
  }) async {
    final db = await DatabaseHelper.instance.database;

    print('====== UPSERT SERVER READING ======');
    print('meterId: $meterId');
    print('month: $month');
    print('indexPrevMonth: $indexPrevMonth');
    print('indexLastMonth: $indexLastMonth');
    print('indexConsumption: $indexConsumption');
    print('createdAt from server: $createdAt');

    final existing = await db.query(
      'readings',
      where: 'meter_id = ? AND month = ? AND reading_type = ?',
      whereArgs: [meterId, month, 'official'],
      limit: 1,
    );

    print('existing rows: ${existing.length}');

    final data = <String, dynamic>{
      'meter_id': meterId,
      'month': month,
      'index_prev_month': indexPrevMonth,
      'index_last_month': indexLastMonth,
      'index_consumption': indexConsumption,
      'created_at': createdAt,
      'reading_type': 'official',
    };

    print('data to save: $data');

    if (existing.isEmpty) {
      data['created_at'] = DateTime.now().toIso8601String();

      print('ACTION: INSERT');
      print('INSERT DATA: $data');

      final id = await db.insert('readings', data);

      print('INSERT DONE. reading_id = $id');
    } else {
      print('ACTION: UPDATE');

      await db.update(
        'readings',
        data,
        where: 'meter_id = ? AND month = ? AND reading_type = ?',
        whereArgs: [meterId, month, 'official'],
      );

      print('UPDATE DONE');
    }

    print('====== END UPSERT ======');
  }

  Future<Map<String, dynamic>?> getOfficialReadingByMeterAndMonth(
    String meterId,
    String month,
  ) async {
    final db = await DatabaseHelper.instance.database;

    final rows = await db.query(
      'readings',
      where: 'meter_id = ? AND month = ? AND reading_type = ?',
      whereArgs: [meterId, month, 'official'],
      limit: 1,
    );

    if (rows.isEmpty) return null;
    return rows.first;
  }

  Future<List<String>> getAllActiveMeterIds() async {
    final db = await DatabaseHelper.instance.database;

    final rows = await db.query(
      'meters',
      columns: ['meter_id'],
      where: 'status = ?',
      whereArgs: ['active'],
      orderBy: 'meter_id ASC',
    );

    return rows
        .map((e) => e['meter_id']?.toString())
        .whereType<String>()
        .toList();
  }

  Future<int> countMetersRequiringPreviousMonth(String selectedMonth) async {
    final db = await DatabaseHelper.instance.database;

    final rows = await db.rawQuery(
      '''
      SELECT COUNT(*) AS total
      FROM meters m
      WHERE m.status = 'active'
        AND m.first_billing_month IS NOT NULL
        AND m.first_billing_month < ?
      ''',
      [selectedMonth],
    );

    return (rows.first['total'] as int?) ?? 0;
  }

  Future<List<String>> findMissingMeterIdsForMonth({
    required String selectedMonth,
    required String prevMonth,
  }) async {
    final db = await DatabaseHelper.instance.database;

    final rows = await db.rawQuery(
      '''
      SELECT m.meter_id
      FROM meters m
      WHERE m.status = 'active'
        AND m.first_billing_month IS NOT NULL
        AND m.first_billing_month < ?
        AND NOT EXISTS (
          SELECT 1
          FROM readings r
          WHERE r.meter_id = m.meter_id
            AND r.month = ?
            AND r.reading_type = 'official'
        )
      ORDER BY m.meter_id ASC
      ''',
      [selectedMonth, prevMonth],
    );

    return rows
        .map((e) => e['meter_id']?.toString())
        .whereType<String>()
        .toList();
  }

  Future<int> countMetersHavingOfficialReadingForMonthRequiredOnly({
    required String selectedMonth,
    required String prevMonth,
  }) async {
    final db = await DatabaseHelper.instance.database;

    final rows = await db.rawQuery(
      '''
      SELECT COUNT(*) AS total
      FROM meters m
      WHERE m.status = 'active'
        AND m.first_billing_month IS NOT NULL
        AND m.first_billing_month < ?
        AND EXISTS (
          SELECT 1
          FROM readings r
          WHERE r.meter_id = m.meter_id
            AND r.month = ?
            AND r.reading_type = 'official'
        )
      ''',
      [selectedMonth, prevMonth],
    );

    return (rows.first['total'] as int?) ?? 0;
  }
}