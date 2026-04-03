import 'package:sqflite/sqflite.dart';

import '../dao/database_helper.dart';
import '../dao/import/company_importer.dart';
import '../dao/import/location_impoter.dart';
import '../dao/import/meter_importer.dart';
import '../dao/import/readings_importer.dart'; // class ReadingsImporter của mày

class DbSeeder {
  DbSeeder._();

  static const _metaTable = 'app_meta';
  static const _seedKey = 'seed_done_v1'; // đổi v2 nếu muốn seed lại

  /// Call 1 lần lúc app start
  static Future<void> seedIfNeeded() async {
    final db = await DatabaseHelper.instance.database;

    await _ensureMetaTable(db);

    final seeded = await _isSeeded(db);
    if (seeded) return;

    await db.transaction((txn) async {
      // ✅ Import theo đúng thứ tự FK cha -> con
      await CompanyImporter().importFromAsset(
        txn,
        'lib/assets/data/companies_seed.json',
      );

      await LocationImporter().importFromAsset(
        txn,
        'lib/assets/data/locations_seed.json',
      );

      await MeterImporter().importFromAsset(
        txn,
        'lib/assets/data/meters_seed.json',
      );

      // ReadingsImporter của mày đang tự lấy db transaction riêng,
      // nên tao viết lại 1 hàm importFromAssetTxn để chạy cùng transaction.
      await ReadingsImporter().importFromAssetTxn(
        txn,
        'lib/assets/data/readings_seed.json',
      );

      await _markSeeded(txn);
    });
  }

  // =========================
  // META
  // =========================
  static Future<void> _ensureMetaTable(DatabaseExecutor db) async {
    await db.execute('''
      CREATE TABLE IF NOT EXISTS $_metaTable (
        key TEXT PRIMARY KEY,
        value TEXT
      )
    ''');
  }

  static Future<bool> _isSeeded(DatabaseExecutor db) async {
    final res = await db.query(
      _metaTable,
      where: 'key = ?',
      whereArgs: [_seedKey],
      limit: 1,
    );
    return res.isNotEmpty;
  }

  static Future<void> _markSeeded(DatabaseExecutor db) async {
    await db.insert(_metaTable, {'key': _seedKey, 'value': 'true'});
  }
}
