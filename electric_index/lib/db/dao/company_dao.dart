import 'package:sqflite/sqflite.dart';
import 'package:electric_index/db/dao/database_helper.dart';

class CompanyDao {
  /// Insert company (nếu trùng company_code thì bỏ qua)
  Future<int> insertCompany({
    required String companyCode,
    required String companyName,
  }) async {
    final db = await DatabaseHelper.instance.database;

    return db.insert('companies', {
      'company_code': companyCode,
      'company_name': companyName,
    }, conflictAlgorithm: ConflictAlgorithm.ignore);
  }

  /// Get company by code (dùng khi scan QR)
  Future<Map<String, dynamic>?> getCompanyByCode(String code) async {
    final db = await DatabaseHelper.instance.database;

    final res = await db.query(
      'companies',
      where: 'company_code = ?',
      whereArgs: [code],
      limit: 1,
    );

    return res.isNotEmpty ? res.first : null;
  }

  /// Get all companies
  Future<List<Map<String, dynamic>>> getAllCompanies() async {
    final db = await DatabaseHelper.instance.database;
    return db.query('companies', orderBy: 'company_name');
  }

  // DELETE
  Future<int> deleteCompany(int companyId) async {
    final db = await DatabaseHelper.instance.database;
    return db.delete(
      'companies',
      where: 'company_id = ?',
      whereArgs: [companyId],
    );
  }

  Future<int> updateCompany({
    required int companyId,
    required String companyName,
    required String companyCode,
  }) async {
    final db = await DatabaseHelper.instance.database;
    return await db.update(
      'companies',
      {'company_name': companyName, 'company_code': companyCode},
      where: 'company_id = ?',
      whereArgs: [companyId],
    );
  }
}
