import 'dart:convert';
import 'package:flutter/services.dart' show rootBundle;
import 'package:sqflite/sqflite.dart';

class MeterImporter {
  Future<int> importFromAsset(
    DatabaseExecutor db,
    String assetPath, {
    ConflictAlgorithm conflictAlgorithm = ConflictAlgorithm.ignore,
  }) async {
    final jsonStr = await rootBundle.loadString(assetPath);
    final decoded = jsonDecode(jsonStr);

    final List<dynamic> items = (decoded is List)
        ? decoded
        : ((decoded as Map<String, dynamic>)['meters'] ?? []) as List<dynamic>;

    int inserted = 0;
    final batch = db.batch();

    for (final raw in items) {
      final m = raw as Map<String, dynamic>;

      final meterId = (m['meter_id'] ?? '').toString().trim();
      final companyId = int.tryParse(m['company_id']?.toString() ?? '');
      final meterName = m['meter_name']?.toString();
      final locationId = (m['location_id'] == null)
          ? null
          : int.tryParse(m['location_id'].toString());
      final createdAt = m['created_at']?.toString();

      if (meterId.isEmpty || companyId == null) continue;

      batch.insert('meters', {
        'meter_id': meterId,
        'company_id': companyId,
        'meter_name': meterName,
        'location_id': locationId,
        'created_at': createdAt,
      }, conflictAlgorithm: conflictAlgorithm);
      inserted++;
    }

    await batch.commit(noResult: true);
    return inserted;
  }
}
