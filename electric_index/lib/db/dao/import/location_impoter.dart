import 'dart:convert';
import 'package:flutter/services.dart' show rootBundle;
import 'package:sqflite/sqflite.dart';

class LocationImporter {
  Future<int> importFromAsset(
    DatabaseExecutor db,
    String assetPath, {
    ConflictAlgorithm conflictAlgorithm = ConflictAlgorithm.ignore,
  }) async {
    final jsonStr = await rootBundle.loadString(assetPath);
    final decoded = jsonDecode(jsonStr);

    final List<dynamic> items = (decoded is List)
        ? decoded
        : ((decoded as Map<String, dynamic>)['locations'] ?? [])
              as List<dynamic>;

    int inserted = 0;
    final batch = db.batch();

    for (final raw in items) {
      final l = raw as Map<String, dynamic>;

      final locationId = int.tryParse(l['location_id']?.toString() ?? '');
      final locationName = (l['location_name'] ?? '').toString().trim();
      final floor = int.tryParse(l['floor']?.toString() ?? '');

      if (locationId == null || locationName.isEmpty || floor == null) continue;

      batch.insert('locations', {
        'location_id': locationId,
        'location_name': locationName,
        'floor': floor,
      }, conflictAlgorithm: conflictAlgorithm);
      inserted++;
    }

    await batch.commit(noResult: true);
    return inserted;
  }
}
