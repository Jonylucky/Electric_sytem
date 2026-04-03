import 'dart:io';
import 'package:path/path.dart' as p;
import 'dart:typed_data';
import 'package:syncfusion_flutter_xlsio/xlsio.dart' as xlsio;
import 'package:electric_index/db/dao/database_helper.dart';
import 'file_storage_service.dart';
import 'zip_service.dart';

class ExcelExportService {
  final _storage = FileStorageService();
  final zipService = ZipService();

  double _toDouble(dynamic v) {
    if (v == null) return 0;
    if (v is num) return v.toDouble();
    return double.tryParse(v.toString()) ?? 0;
  }

  String _toStr(dynamic v) => (v ?? '').toString();
  // load img

  /// Export 1 file Excel tổng cho 1 company trong 1 kỳ YYYY-MM
  /// Trả về File đã tạo

  // 4️⃣ Layout
  // Freeze header để scroll vẫn thấy (khỏi dùng freezePanes lỗi)

  Future<File> exportCompanyMonth({
    required int companyId,
    required String? conpanyName,
    required String yyyyMm,
  }) async {
    final db = await DatabaseHelper.instance.database;

    final rows = await db.rawQuery(
      '''
    SELECT
      c.company_id,
      c.company_name,
      m.meter_id,
      m.meter_name,
      l.floor,
      l.location_name,
      r.month,
      r.index_prev_month,
      r.index_last_month,
      r.index_consumption,
      r.image_reading,
      r.created_at
    FROM readings r
    JOIN meters m ON m.meter_id = r.meter_id
    JOIN companies c ON c.company_id = m.company_id
    LEFT JOIN locations l ON l.location_id = m.location_id
    WHERE c.company_id = ? AND r.month = ?
    ORDER BY m.meter_id ASC;
    ''',
      [companyId, yyyyMm],
    );

    // 1) Thư mục base export như mày đang dùng
    final outDir = await _storage.companyExportsDir(
      companyId,
      yyyyMm,
      conpanyName,
    );

    // 2) Tạo package folder chứa Excel + IMAGES
    final safeCompany = _storage.safeFileName(
      (conpanyName?.trim().isNotEmpty == true)
          ? conpanyName!.trim()
          : 'Company_$companyId',
    );

    final pkgFolderName = _storage.safeFileName(
      'EI__${safeCompany}__${yyyyMm}__PKG__${DateTime.now().millisecondsSinceEpoch}',
    );

    final exportRoot = Directory(p.join(outDir.path, pkgFolderName));
    await exportRoot.create(recursive: true);

    // 3) Excel nằm trong package folder (cùng cấp với IMAGES/)
    final excelName = _storage.safeFileName(
      'EI__${safeCompany}__${yyyyMm}__READINGS.xlsx',
    );
    final excelPath = p.join(exportRoot.path, excelName);

    // 4) Write Excel: cột ảnh dùng hyperlink + copy ảnh vào exportRoot/IMAGES
    await _writeXlsioReadings(
      filePath: excelPath,
      sheetName: 'Readings',
      yyyyMm: yyyyMm,
      rows: rows,
    );

    // 5) Zip package folder -> 1 file .zip để share
    final zipFile = await zipService.zipExportFolder(
      folder: exportRoot,
      zipName: '$pkgFolderName.zip',
    );

    return zipFile;
  }

  static const int _allExportBatchSize = 8;

