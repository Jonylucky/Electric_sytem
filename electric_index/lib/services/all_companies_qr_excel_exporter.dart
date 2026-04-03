import 'dart:io';
import 'dart:typed_data';

import 'package:path/path.dart' as p;
import 'package:path_provider/path_provider.dart';
import 'package:syncfusion_flutter_xlsio/xlsio.dart' as xlsio;

import '../db/dao/company_dao.dart';
import '../db/dao/meter_dao.dart';
import 'file_storage_service.dart';
import '../utils/qr_utils.dart';

class AllCompaniesQrExcelExporter {
  final CompanyDao _companyDao;
  final MeterDao _meterDao;
  final _fileStorgate = FileStorageService();

  AllCompaniesQrExcelExporter({
    required CompanyDao companyDao,
    required MeterDao meterDao,
  }) : _companyDao = companyDao,
       _meterDao = meterDao;

  /// Export 1 file Excel chứa tất cả company + meter + QR
  Future<String> exportAllCompaniesQrExcel() async {
    final companies = await _companyDao.getAllCompanies();

    final workbook = xlsio.Workbook();
    final sheet = workbook.worksheets[0];
    sheet.name = 'All QR';

    // Header
    sheet.getRangeByIndex(1, 1).setText('Company ID');
    sheet.getRangeByIndex(1, 2).setText('Company Name');
    sheet.getRangeByIndex(1, 3).setText('Meter ID');
    sheet.getRangeByIndex(1, 4).setText('Meter Name');
    sheet.getRangeByIndex(1, 5).setText('QR');

    final header = sheet.getRangeByIndex(1, 1, 1, 5);
    header.cellStyle.bold = true;
    sheet.setRowHeightInPixels(1, 26);

    sheet.setColumnWidthInPixels(1, 110);
    sheet.setColumnWidthInPixels(2, 220);
    sheet.setColumnWidthInPixels(3, 160);
    sheet.setColumnWidthInPixels(4, 220);
    sheet.setColumnWidthInPixels(5, 180);

    int row = 2;

    for (final c in companies) {
      // ⚠️ đổi key nếu DB m khác: company_id / name
      final companyId = (c['company_id'] as int);
      print(' company_Id :${companyId}');
      final companyName = (c['company_name'] ?? '').toString();

      final meters = await _meterDao.getMetersByCompany(companyId);

      for (final m in meters) {
        // ⚠️ đổi key nếu DB m khác: meter_id / name
        final meterId = (m['meter_id'] ?? '').toString().trim();
        final meterName = (m['meter_name'] ?? '').toString();

        final qrData = buildQrData(
          companyId: companyId.toString(),
          meterId: meterId,
        );

        final Uint8List png = await qrPngBytes(qrData, size: 220);

        sheet.getRangeByIndex(row, 1).setText(companyId.toString());
        sheet.getRangeByIndex(row, 2).setText(companyName);
        sheet.getRangeByIndex(row, 3).setText(meterId);
        sheet.getRangeByIndex(row, 4).setText(meterName);

        sheet.setRowHeightInPixels(row, 170);

        final pic = sheet.pictures.addStream(row, 5, png);
        pic.height = 160;
        pic.width = 160;

        row++;
      }
    }

    final bytes = workbook.saveAsStream();
    workbook.dispose();

    final dir = await getApplicationDocumentsDirectory();
    final fileName =
        'QR_ALL_COMPANIES_${DateTime.now().millisecondsSinceEpoch}.xlsx';
    final filePath = p.join(dir.path, _fileStorgate.sanitizeFileName(fileName));

    await File(filePath).writeAsBytes(bytes, flush: true);
    return filePath;
  }
}
