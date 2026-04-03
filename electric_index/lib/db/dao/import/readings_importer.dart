import 'dart:convert';
import 'package:flutter/services.dart' show rootBundle;
import 'package:sqflite/sqflite.dart';

class ReadingsImporter {
  Future<int> importFromAssetTxn(DatabaseExecutor db, String assetPath) async {
    final jsonStr = await rootBundle.loadString(assetPath);
    final decoded = jsonDecode(jsonStr);

    final List<dynamic> items = (decoded is List)
        ? decoded
        : ((decoded as Map<String, dynamic>)['readings'] ?? [])
              as List<dynamic>;

    int inserted = 0;
    final batch = db.batch();

    for (final raw in items) {
      final m = raw as Map<String, dynamic>;

      final meterId = (m['meter_id'] ?? '').toString().trim();
      final month = (m['month'] ?? '').toString().trim();
      final prev = int.tryParse(m['index_prev_month']?.toString() ?? '') ?? 0;
      final last = int.tryParse(m['index_last_month']?.toString() ?? '') ?? 0;
      final image = m['image_reading']?.toString();
      final createdAt = m['created_at']?.toString();

      if (meterId.isEmpty || month.isEmpty) continue;
      if (last < prev) continue;

      batch.insert('readings', {
        'meter_id': meterId,
        'month': month,
        'index_prev_month': prev,
        'index_last_month': last,
        'index_consumption': last - prev,
        'image_reading': image,
        'created_at': createdAt,
      }, conflictAlgorithm: ConflictAlgorithm.ignore);

      inserted++;
    }

    await batch.commit(noResult: true);
    return inserted;
  }
}
