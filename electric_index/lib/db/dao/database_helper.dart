// lib/db/database_helper.dart
//
// Flutter packages needed:
//   sqflite, path_provider, path
//
// Usage:
//   final db = await DatabaseHelper.instance.database;
//   await db.query('companies');
//
// Notes:
// - bật foreign keys (SQLite không bật mặc định trên Android)
// - onCreate: dùng cho cài mới
// - onUpgrade: dùng cho DB cũ nâng version

import 'dart:io';

import 'package:path/path.dart' as p;
import 'package:path_provider/path_provider.dart';
import 'package:sqflite/sqflite.dart';

class DatabaseHelper {
  DatabaseHelper._internal();
  static final DatabaseHelper instance = DatabaseHelper._internal();

  static const String _dbName = 'electric_index.db';
  static const int _dbVersion = 6;

  Database? _db;

  Future<Database> get database async {
    if (_db != null) return _db!;
    _db = await _openDb();
    return _db!;
  }

  Future<void> close() async {
    final db = _db;
    if (db != null) {
      await db.close();
      _db = null;
    }
  }

  Future<String> _dbPath() async {
    final Directory dir = await getApplicationDocumentsDirectory();
    return p.join(dir.path, _dbName);
  }

  Future<Database> _openDb() async {
    final String path = await _dbPath();

    return openDatabase(
      path,
      version: _dbVersion,
      onConfigure: (db) async {
        await db.execute('PRAGMA foreign_keys = ON;');
      },
      onCreate: (db, version) async {
        await _createSchema(db);
      },
      onUpgrade: (db, oldVersion, newVersion) async {
        await _upgradeSchema(db, oldVersion, newVersion);
      },
    );
  }

