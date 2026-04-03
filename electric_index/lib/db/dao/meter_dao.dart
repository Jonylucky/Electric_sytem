import 'package:sqflite/sqflite.dart';
import 'package:electric_index/db/dao/database_helper.dart';
import 'package:electric_index/db/dao/reading_dao.dart';

class MeterDao {
  //date
  String nextMonthYyyyMm([DateTime? from]) {
    final now = from ?? DateTime.now();
    final dt = DateTime(now.year, now.month + 1, 1);
    final mm = dt.month.toString().padLeft(2, '0');
    return '${dt.year}-$mm';
  }

  Future<int> insertMeter({
    required String meterId,
    required int companyId,
    int? locationId,
    String? meterName,
  }) async {
    final db = await DatabaseHelper.instance.database;

    return await db.insert('meters', {
      'meter_id': meterId,
      'company_id': companyId,
      'location_id': locationId,
      'meter_name': meterName,
      'status': 'active',
      'first_billing_month': nextMonthYyyyMm(), // ✅ tháng sau mới ghi
      'created_at': DateTime.now().toIso8601String(),
    });
  }

  Future<int> updateMeter({
    required String meterId,
    required int companyId,
    int? locationId,
    String? meterName,
  }) async {
    final db = await DatabaseHelper.instance.database;
    return db.update(
      'meters',
      {
        'company_id': companyId,
        'location_id': locationId,
        'meter_name': meterName,
      },
      where: 'meter_id = ?',
      whereArgs: [meterId],
    );
  }

  Future<Map<String, dynamic>?> getMeterById(String meterId) async {
    final db = await DatabaseHelper.instance.database;
    final res = await db.query(
      'meters',
      where: 'meter_id = ?',
      whereArgs: [meterId],
      limit: 1,
    );
    return res.isNotEmpty ? res.first : null;
  }

  Future<int> deleteMeter(String meterId) async {
    final db = await DatabaseHelper.instance.database;
    return db.delete('meters', where: 'meter_id = ?', whereArgs: [meterId]);
  }

  /// List meters + join company + location để hiển thị
  Future<List<Map<String, dynamic>>> getMetersView() async {
    final db = await DatabaseHelper.instance.database;
    return db.rawQuery('''
      SELECT
        m.meter_id,
        m.meter_name,
        m.company_id,
        c.company_name,
        m.location_id,
        l.location_name,
        l.floor,
        m.created_at
      FROM meters m
      JOIN companies c ON c.company_id = m.company_id
      LEFT JOIN locations l ON l.location_id = m.location_id
      ORDER BY c.company_name, m.meter_id;
    ''');
  }

  /// Get all meters by company
  Future<List<Map<String, dynamic>>> getMetersByCompany(int companyId) async {
    final db = await DatabaseHelper.instance.database;
    return db.query(
      'meters',
      columns: ['meter_id', 'meter_name', 'company_id'],
      where: 'company_id = ?',
      whereArgs: [companyId],
      orderBy: 'meter_name ASC',
    );
  }

