import 'package:sqflite/sqflite.dart';
import 'package:electric_index/db/dao/database_helper.dart';

class LocationsDao {
  // CREATE
  Future<int> createLocation({
    required String locationName,
    required int floor,
  }) async {
    final db = await DatabaseHelper.instance.database;

    return db.insert(
      'locations',
      {'location_name': locationName.trim(), 'floor': floor},
      conflictAlgorithm: ConflictAlgorithm.abort, // trùng name+floor -> lỗi
    );
  }

  // READ - tất cả location
  Future<List<Map<String, dynamic>>> getAllLocations() async {
    final db = await DatabaseHelper.instance.database;

    return db.query('locations', orderBy: 'floor, location_name');
  }

  // READ - theo id
  Future<Map<String, dynamic>?> getLocationById(int locationId) async {
    final db = await DatabaseHelper.instance.database;

    final res = await db.query(
      'locations',
      where: 'location_id = ?',
      whereArgs: [locationId],
      limit: 1,
    );
    return res.isNotEmpty ? res.first : null;
  }

  // UPDATE
  Future<int> updateLocation({
    required int locationId,
    required String locationName,
    required int floor,
  }) async {
    final db = await DatabaseHelper.instance.database;

    return db.update(
      'locations',
      {'location_name': locationName.trim(), 'floor': floor},
      where: 'location_id = ?',
      whereArgs: [locationId],
    );
  }

  // DELETE
  Future<int> deleteLocation(int locationId) async {
    final db = await DatabaseHelper.instance.database;

    return db.delete(
      'locations',
      where: 'location_id = ?',
      whereArgs: [locationId],
    );
  }
}