  Future<void> _createSchema(Database db) async {
    // 1) companies
    await db.execute('''
      CREATE TABLE IF NOT EXISTS companies (
        company_id    INTEGER PRIMARY KEY AUTOINCREMENT,
        company_code  TEXT UNIQUE,
        company_name  TEXT NOT NULL,
        created_at    TEXT DEFAULT (datetime('now'))
      );
    ''');

    // 2) locations
    await db.execute('''
      CREATE TABLE IF NOT EXISTS locations (
        location_id   INTEGER PRIMARY KEY AUTOINCREMENT,
        location_name TEXT NOT NULL,
        floor         INTEGER NOT NULL,
        UNIQUE(location_name, floor)
      );
    ''');

    // 3) meters
    await db.execute('''
      CREATE TABLE IF NOT EXISTS meters (
        meter_id               TEXT PRIMARY KEY,
        company_id             INTEGER NOT NULL,
        meter_name             TEXT,
        location_id            INTEGER,
        status                 TEXT NOT NULL DEFAULT 'active',
        installed_at           TEXT,
        replaced_at            TEXT,
        replaced_by_meter_id   TEXT,
        initial_index          INTEGER NOT NULL DEFAULT 0,
        first_billing_month    TEXT,
        created_at             TEXT DEFAULT (datetime('now')),
        FOREIGN KEY (company_id) REFERENCES companies(company_id)
          ON UPDATE CASCADE ON DELETE CASCADE,
        FOREIGN KEY (location_id) REFERENCES locations(location_id)
          ON UPDATE CASCADE ON DELETE SET NULL
      );
    ''');

    await db.execute(
      'CREATE INDEX IF NOT EXISTS idx_meters_company_id ON meters(company_id);',
    );
    await db.execute(
      'CREATE INDEX IF NOT EXISTS idx_meters_location_id ON meters(location_id);',
    );
    await db.execute(
      'CREATE INDEX IF NOT EXISTS idx_meters_status ON meters(status);',
    );
    await db.execute(
      'CREATE INDEX IF NOT EXISTS idx_meters_first_billing_month ON meters(first_billing_month);',
    );

    // 4) readings
    await db.execute('''
      CREATE TABLE IF NOT EXISTS readings (
        reading_id         INTEGER PRIMARY KEY AUTOINCREMENT,
        meter_id           TEXT NOT NULL,
        month              TEXT NOT NULL,
        index_prev_month   INTEGER NOT NULL,
        index_last_month   INTEGER NOT NULL,
        index_consumption  INTEGER NOT NULL,
        image_reading      TEXT,
        reading_type       TEXT NOT NULL DEFAULT 'normal',
        created_at         TEXT DEFAULT (datetime('now')),
        FOREIGN KEY (meter_id) REFERENCES meters(meter_id)
          ON UPDATE CASCADE ON DELETE CASCADE
      );
    ''');

    await db.execute(
      'CREATE INDEX IF NOT EXISTS idx_readings_meter_id ON readings(meter_id);',
    );
    await db.execute(
      'CREATE INDEX IF NOT EXISTS idx_readings_month ON readings(month);',
    );
    await db.execute(
      'CREATE INDEX IF NOT EXISTS idx_readings_type ON readings(reading_type);',
    );

    // Mỗi meter + month + reading_type chỉ có 1 record
    await db.execute('''
      CREATE UNIQUE INDEX IF NOT EXISTS uq_readings_meter_month_type
      ON readings(meter_id, month, reading_type);
    ''');

    // 5) meter_replacements
    await db.execute('''
      CREATE TABLE IF NOT EXISTS meter_replacements (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        old_meter_id TEXT NOT NULL,
        new_meter_id TEXT NOT NULL,
        replacement_date TEXT NOT NULL,
        last_index_old INTEGER NOT NULL,
        initial_index_new INTEGER NOT NULL DEFAULT 0,
        note TEXT,
        created_at TEXT NOT NULL DEFAULT (datetime('now'))
      );
    ''');

    await db.execute(
      'CREATE INDEX IF NOT EXISTS idx_repl_old ON meter_replacements(old_meter_id);',
    );
    await db.execute(
      'CREATE INDEX IF NOT EXISTS idx_repl_new ON meter_replacements(new_meter_id);',
    );
    await db.execute(
      'CREATE INDEX IF NOT EXISTS idx_repl_date ON meter_replacements(replacement_date);',
    );

    // 6) meter_resets
    await db.execute('''
      CREATE TABLE IF NOT EXISTS meter_resets (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        meter_id TEXT NOT NULL,
        reset_date TEXT NOT NULL,
        reset_month TEXT NOT NULL,
        last_index_before_reset INTEGER NOT NULL,
        offset_after_reset INTEGER NOT NULL,
        note TEXT,
        created_at TEXT NOT NULL DEFAULT (datetime('now')),
        FOREIGN KEY (meter_id) REFERENCES meters(meter_id)
          ON UPDATE CASCADE ON DELETE CASCADE
      );
    ''');

    await db.execute(
      'CREATE INDEX IF NOT EXISTS idx_resets_meter ON meter_resets(meter_id);',
    );
    await db.execute(
      'CREATE INDEX IF NOT EXISTS idx_resets_month ON meter_resets(reset_month);',
    );
  }