  /// Thay đồng hồ (Mode A):
  /// - meter cũ: tạo/Update record 'closing' cho kỳ month
  /// - meter mới: insert + tạo record 'opening' (prev=last=initial, consumption=0)
  /// - update meters + log meter_replacements
  Future<void> replaceMeter({
    required String oldMeterId,
    required String newMeterId,
    required DateTime replacementDate,
    required int lastIndexOld,
    required int initialIndexNew,
    String? note,
    String? oldImage,
    String? newImage,
  }) async {
    final db = await DatabaseHelper.instance.database;
    final replaceAtIso = replacementDate.toIso8601String();
    final month = _billingMonthFromDate(replacementDate);

    await db.transaction((txn) async {
      // 1) load old meter info để copy
      final oldRows = await txn.query(
        'meters',
        columns: ['company_id', 'location_id', 'meter_name'],
        where: 'meter_id = ?',
        whereArgs: [oldMeterId],
        limit: 1,
      );
      if (oldRows.isEmpty) {
        throw Exception('Old meter not found: $oldMeterId');
      }

      final companyId = oldRows.first['company_id'] as int;
      final locationId = oldRows.first['location_id'] as int?;
      final oldName = oldRows.first['meter_name'] as String?;
      final newMeterName =
          oldName; // ✅ default copy name (mày muốn đổi thì đổi ở UI sau)

      // 2) ensure new not exist
      final newRows = await txn.query(
        'meters',
        columns: ['meter_id'],
        where: 'meter_id = ?',
        whereArgs: [newMeterId],
        limit: 1,
      );
      if (newRows.isNotEmpty) {
        throw Exception('New meter already exists: $newMeterId');
      }

      // 3) Closing reading cho meter cũ (official = first record của tháng)
      await ReadingDao.upsertOfficialReadingForMonth(
        db: txn,
        meterId: oldMeterId,
        month: month,
        indexLastMonth: lastIndexOld,
        imageReading: oldImage,
        createdAt: replacementDate,
        readingType: 'closing',
      );

      // 4) Insert meter mới (active)
      await txn.insert('meters', {
        'meter_id': newMeterId,
        'company_id': companyId,
        'location_id': locationId,
        'meter_name': newMeterName,
        'status': 'active',
        'installed_at': replaceAtIso,
        'initial_index': initialIndexNew,
      });

      // 5) Opening reading cho meter mới (Mode A: consumption=0)
      await _upsertOpeningFixedZero(
        db: txn,
        meterId: newMeterId,
        month: month,
        initialIndex: initialIndexNew,
        imageReading: newImage,
        createdAt: replacementDate,
      );

      // 6) Update old meter status
      await txn.update(
        'meters',
        {
          'status': 'replaced',
          'replaced_at': replaceAtIso,
          'replaced_by_meter_id': newMeterId,
        },
        where: 'meter_id = ?',
        whereArgs: [oldMeterId],
      );

      // 7) log replace
      await txn.insert('meter_replacements', {
        'old_meter_id': oldMeterId,
        'new_meter_id': newMeterId,
        'replacement_date': replaceAtIso,
        'last_index_old': lastIndexOld,
        'initial_index_new': initialIndexNew,
        'note': note,
      });
    });
  }

  /// ✅ Mode A opening record: prev=last=initial, consumption=0
  static Future<void> _upsertOpeningFixedZero({
    required DatabaseExecutor db,
    required String meterId,
    required String month,
    required int initialIndex,
    String? imageReading,
    DateTime? createdAt,
  }) async {
    final nowIso = (createdAt ?? DateTime.now()).toIso8601String();

    final first = await ReadingDao.getFirstReadingOfMonth(db, meterId, month);

    final data = <String, Object?>{
      'meter_id': meterId,
      'month': month,
      'index_prev_month': initialIndex,
      'index_last_month': initialIndex,
      'index_consumption': 0,
      'image_reading': imageReading,
      'created_at': nowIso,
      'reading_type': 'opening',
    };

    if (first == null) {
      await db.insert('readings', data);
    } else {
      final id = first['reading_id'];
      await db.update(
        'readings',
        data,
        where: 'reading_id = ?',
        whereArgs: [id],
      );
    }
  }

  String _billingMonthFromDate(DateTime d, {int cutoffDay = 23}) {
    final ref = (d.day < cutoffDay)
        ? DateTime(d.year, d.month - 1, 1)
        : DateTime(d.year, d.month, 1);
    final mm = ref.month.toString().padLeft(2, '0');
    return '${ref.year}-$mm';
  }

  /// Chuỗi meter: [startMeterId, replaced_by, ...] để báo cáo theo location/company.
  Future<List<String>> getMeterChain(String startMeterId) async {
    final db = await DatabaseHelper.instance.database;
    final chain = <String>[];
    String? cur = startMeterId;

    while (cur != null) {
      chain.add(cur);
      final rows = await db.query(
        'meters',
        columns: ['replaced_by_meter_id'],
        where: 'meter_id = ?',
        whereArgs: [cur],
        limit: 1,
      );
      if (rows.isEmpty) break;
      cur = rows.first['replaced_by_meter_id'] as String?;
    }
    return chain;
  }
}
