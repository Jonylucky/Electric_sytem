import 'dart:convert';
import 'package:flutter/services.dart' show rootBundle;
import 'package:sqflite/sqflite.dart';

class CompanyImporter {
  Future<int> importFromAsset(
    DatabaseExecutor db,
    String assetPath, {
    ConflictAlgorithm conflictAlgorithm = ConflictAlgorithm.ignore,
  }) async {
    final jsonStr = await rootBundle.loadString(assetPath);
    final decoded = jsonDecode(jsonStr);

    final List<dynamic> items = (decoded is List)
        ? decoded
        : ((decoded as Map<String, dynamic>)['companies'] ?? [])
              as List<dynamic>;

    int inserted = 0;
    final batch = db.batch();

    for (final raw in items) {
      final c = raw as Map<String, dynamic>;

      final companyId = int.tryParse(c['company_id']?.toString() ?? '');
      final companyName = (c['company_name'] ?? '').toString().trim();
      final companyCode = c['company_code']?.toString();
      final createdAt = c['created_at']?.toString();

      if (companyId == null || companyName.isEmpty) continue;

      batch.insert('companies', {
        'company_id': companyId,
        'company_code': companyCode,
        'company_name': companyName,
        'created_at': createdAt,
      }, conflictAlgorithm: conflictAlgorithm);
      inserted++;
    }

    await batch.commit(noResult: true);
    return inserted;
  }
}
