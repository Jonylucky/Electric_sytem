import 'dart:io';
import 'dart:typed_data';

import 'package:path/path.dart' as p;
import 'package:path_provider/path_provider.dart';
import 'package:syncfusion_flutter_xlsio/xlsio.dart' as xlsio;
import '../db/dao/meter_dao.dart';
import '../utils/qr_utils.dart';

class QrExcelExporter {
  final MeterDao _meterDao;

  QrExcelExporter({required MeterDao meterDao}) : _meterDao = meterDao;

  String sanitizeFileName(String input) {
    final illegal = RegExp(r'[\\/:*?"<>|]');
    final cleaned = input.replaceAll(illegal, '_').trim();
    return cleaned.isEmpty ? 'unknown' : cleaned;
  }

  Future<String> exportMetersQrExcel({
    required int companyId,
    required String companyName,
    String? highlightMeterId, // ✅ NEW
  }) async {
    final meters = await _meterDao.getMetersByCompany(companyId);

    final workbook = xlsio.Workbook();
    final sheet = workbook.worksheets[0];
    sheet.name = 'Meters QR';

    // Header
    // Header
    sheet.getRangeByIndex(1, 1).setText('TAG'); // ✅ NEW
    sheet.getRangeByIndex(1, 2).setText('Company');
    sheet.getRangeByIndex(1, 3).setText('Meter');
    sheet.getRangeByIndex(1, 4).setText('QR');

    final header = sheet.getRangeByIndex(1, 1, 1, 4); // ✅ 4 cột
    header.cellStyle.bold = true;

    sheet.setRowHeightInPixels(1, 26);
    sheet.setColumnWidthInPixels(1, 28 * 4); // TAG
    sheet.setColumnWidthInPixels(2, 28 * 6); // Company
    sheet.setColumnWidthInPixels(3, 28 * 7); // Meter
    sheet.setColumnWidthInPixels(4, 180); // QR

    int row = 2;

    for (final m in meters) {
      final meterId = (m['meter_id'] ?? '') as String;
      final meterName = (m['meter_name'] ?? '') as String;

      final isNew =
          (highlightMeterId != null && meterId == highlightMeterId); // ✅ NEW

      final qrData = buildQrData(
        companyId: companyId.toString(),
        meterId: meterId,
      );

      final Uint8List png = await qrPngBytes(qrData, size: 220);

      sheet.getRangeByIndex(row, 1).setText(isNew ? 'NEW' : ''); // ✅ TAG
      sheet.getRangeByIndex(row, 2).setText(companyName);
      sheet.getRangeByIndex(row, 3).setText(meterName);

      sheet.setRowHeightInPixels(row, 170);

      final pic = sheet.pictures.addStream(row, 4, png); // ✅ QR cột 4
      pic.height = 160;
      pic.width = 160;

      // ✅ highlight NEW row (không cần màu cũng nổi)
      if (isNew) {
        final r = sheet.getRangeByIndex(row, 1, row, 4);
        r.cellStyle.bold = true;
        r.cellStyle.fontSize = 12;
        r.cellStyle.borders.all.lineStyle = xlsio.LineStyle.thin;
      }

      row++;
    }

    final bytes = workbook.saveAsStream();
    workbook.dispose();

    final dir = await getApplicationDocumentsDirectory();
    final safeCompany = sanitizeFileName(companyName);

    final fileName =
        'QR_${safeCompany}_${DateTime.now().millisecondsSinceEpoch}.xlsx';
    final filePath = p.join(dir.path, fileName);

    await File(filePath).writeAsBytes(bytes, flush: true);
    return filePath;
  }
}