  /// ✅ all_company export ZIP (Excel + IMAGES) — batch 8 item + yield để giảm RAM (60+ ảnh)
  Future<File> exportAllCompaniesMonth({required String yyyyMm}) async {
    final db = await DatabaseHelper.instance.database;

    // 1) Base dir
    final baseDir = await _storage.companyExportsDir(0, yyyyMm, 'ALL');

    // 2) Tạo folder export (Excel + IMAGES)
    final exportFolderName = _storage.safeFileName(
      'EI__ALL__${yyyyMm}__PKG__${DateTime.now().millisecondsSinceEpoch}',
    );
    final exportRoot = Directory(p.join(baseDir.path, exportFolderName));
    await exportRoot.create(recursive: true);

    final excelName = _storage.safeFileName(
      'EI__ALL__${yyyyMm}__READINGS.xlsx',
    );
    final excelPath = p.join(exportRoot.path, excelName);

    // 3) Ghi Excel + copy ảnh theo batch 8 dòng (giảm RAM khi 60+ ảnh)
    await _writeXlsioReadingsAllCompaniesBatched(
      db: db,
      filePath: excelPath,
      sheetName: 'Readings_All',
      yyyyMm: yyyyMm,
      batchSize: _allExportBatchSize,
    );

    // 4) Zip folder
    final zipFile = await zipService.zipExportFolder(
      folder: exportRoot,
      zipName: '${p.basename(exportRoot.path)}.zip',
    );

    return zipFile;
  }

  static const String _allCompaniesSelectSql = '''
    SELECT
      c.company_id,
      c.company_name,
      m.meter_id,
      m.meter_name,
      l.floor,
      l.location_name,
      r.month,
      r.index_prev_month,
      r.index_last_month,
      r.index_consumption,
  
      r.created_at
    FROM readings r
    JOIN meters m ON m.meter_id = r.meter_id
    JOIN companies c ON c.company_id = m.company_id
    LEFT JOIN locations l ON l.location_id = m.location_id
    WHERE r.month = ?
    ORDER BY c.company_name ASC, m.meter_id ASC
    LIMIT ? OFFSET ?
  ''';

