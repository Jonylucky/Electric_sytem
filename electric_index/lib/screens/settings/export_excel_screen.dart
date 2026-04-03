import 'package:flutter/material.dart';
import 'package:share_plus/share_plus.dart';

import '../../db/dao/company_dao.dart';
import '../../services/excel_export_service.dart';

class ExportExcelScreen extends StatefulWidget {
  const ExportExcelScreen({super.key});

  @override
  State<ExportExcelScreen> createState() => _ExportExcelScreenState();
}

class _ExportExcelScreenState extends State<ExportExcelScreen> {
  final _companyDao = CompanyDao();
  final _exporter = ExcelExportService();

  List<Map<String, dynamic>> _companies = [];
  int? _companyId;
  //companyName
  String? _companyName;

  String _month = _yyyyMmNow();
  bool _loading = true;
  bool _exporting = false;

  static String _yyyyMmNow() {
    final now = DateTime.now();
    final mm = now.month.toString().padLeft(2, '0');
    return '${now.year}-$mm';
  }

  @override
  void initState() {
    super.initState();
    _init();
  }

  Future<void> _init() async {
    final companies = await _companyDao.getAllCompanies();
    setState(() {
      _companies = companies;
      _companyId = companies.isNotEmpty
          ? companies.first['company_id'] as int
          : null;

      _companyName = companies.isNotEmpty
          ? companies.first['company_name'] as String
          : null;
      _loading = false;
    });
  }

  Future<void> _export() async {
    if (_companyId == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Chưa có company để export')),
      );
      return;
    }

    setState(() => _exporting = true);
    try {
      final file = await _exporter.exportCompanyMonth(
        companyId: _companyId!,
        conpanyName: _companyName,
        yyyyMm: _month,
      );

      if (!mounted) return;
      ScaffoldMessenger.of(
        context,
      ).showSnackBar(SnackBar(content: Text('✅ Exported: ${file.path}')));

      await Share.shareXFiles([
        XFile(file.path),
      ], text: 'Electric Index export $_month');
    } catch (e) {
      if (!mounted) return;
      ScaffoldMessenger.of(
        context,
      ).showSnackBar(SnackBar(content: Text('❌ Export failed: $e')));
    } finally {
      if (mounted) setState(() => _exporting = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    if (_loading) {
      return const Scaffold(body: Center(child: CircularProgressIndicator()));
    }

    return Scaffold(
      appBar: AppBar(title: const Text('Export Excel')),
      body: Padding(
        padding: const EdgeInsets.all(12),
        child: Column(
          children: [
            DropdownButtonFormField<int>(
              value: _companyId,
              items: _companies
                  .map(
                    (c) => DropdownMenuItem<int>(
                      value: c['company_id'] as int,
                      child: Text(c['company_name'] as String),
                    ),
                  )
                  .toList(),
              onChanged: (v) => setState(() => _companyId = v),
              decoration: const InputDecoration(
                labelText: 'Company',
                prefixIcon: Icon(Icons.apartment),
              ),
            ),
            const SizedBox(height: 12),

            TextField(
              controller: TextEditingController(text: _month),
              readOnly: true,
              decoration: const InputDecoration(
                labelText: 'Month (YYYY-MM)',
                prefixIcon: Icon(Icons.calendar_month),
              ),
              onTap: () async {
                // đơn giản: chọn bằng dialog nhập tay
                final ctrl = TextEditingController(text: _month);
                final res = await showDialog<String>(
                  context: context,
                  builder: (_) => AlertDialog(
                    title: const Text('Set month (YYYY-MM)'),
                    content: TextField(controller: ctrl),
                    actions: [
                      TextButton(
                        onPressed: () => Navigator.pop(context),
                        child: const Text('Cancel'),
                      ),
                      FilledButton(
                        onPressed: () =>
                            Navigator.pop(context, ctrl.text.trim()),
                        child: const Text('OK'),
                      ),
                    ],
                  ),
                );
                if (res != null && RegExp(r'^\d{4}-\d{2}$').hasMatch(res)) {
                  setState(() => _month = res);
                }
              },
            ),

            const Spacer(),
            SizedBox(
              width: double.infinity,
              child: FilledButton.icon(
                onPressed: _exporting ? null : _export,
                icon: const Icon(Icons.file_download),
                label: Text(_exporting ? 'Exporting...' : 'Export & Share'),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
