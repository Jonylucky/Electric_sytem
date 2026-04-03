import 'package:flutter/material.dart';
import 'package:qr_flutter/qr_flutter.dart';
import 'package:share_plus/share_plus.dart';

import '../../db/dao/meter_dao.dart';
import '../../utils/qr_utils.dart';
import '../../services/qr_excel_exporter.dart';

class MeterQrListPage extends StatefulWidget {
  final int companyId;
  final String companyName;
  final MeterDao meterDao;

  const MeterQrListPage({
    super.key,
    required this.companyId,
    required this.companyName,
    required this.meterDao,
  });

  @override
  State<MeterQrListPage> createState() => _MeterQrListPageState();
}

class _MeterQrListPageState extends State<MeterQrListPage> {
  bool _exporting = false;

  Future<void> _exportAndShareExcel() async {
    if (_exporting) return;
    setState(() => _exporting = true);

    try {
      final exporter = QrExcelExporter(meterDao: widget.meterDao);

      final filePath = await exporter.exportMetersQrExcel(
        companyId: widget.companyId,
        companyName: widget.companyName,
      );

      if (!mounted) return;

      await Share.shareXFiles(
        [XFile(filePath)],
        text: 'QR meters – ${widget.companyName}',
        subject: 'QR Excel – ${widget.companyName}',
      );
    } catch (e) {
      if (!mounted) return;
      ScaffoldMessenger.of(
        context,
      ).showSnackBar(SnackBar(content: Text('Export/Share lỗi: $e')));
    } finally {
      if (mounted) setState(() => _exporting = false);
    }
  }

  /// Lấy meterId (TEXT PRIMARY KEY) từ row Map, tránh crash nếu key lệch
  String _readMeterId(Map<String, dynamic> row) {
    // ⚠️ Đảm bảo key KHÔNG có khoảng trắng: 'meter_id' (không phải 'meter_id ')
    final v = row['meter_id'] ?? row['id'] ?? row['meterId'];
    return (v ?? '').toString().trim();
  }

  /// Lấy meterName từ row Map, tránh crash nếu key lệch
  String _readMeterName(Map<String, dynamic> row) {
    final v = row['meter_name'] ?? row['name'] ?? row['meterName'];
    return (v ?? '').toString().trim();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('QR - ${widget.companyName}'),
        actions: [
          IconButton(
            tooltip: 'Xuất & Share Excel',
            onPressed: _exporting ? null : _exportAndShareExcel,
            icon: _exporting
                ? const SizedBox(
                    width: 18,
                    height: 18,
                    child: CircularProgressIndicator(strokeWidth: 2),
                  )
                : const Icon(Icons.share),
          ),
        ],
      ),
      body: FutureBuilder<List<Map<String, dynamic>>>(
        future: widget.meterDao.getMetersByCompany(widget.companyId),
        builder: (context, snap) {
          if (snap.connectionState != ConnectionState.done) {
            return const Center(child: CircularProgressIndicator());
          }
          if (snap.hasError) {
            return Center(child: Text('Error: ${snap.error}'));
          }

          final meters = snap.data ?? const <Map<String, dynamic>>[];
          if (meters.isEmpty) {
            return const Center(child: Text('Không có meter nào'));
          }

          return ListView.separated(
            itemCount: meters.length,
            separatorBuilder: (_, __) => const Divider(height: 1),
            itemBuilder: (context, i) {
              final row = meters[i];

              final meterId = _readMeterId(row);
              final meterName = _readMeterName(row);

              // QR encode: companyId có thể int -> toString()
              final qrData = buildQrData(
                companyId: widget.companyId.toString(),
                meterId: meterId,
              );

              return ListTile(
                title: Text(meterName.isEmpty ? '(No name)' : meterName),
                subtitle: Text(qrData),
                trailing: SizedBox(
                  width: 74,
                  height: 74,
                  child: QrImageView(
                    data: qrData,
                    version: QrVersions.auto,
                    gapless: true,
                  ),
                ),
              );
            },
          );
        },
      ),
    );
  }
}
