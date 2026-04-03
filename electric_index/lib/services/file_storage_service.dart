import 'dart:io';
import 'package:path/path.dart' as p;
import 'package:path_provider/path_provider.dart';

class FileStorageService {
  static const String _rootFolder = 'electric_index';

  Future<Directory> _rootDir() async {
    final base = await getApplicationDocumentsDirectory();
    final dir = Directory(p.join(base.path, _rootFolder));
    if (!await dir.exists()) await dir.create(recursive: true);
    return dir;
  }

  Future<Directory> companyDir(String conpanyName) async {
    final root = await _rootDir();
    final dir = Directory(
      p.join(root.path, 'companies', 'company_$conpanyName'),
    );
    if (!await dir.exists()) await dir.create(recursive: true);
    return dir;
  }

  /// Xoá toàn bộ file export của 1 công ty trong 1 tháng
  Future<void> deleteExportByCompanyMonth({
    required int companyId,
    required String yyyyMm,
    required String companyName,
  }) async {
    final dir = await companyExportsDir(companyId, yyyyMm, companyName);

    if (await dir.exists()) {
      await dir.delete(recursive: true);
    }
  }

  Future<Directory> meterImagesDir(
    int companyId,
    String meterId,
    String conpanyName,
  ) async {
    final cdir = await companyDir(conpanyName);
    final dir = Directory(p.join(cdir.path, 'meters', meterId, 'images'));
    if (!await dir.exists()) await dir.create(recursive: true);
    return dir;
  }

  Future<Directory> companyExportsDir(
    int companyId,
    String yyyyMm,
    String? companyName,
  ) async {
    final safeCompanyName = companyName ?? 'unknown_company';
    final cdir = await companyDir(safeCompanyName);
    final year = yyyyMm.substring(0, 4);

    final dir = Directory(p.join(cdir.path, 'exports', year, yyyyMm));

    if (!await dir.exists()) {
      await dir.create(recursive: true);
    }

    return dir;
  }

  String safeSlug(String input, {int maxLen = 40}) {
    var s = input.trim();

    // bỏ ký tự cấm trong tên file
    s = s.replaceAll(RegExp(r'[<>:"/\\|?*]'), '_');

    // gộp khoảng trắng
    s = s.replaceAll(RegExp(r'\s+'), '_');

    // cắt độ dài cho gọn khi share
    if (s.length > maxLen) s = s.substring(0, maxLen);

    // tránh rỗng
    if (s.isEmpty) s = 'NA';
    return s;
  }

  /// Windows-safe filename: tránh ký tự : * ? " < > | / \
  String safeFileName(String name) {
    return name.replaceAll(RegExp(r'[<>:"/\\|?*]'), '_').trim();
  }

  //
  String sanitizeFileName(String input) {
    // Windows + Android safe
    final illegal = RegExp(r'[\\/:*?"<>|]');
    final cleaned = input.replaceAll(illegal, '_').trim();
    return cleaned.isEmpty ? 'unknown' : cleaned;
  }
}