  Future<void> _upgradeSchema(
    Database db,
    int oldVersion,
    int newVersion,
  ) async {
    await db.transaction((txn) async {
      // ===== Upgrade lên v6 =====
      if (oldVersion < 6) {
        // meters
        await _addColumnIfNotExists(
          txn,
          'meters',
          'status',
          "TEXT NOT NULL DEFAULT 'active'",
        );
        await _addColumnIfNotExists(txn, 'meters', 'installed_at', 'TEXT');
        await _addColumnIfNotExists(txn, 'meters', 'replaced_at', 'TEXT');
        await _addColumnIfNotExists(
          txn,
          'meters',
          'replaced_by_meter_id',
          'TEXT',
        );
        await _addColumnIfNotExists(
          txn,
          'meters',
          'initial_index',
          'INTEGER NOT NULL DEFAULT 0',
        );
        await _addColumnIfNotExists(
          txn,
          'meters',
          'first_billing_month',
          'TEXT',
        );

        // Fill giá trị mặc định cho dữ liệu cũ
        await txn.execute('''
          UPDATE meters
          SET first_billing_month = '2026-01'
          WHERE first_billing_month IS NULL
        ''');

        // indexes meters
        await txn.execute(
          'CREATE INDEX IF NOT EXISTS idx_meters_company_id ON meters(company_id);',
        );
        await txn.execute(
          'CREATE INDEX IF NOT EXISTS idx_meters_location_id ON meters(location_id);',
        );
        await txn.execute(
          'CREATE INDEX IF NOT EXISTS idx_meters_status ON meters(status);',
        );
        await txn.execute(
          'CREATE INDEX IF NOT EXISTS idx_meters_first_billing_month ON meters(first_billing_month);',
        );

        // readings
        await _addColumnIfNotExists(
          txn,
          'readings',
          'reading_type',
          "TEXT NOT NULL DEFAULT 'normal'",
        );

        // nếu DB cũ từng có unique cũ thì bỏ đi
        await txn.execute('DROP INDEX IF EXISTS uq_readings_meter_month;');

        // chuẩn hóa data cũ
        await txn.execute("""
          UPDATE readings
          SET reading_type = 'official'
          WHERE reading_type = 'normal'
        """);

        await txn.execute(
          'CREATE INDEX IF NOT EXISTS idx_readings_meter_id ON readings(meter_id);',
        );
        await txn.execute(
          'CREATE INDEX IF NOT EXISTS idx_readings_month ON readings(month);',
        );
        await txn.execute(
          'CREATE INDEX IF NOT EXISTS idx_readings_type ON readings(reading_type);',
        );

        await txn.execute('''
          CREATE UNIQUE INDEX IF NOT EXISTS uq_readings_meter_month_type
          ON readings(meter_id, month, reading_type);
        ''');

        // meter_replacements
        await txn.execute('''
          CREATE TABLE IF NOT EXISTS meter_replacements (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            old_meter_id TEXT NOT NULL,
            new_meter_id TEXT NOT NULL,
            replacement_date TEXT NOT NULL,
            last_index_old INTEGER NOT NULL,
            initial_index_new INTEGER NOT NULL DEFAULT 0,
            note TEXT,
            created_at TEXT NOT NULL DEFAULT (datetime('now'))
          );
        ''');

        await txn.execute(
          'CREATE INDEX IF NOT EXISTS idx_repl_old ON meter_replacements(old_meter_id);',
        );
        await txn.execute(
          'CREATE INDEX IF NOT EXISTS idx_repl_new ON meter_replacements(new_meter_id);',
        );
        await txn.execute(
          'CREATE INDEX IF NOT EXISTS idx_repl_date ON meter_replacements(replacement_date);',
        );

        // meter_resets
        await txn.execute('''
          CREATE TABLE IF NOT EXISTS meter_resets (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            meter_id TEXT NOT NULL,
            reset_date TEXT NOT NULL,
            reset_month TEXT NOT NULL,
            last_index_before_reset INTEGER NOT NULL,
            offset_after_reset INTEGER NOT NULL,
            note TEXT,
            created_at TEXT NOT NULL DEFAULT (datetime('now')),
            FOREIGN KEY (meter_id) REFERENCES meters(meter_id)
              ON UPDATE CASCADE ON DELETE CASCADE
          );
        ''');

        await txn.execute(
          'CREATE INDEX IF NOT EXISTS idx_resets_meter ON meter_resets(meter_id);',
        );
        await txn.execute(
          'CREATE INDEX IF NOT EXISTS idx_resets_month ON meter_resets(reset_month);',
        );

        print('Migration to v6 done.');
      }
    });
  }

  static Future<void> _addColumnIfNotExists(
    DatabaseExecutor db,
    String table,
    String column,
    String columnDef,
  ) async {
    final cols = await db.rawQuery('PRAGMA table_info($table);');
    final exists = cols.any((r) => (r['name'] as String?) == column);

    if (!exists) {
      await db.execute('ALTER TABLE $table ADD COLUMN $column $columnDef;');
    }
  }

  Future<T> transaction<T>(Future<T> Function(Transaction txn) action) async {
    final db = await database;
    return db.transaction(action);
  }
}