  /// Ghi Excel "all companies" theo batch: mỗi lần query 20 dòng, ghi + copy ảnh, rồi batch tiếp (giảm RAM).
  Future<File> _writeXlsioReadingsAllCompaniesBatched({
    required dynamic db,
    required String filePath,
    required String sheetName,
    required String yyyyMm,
    int batchSize = 20,
  }) async {
    const int totalCols = 10;
    const int imageCol = 9;
    const int headerRow = 5;

    final workbook = xlsio.Workbook();
    final sheet = workbook.worksheets[0];
    sheet.name = sheetName;
    sheet.setColumnWidthInPixels(imageCol, 160);

    // Title + header (giống _writeXlsioReadings)
    sheet.getRangeByIndex(1, 1, 1, totalCols).merge();
    sheet.getRangeByIndex(1, 1).setText('CỘNG HÒA XÃ HỘI CHỦ NGHĨA VIỆT NAM');
    final nationalStyle = sheet.getRangeByIndex(1, 1).cellStyle;
    nationalStyle.bold = true;
    nationalStyle.fontSize = 14;
    nationalStyle.hAlign = xlsio.HAlignType.center;
    nationalStyle.vAlign = xlsio.VAlignType.center;

    sheet.getRangeByIndex(2, 1, 2, totalCols).merge();
    sheet.getRangeByIndex(2, 1).setText('Độc lập - Tự do - Hạnh phúc');
    sheet.getRangeByIndex(2, 1).cellStyle.bold = true;
    sheet.getRangeByIndex(2, 1).cellStyle.fontSize = 12;
    sheet.getRangeByIndex(2, 1).cellStyle.hAlign = xlsio.HAlignType.center;
    sheet.getRangeByIndex(2, 1).cellStyle.vAlign = xlsio.VAlignType.center;

    sheet.getRangeByIndex(3, 1, 3, totalCols).merge();
    sheet.getRangeByIndex(3, 1).setText('BẢNG CHỈ SỐ ĐIỆN');
    sheet.getRangeByIndex(3, 1).cellStyle.bold = true;
    sheet.getRangeByIndex(3, 1).cellStyle.fontSize = 18;
    sheet.getRangeByIndex(3, 1).cellStyle.hAlign = xlsio.HAlignType.center;
    sheet.getRangeByIndex(3, 1).cellStyle.vAlign = xlsio.VAlignType.center;

    sheet.getRangeByIndex(4, 1, 4, totalCols).merge();
    sheet.getRangeByIndex(4, 1).setText('Chỉ số điện tháng $yyyyMm');
    sheet.getRangeByIndex(4, 1).cellStyle.fontSize = 12;
    sheet.getRangeByIndex(4, 1).cellStyle.hAlign = xlsio.HAlignType.center;
    sheet.getRangeByIndex(4, 1).cellStyle.vAlign = xlsio.VAlignType.center;

    final headers = [
      'Tên công ty',
      'Tên đồng hồ',
      'Tầng',
      'Vị trí',
      'Tháng',
      'Chỉ số tháng trước',
      'Chỉ số tháng này',
      'Sản lượng tiêu thụ',
      'Hình ảnh chỉ số',
      'Thời gian ghi',
    ];
    final headerStyle = workbook.styles.add('headerStyle_all');
    headerStyle.bold = true;
    headerStyle.fontSize = 11;
    headerStyle.hAlign = xlsio.HAlignType.center;
    headerStyle.vAlign = xlsio.VAlignType.center;
    headerStyle.borders.all.lineStyle = xlsio.LineStyle.thin;
    headerStyle.wrapText = true;
    final dataStyle = workbook.styles.add('dataStyle_all');
    dataStyle.fontSize = 10;
    dataStyle.vAlign = xlsio.VAlignType.center;
    dataStyle.borders.all.lineStyle = xlsio.LineStyle.thin;
    dataStyle.wrapText = true;

    sheet.getRangeByIndex(1, 1).rowHeight = 18;
    sheet.getRangeByIndex(2, 1).rowHeight = 16;
    sheet.getRangeByIndex(3, 1).rowHeight = 22;
    sheet.getRangeByIndex(4, 1).rowHeight = 16;
    sheet.getRangeByIndex(headerRow, 1).rowHeight = 28;

    for (int i = 0; i < headers.length; i++) {
      final cell = sheet.getRangeByIndex(headerRow, i + 1);
      cell.setText(headers[i]);
      cell.cellStyle = headerStyle;
      final len = headers[i].length;
      final width = (len + 6).clamp(12, 35).toDouble();
      sheet.setColumnWidthInPixels(i + 1, (width * 7.0).toInt());
    }
    final headerCell = sheet.getRangeByIndex(headerRow, imageCol);
    headerCell.cellStyle.wrapText = true;
    headerCell.cellStyle.hAlign = xlsio.HAlignType.center;
    headerCell.cellStyle.vAlign = xlsio.VAlignType.center;
    sheet.setRowHeightInPixels(headerRow, 36);

    int rowIndex = headerRow + 1;
    int offset = 0;

    while (true) {
      final rows = await db.rawQuery(_allCompaniesSelectSql, [
        yyyyMm,
        batchSize,
        offset,
      ]);
      if (rows.isEmpty) break;

      for (final r in rows) {
        for (int c = 1; c <= totalCols; c++) {
          sheet.getRangeByIndex(rowIndex, c).cellStyle = dataStyle;
        }
        for (final c in [6, 7, 8]) {
          sheet.getRangeByIndex(rowIndex, c).cellStyle.hAlign =
              xlsio.HAlignType.center;
        }

        sheet.getRangeByIndex(rowIndex, 1).setText(_toStr(r['company_name']));
        sheet.getRangeByIndex(rowIndex, 2).setText(_toStr(r['meter_name']));
        sheet.getRangeByIndex(rowIndex, 3).setText(_toStr(r['floor']));
        sheet.getRangeByIndex(rowIndex, 4).setText(_toStr(r['location_name']));
        sheet.getRangeByIndex(rowIndex, 5).setText(_toStr(r['month']));
        sheet
            .getRangeByIndex(rowIndex, 6)
            .setNumber(_toDouble(r['index_prev_month']));
        sheet
            .getRangeByIndex(rowIndex, 7)
            .setNumber(_toDouble(r['index_last_month']));
        sheet
            .getRangeByIndex(rowIndex, 8)
            .setNumber(_toDouble(r['index_consumption']));

        final createdRaw = _toStr(r['created_at']).trim();
        if (createdRaw.isNotEmpty) {
          try {
            final utc = DateTime.parse(
              createdRaw.endsWith('Z')
                  ? createdRaw
                  : '${createdRaw.replaceFirst(' ', 'T')}Z',
            );
            final vnTime = utc.add(const Duration(hours: 7));
            final formatted =
                '${vnTime.day.toString().padLeft(2, '0')}/'
                '${vnTime.month.toString().padLeft(2, '0')}/${vnTime.year} '
                '${vnTime.hour.toString().padLeft(2, '0')}:${vnTime.minute.toString().padLeft(2, '0')}';
            sheet.getRangeByIndex(rowIndex, 10).setText(formatted);
          } catch (_) {
            sheet.getRangeByIndex(rowIndex, 10).setText(createdRaw);
          }
        }

        sheet.setRowHeightInPixels(rowIndex, 18);
        final imagePath = _toStr(r['image_reading']).trim();
        final imgCell = sheet.getRangeByIndex(rowIndex, imageCol);
        imgCell.setText('');

        if (imagePath.isNotEmpty) {
          final relPath = await _copyImageForExport(
            originalPath: imagePath,
            excelFilePath: filePath,
            yyyyMm: yyyyMm,
            rowIndex: rowIndex,
            meterName: _toStr(r['meter_name']),
          );
          if (relPath != null) {
            imgCell.setText('Xem ảnh');
            _styleAsHyperlink(imgCell);
            _addFileHyperlink(
              sheet: sheet,
              range: imgCell,
              fileRelativePath: relPath,
              displayText: 'Xem ảnh',
            );
          }
        }
        rowIndex++;
      }

      offset += batchSize;
      if (rows.length < batchSize) break;
      await Future.delayed(Duration.zero);
    }

    final bytes = Uint8List.fromList(workbook.saveAsStream());
    workbook.dispose();

    final file = File(filePath);
    await file.writeAsBytes(bytes, flush: true);
    return file;
  }

  Future<String?> _copyImageForExport({
    required String originalPath,
    required String excelFilePath,
    required String yyyyMm,
    required int rowIndex,
    required String meterName,
  }) async {
    try {
      final src = File(originalPath);
      if (!await src.exists()) return null;

      final excelDir = Directory(p.dirname(excelFilePath));
      final imagesDir = Directory(p.join(excelDir.path, 'IMAGES'));
      if (!await imagesDir.exists()) {
        await imagesDir.create(recursive: true);
      }

      final ext = p.extension(originalPath).toLowerCase();
      final safeMeter = meterName
          .replaceAll(RegExp(r'[^\w\-]+'), '_')
          .replaceAll(RegExp(r'_+'), '_')
          .trim();

      final fileName =
          'IMG_${yyyyMm}_R${rowIndex}_${safeMeter.isEmpty ? "METER" : safeMeter}$ext';

      final destAbsPath = p.join(imagesDir.path, fileName);
      await _streamCopyFile(src, File(destAbsPath));

      return p.join('IMAGES', fileName);
    } catch (_) {
      return null;
    }
  }

  /// Copy file bằng stream (chunk ~64KB), không load cả file vào RAM.
  Future<void> _streamCopyFile(File source, File dest) async {
    final sink = dest.openWrite();
    try {
      await sink.addStream(source.openRead(0, null));
    } finally {
      await sink.close();
    }
  }

  void _styleAsHyperlink(xlsio.Range cell) {
    cell.cellStyle.underline = true;
    cell.cellStyle.hAlign = xlsio.HAlignType.center;
    cell.cellStyle.vAlign = xlsio.VAlignType.center;
  }

  void _addFileHyperlink({
    required xlsio.Worksheet sheet,
    required xlsio.Range range,
    required String fileRelativePath,
    required String displayText,
  }) {
    sheet.hyperlinks.add(
      range,
      xlsio.HyperlinkType.file,
      fileRelativePath,
      displayText,
    );
  }

  /// 🔒 viết Excel bằng Syncfusion (format + ảnh giống exportCompanyMonth)
  Future<File> _writeXlsioReadings({
    required String filePath,
    required String sheetName,
    required String yyyyMm,
    required List<Map<String, dynamic>> rows,
  }) async {
    final workbook = xlsio.Workbook();
    final sheet = workbook.worksheets[0];
    sheet.name = sheetName;

    const int totalCols = 10;
    const int imageCol = 9;
    const int headerRow = 5;

    // set cột ảnh trước
    sheet.setColumnWidthInPixels(imageCol, 160);

    // ===== NATIONAL TITLE (Row 1) =====
    sheet.getRangeByIndex(1, 1, 1, totalCols).merge();
    sheet.getRangeByIndex(1, 1).setText('CỘNG HÒA XÃ HỘI CHỦ NGHĨA VIỆT NAM');
    final nationalStyle = sheet.getRangeByIndex(1, 1).cellStyle;
    nationalStyle.bold = true;
    nationalStyle.fontSize = 14;
    nationalStyle.hAlign = xlsio.HAlignType.center;
    nationalStyle.vAlign = xlsio.VAlignType.center;

    // Row 2
    sheet.getRangeByIndex(2, 1, 2, totalCols).merge();
    sheet.getRangeByIndex(2, 1).setText('Độc lập - Tự do - Hạnh phúc');
    final mottoStyle = sheet.getRangeByIndex(2, 1).cellStyle;
    mottoStyle.bold = true;
    mottoStyle.fontSize = 12;
    mottoStyle.hAlign = xlsio.HAlignType.center;
    mottoStyle.vAlign = xlsio.VAlignType.center;

    // ===== TITLE (Row 3) =====
    sheet.getRangeByIndex(3, 1, 3, totalCols).merge();
    sheet.getRangeByIndex(3, 1).setText('BẢNG CHỈ SỐ ĐIỆN');
    final titleStyle = sheet.getRangeByIndex(3, 1).cellStyle;
    titleStyle.bold = true;
    titleStyle.fontSize = 18;
    titleStyle.hAlign = xlsio.HAlignType.center;
    titleStyle.vAlign = xlsio.VAlignType.center;

    // ===== SUB TITLE (Row 4) =====
    sheet.getRangeByIndex(4, 1, 4, totalCols).merge();
    sheet.getRangeByIndex(4, 1).setText('Chỉ số điện tháng $yyyyMm');
    final subTitleStyle = sheet.getRangeByIndex(4, 1).cellStyle;
    subTitleStyle.fontSize = 12;
    subTitleStyle.hAlign = xlsio.HAlignType.center;
    subTitleStyle.vAlign = xlsio.VAlignType.center;

    // ===== Header =====
    final headers = [
      'Tên công ty',
      'Tên đồng hồ',
      'Tầng',
      'Vị trí',
      'Tháng',
      'Chỉ số tháng trước',
      'Chỉ số tháng này',
      'Sản lượng tiêu thụ',
      'Hình ảnh chỉ số',
      'Thời gian ghi',
    ];

    // style
    final headerStyle = workbook.styles.add('headerStyle_all');
    headerStyle.bold = true;
    headerStyle.fontSize = 11;
    headerStyle.hAlign = xlsio.HAlignType.center;
    headerStyle.vAlign = xlsio.VAlignType.center;
    headerStyle.borders.all.lineStyle = xlsio.LineStyle.thin;
    headerStyle.wrapText = true;

    final dataStyle = workbook.styles.add('dataStyle_all');
    dataStyle.fontSize = 10;
    dataStyle.vAlign = xlsio.VAlignType.center;
    dataStyle.borders.all.lineStyle = xlsio.LineStyle.thin;
    dataStyle.wrapText = true;

    // heights
    sheet.getRangeByIndex(1, 1).rowHeight = 18;
    sheet.getRangeByIndex(2, 1).rowHeight = 16;
    sheet.getRangeByIndex(3, 1).rowHeight = 22;
    sheet.getRangeByIndex(4, 1).rowHeight = 16;
    sheet.getRangeByIndex(headerRow, 1).rowHeight = 28;

    // set header + auto width theo header
    for (int i = 0; i < headers.length; i++) {
      final cell = sheet.getRangeByIndex(headerRow, i + 1);
      cell.setText(headers[i]);
      cell.cellStyle = headerStyle;

      final len = headers[i].length;
      final width = (len + 6).clamp(12, 35).toDouble();
      sheet.setColumnWidthInPixels(i + 1, (width * 7.0).toInt());
    }

    // đảm bảo header cột ảnh căn giữa + không đè chữ
    final headerCell = sheet.getRangeByIndex(headerRow, imageCol);
    headerCell.cellStyle.wrapText = true;
    headerCell.cellStyle.hAlign = xlsio.HAlignType.center;
    headerCell.cellStyle.vAlign = xlsio.VAlignType.center;
    sheet.setRowHeightInPixels(headerRow, 36);

    // ===== Data rows =====
    int rowIndex = headerRow + 1;

    for (final r in rows) {
      // apply style cho cả hàng
      for (int c = 1; c <= totalCols; c++) {
        sheet.getRangeByIndex(rowIndex, c).cellStyle = dataStyle;
      }

      // căn giữa các cột số
      for (final c in [6, 7, 8]) {
        sheet.getRangeByIndex(rowIndex, c).cellStyle.hAlign =
            xlsio.HAlignType.center;
      }

      // fill data
      sheet.getRangeByIndex(rowIndex, 1).setText(_toStr(r['company_name']));
      sheet.getRangeByIndex(rowIndex, 2).setText(_toStr(r['meter_name']));
      sheet.getRangeByIndex(rowIndex, 3).setText(_toStr(r['floor']));
      sheet.getRangeByIndex(rowIndex, 4).setText(_toStr(r['location_name']));
      sheet.getRangeByIndex(rowIndex, 5).setText(_toStr(r['month']));

      sheet
          .getRangeByIndex(rowIndex, 6)
          .setNumber(_toDouble(r['index_prev_month']));
      sheet
          .getRangeByIndex(rowIndex, 7)
          .setNumber(_toDouble(r['index_last_month']));
      sheet
          .getRangeByIndex(rowIndex, 8)
          .setNumber(_toDouble(r['index_consumption']));

      final createdRaw = _toStr(r['created_at']).trim();

      if (createdRaw.isNotEmpty) {
        try {
          // Nếu DB lưu UTC thì thêm 'Z'
          final utc = DateTime.parse(
            createdRaw.endsWith('Z')
                ? createdRaw
                : '${createdRaw.replaceFirst(' ', 'T')}Z',
          );

          final vnTime = utc.add(const Duration(hours: 7));

          final formatted =
              '${vnTime.day.toString().padLeft(2, '0')}/'
              '${vnTime.month.toString().padLeft(2, '0')}/'
              '${vnTime.year} '
              '${vnTime.hour.toString().padLeft(2, '0')}:'
              '${vnTime.minute.toString().padLeft(2, '0')}';

          sheet.getRangeByIndex(rowIndex, 10).setText(formatted);
        } catch (_) {
          sheet.getRangeByIndex(rowIndex, 10).setText(createdRaw);
        }
      }

      // default height
      sheet.setRowHeightInPixels(rowIndex, 18);

      // image
      // ===== IMAGE -> hyperlink only (no memory heavy picture insert) =====
      final imagePath = _toStr(r['image_reading']).trim();
      final imgCell = sheet.getRangeByIndex(rowIndex, imageCol);
      imgCell.setText('');

      // row height bình thường
      sheet.setRowHeightInPixels(rowIndex, 18);

      if (imagePath.isNotEmpty) {
        final relPath = await _copyImageForExport(
          originalPath: imagePath,
          excelFilePath: filePath,
          yyyyMm: yyyyMm,
          rowIndex: rowIndex,
          meterName: _toStr(r['meter_name']),
        );

        if (relPath != null) {
          imgCell.setText('Xem ảnh');
          _styleAsHyperlink(imgCell);

          _addFileHyperlink(
            sheet: sheet,
            range: imgCell,
            fileRelativePath: relPath,
            displayText: 'Xem ảnh',
          );
        }
      }

      rowIndex++;
    }

    final bytes = Uint8List.fromList(workbook.saveAsStream());
    workbook.dispose();

    final file = File(filePath);
    await file.writeAsBytes(bytes, flush: true);
    return file;
  }
}
