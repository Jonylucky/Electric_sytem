import 'dart:io';
import 'package:flutter/material.dart';
import 'package:share_plus/share_plus.dart';
import 'package:path/path.dart' as p;

import '../../db/dao/reading_dao.dart';
import '../../services/excel_export_service.dart';
import '../../db/dao/company_dao.dart';
import '../../services/file_storage_service.dart';

class ReadingsMonthListScreen extends StatefulWidget {
  const ReadingsMonthListScreen({super.key});

  @override
  State<ReadingsMonthListScreen> createState() =>
      _ReadingsMonthListScreenState();
}

class _ReadingsMonthListScreenState extends State<ReadingsMonthListScreen> {
  final _companyDao = CompanyDao();
  final _readingDao = ReadingDao();
  final _storage = FileStorageService();

  List<Map<String, dynamic>> _companies = [];
  int? _companyId;
  String _companyName = '';
  static const int kAllCompaniesId = -1;

  String _month = _yyyyMmNow();

  bool _loading = true;
  bool _exporting = false;
  List<Map<String, dynamic>> _rows = [];

  static String _yyyyMmNow() {
    final n = DateTime.now();
    return '${n.year}-${n.month.toString().padLeft(2, '0')}';
  }

  @override
  void initState() {
    super.initState();
    _init();
  }

  Future<void> _init() async {
    final companies = await _companyDao.getAllCompanies();
    if (!mounted) return;

    _companies = companies;
    if (companies.isNotEmpty) {
      _companyId = companies.first['company_id'] as int;
      _companyName = companies.first['company_name'] as String;
    }

    setState(() => _loading = false);
    await _loadReadings();
  }

  Future<void> _loadReadings() async {
    if (_month.isEmpty) {
      setState(() => _rows = []);
      return;
    }

    setState(() => _loading = true);

    List<Map<String, dynamic>> rows;

    // 🔹 ALL companies
    if (_companyId == kAllCompaniesId) {
      rows = await _readingDao.getAllCompaniesMonthReadings(month: _month);
    }
    // 🔹 1 company cụ thể
    else if (_companyId != null) {
      rows = await _readingDao.getReadingsByCompanyMonth(
        companyId: _companyId!,
        month: _month,
        conpanyName: _companyName,
      );
    } else {
      rows = [];
    }

    if (!mounted) return;
    setState(() {
      _rows = rows;
      _loading = false;
    });
  }

  Future<void> _pickMonth() async {
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
            onPressed: () => Navigator.pop(context, ctrl.text.trim()),
            child: const Text('OK'),
          ),
        ],
      ),
    );
    if (res != null && RegExp(r'^\d{4}-\d{2}$').hasMatch(res)) {
      setState(() => _month = res);
      await _loadReadings();
    }
  }

  Future<void> _export() async {
    if (_month.isEmpty) return;

    setState(() => _exporting = true);
    try {
      final service = ExcelExportService();

      File file;
      if (_companyId == kAllCompaniesId) {
        file = await service.exportAllCompaniesMonth(yyyyMm: _month);
        print('flie : ${file}');
      } else {
        if (_companyId == null) return;
        file = await service.exportCompanyMonth(
          companyId: _companyId!,
          conpanyName: _companyName,
          yyyyMm: _month,
        );
      }

      // ✅ SHARE
      final xFile = XFile(
        file.path,
        name: p.basename(file.path),
        mimeType:
            'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
      );

      await Share.shareXFiles([xFile], text: 'Excel export $_month');
    } finally {
      if (mounted) setState(() => _exporting = false);
    }
  }

  //delete recor reding
  Future<void> _confirmDeleteReading(Map<String, dynamic> r) async {
    final id = r['reading_id'] as int;
    final meterId = r['meter_id'];
    final month = r['month'];
    final last = r['index_last_month'];

    final ok = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('Xoá bản ghi?'),
        content: Text(
          'Meter: $meterId\nMonth: $month\nLast: $last\n\nXoá luôn ảnh kèm theo.',
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx, false),
            child: const Text('Huỷ'),
          ),
          FilledButton(
            onPressed: () => Navigator.pop(ctx, true),
            child: const Text('Xoá'),
          ),
        ],
      ),
    );

    if (ok != true) return;
    if (!mounted) return;

    try {
      await _readingDao.deleteReadingAndImage(id);
      if (!mounted) return;

      await _loadReadings(); // reload list
      if (!mounted) return;

      ScaffoldMessenger.of(
        context,
      ).showSnackBar(const SnackBar(content: Text('✅ Đã xoá bản ghi')));
    } catch (e) {
      if (!mounted) return;
      ScaffoldMessenger.of(
        context,
      ).showSnackBar(SnackBar(content: Text('❌ Xoá lỗi: $e')));
    }
  }

  Future<void> _confirmDeleteExport() async {
    final ok =
        await showDialog<bool>(
          context: context,
          builder: (_) => AlertDialog(
            title: const Text('Xoá file Excel?'),
            content: Text('Xoá toàn bộ file Excel đã xuất cho tháng $_month?'),
            actions: [
              TextButton(
                onPressed: () => Navigator.pop(context, false),
                child: const Text('Huỷ'),
              ),
              ElevatedButton(
                style: ElevatedButton.styleFrom(backgroundColor: Colors.red),
                onPressed: () => Navigator.pop(context, true),
                child: const Text('Xoá'),
              ),
            ],
          ),
        ) ??
        false;

    if (!ok) return;

    await _storage.deleteExportByCompanyMonth(
      companyId: _companyId!,
      yyyyMm: _month,
      companyName: _companyName.isEmpty ? 'unknown_company' : _companyName,
    );

    if (!mounted) return;
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(content: Text('Đã xoá file Excel của tháng này')),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFFE3F2FD), // 🔵 nền xanh nhạt

      appBar: AppBar(
        title: const Text('Readings by Month'),
        backgroundColor: const Color(0xFF1565C0),
        foregroundColor: Colors.white,
        elevation: 0,
        actions: [
          IconButton(
            tooltip: 'Xuất file Excel',
            onPressed: (_exporting || _rows.isEmpty) ? null : _export,
            icon: _exporting
                ? const SizedBox(
                    width: 18,
                    height: 18,
                    child: CircularProgressIndicator(
                      strokeWidth: 2,
                      color: Colors.white,
                    ),
                  )
                : const Icon(Icons.file_download),
          ),
          IconButton(
            tooltip: 'Xoá file Excel tháng này',
            onPressed: (_companyId == null || _rows.isEmpty)
                ? null
                : _confirmDeleteExport,
            icon: const Icon(Icons.delete_outline),
          ),
          const SizedBox(width: 6),
        ],
      ),

      body: GestureDetector(
        onTap: () => FocusScope.of(context).unfocus(),
        behavior: HitTestBehavior.opaque,
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Column(
            children: [
              // Company dropdown bọc Card cho đồng bộ
              Card(
                elevation: 3,
                shadowColor: Colors.blue.withOpacity(0.15),
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(18),
                ),
                child: Padding(
                  padding: const EdgeInsets.all(10),
                  child: DropdownMenu<int>(
                    initialSelection: _companyId ?? kAllCompaniesId,
                    enableFilter: true,
                    enableSearch: true,
                    expandedInsets: const EdgeInsets.all(5),

                    // ✅ style cho ô select
                    inputDecorationTheme: InputDecorationTheme(
                      filled: true,
                      fillColor: Colors.white,
                      isDense: true,
                      contentPadding: const EdgeInsets.symmetric(
                        horizontal: 12,
                        vertical: 14,
                      ),
                      prefixIconColor: const Color(0xFF1565C0),
                      labelStyle: const TextStyle(
                        color: Color(0xFF1565C0),
                        fontWeight: FontWeight.w600,
                      ),
                      enabledBorder: OutlineInputBorder(
                        borderRadius: BorderRadius.circular(14),
                        borderSide: BorderSide(
                          color: Colors.blue.withOpacity(0.25),
                        ),
                      ),
                      focusedBorder: OutlineInputBorder(
                        borderRadius: BorderRadius.circular(14),
                        borderSide: const BorderSide(
                          color: Color(0xFF1565C0),
                          width: 1.6,
                        ),
                      ),
                    ),

                    label: const Text('Company'),
                    leadingIcon: const Icon(
                      Icons.apartment,
                      color: Color(0xFF1565C0),
                    ),

                    dropdownMenuEntries: [
                      const DropdownMenuEntry<int>(
                        value: kAllCompaniesId,
                        label: 'All companies',
                      ),
                      ..._companies.map((c) {
                        final id = c['company_id'] as int;
                        final name = c['company_name'] as String;
                        return DropdownMenuEntry<int>(
                          value: id,
                          label: name,
                          labelWidget: Text(
                            name,
                            softWrap: true,
                            maxLines: null,
                            style: const TextStyle(color: Color(0xFF0D47A1)),
                          ),
                        );
                      }),
                    ],

                    onSelected: (v) async {
                      if (v == null) return;

                      if (v == kAllCompaniesId) {
                        setState(() {
                          _companyId = kAllCompaniesId;
                          _companyName = 'ALL';
                        });
                        await _loadReadings();
                        return;
                      }

                      final c = _companies.firstWhere(
                        (e) => e['company_id'] == v,
                      );
                      setState(() {
                        _companyId = v;
                        _companyName = c['company_name'] as String;
                      });
                      await _loadReadings();
                    },
                  ),
                ),
              ),

              const SizedBox(height: 12),

              // Month picker bọc Card
              Card(
                elevation: 3,
                shadowColor: Colors.blue.withOpacity(0.15),
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(18),
                ),
                child: ListTile(
                  leading: Container(
                    padding: const EdgeInsets.all(10),
                    decoration: BoxDecoration(
                      color: const Color(0xFFBBDEFB),
                      borderRadius: BorderRadius.circular(14),
                    ),
                    child: const Icon(
                      Icons.calendar_month,
                      color: Color(0xFF1565C0),
                    ),
                  ),
                  title: Text(
                    'Month: $_month',
                    style: const TextStyle(
                      fontWeight: FontWeight.w700,
                      color: Color(0xFF0D47A1),
                    ),
                  ),
                  trailing: const Icon(Icons.edit, color: Color(0xFF1565C0)),
                  onTap: _pickMonth,
                ),
              ),

              const SizedBox(height: 12),

              Expanded(
                child: _loading
                    ? const Center(child: CircularProgressIndicator())
                    : _rows.isEmpty
                    ? const Center(
                        child: Text(
                          'Chưa có readings tháng này',
                          style: TextStyle(color: Colors.black54),
                        ),
                      )
                    : ListView.separated(
                        itemCount: _rows.length,
                        separatorBuilder: (_, __) => const SizedBox(height: 12),
                        itemBuilder: (_, i) {
                          final r = _rows[i];
                          final meterId = (r['meter_id'] ?? '').toString();
                          final meterName = (r['meter_name'] ?? '').toString();
                          final prev = r['index_prev_month'] as int;
                          final last = r['index_last_month'] as int;
                          final con = r['index_consumption'] as int;
                          final img = r['image_reading'] as String?;

                          return Card(
                            elevation: 3,
                            shadowColor: Colors.blue.withOpacity(0.15),
                            shape: RoundedRectangleBorder(
                              borderRadius: BorderRadius.circular(18),
                            ),
                            child: ListTile(
                              contentPadding: const EdgeInsets.symmetric(
                                horizontal: 14,
                                vertical: 10,
                              ),

                              leading: ClipRRect(
                                borderRadius: BorderRadius.circular(12),
                                child: SizedBox(
                                  width: 54,
                                  height: 54,
                                  child:
                                      (img != null &&
                                          img.isNotEmpty &&
                                          File(img).existsSync())
                                      ? Image.file(File(img), fit: BoxFit.cover)
                                      : Container(
                                          color: Colors.white,
                                          alignment: Alignment.center,
                                          child: const Icon(
                                            Icons.photo,
                                            size: 24,
                                            color: Color(0xFF1565C0),
                                          ),
                                        ),
                                ),
                              ),

                              title: Text(
                                meterName.isEmpty
                                    ? meterId
                                    : '$meterName ($meterId)',
                                maxLines: 1,
                                overflow: TextOverflow.ellipsis,
                                style: const TextStyle(
                                  fontWeight: FontWeight.w700,
                                  color: Color(0xFF0D47A1),
                                ),
                              ),

                              subtitle: Padding(
                                padding: const EdgeInsets.only(top: 6),
                                child: Text(
                                  'Prev: $prev  •  Last: $last  •  kWh: $con',
                                  style: const TextStyle(color: Colors.black54),
                                ),
                              ),

                              trailing: IconButton(
                                tooltip: 'Delete reading',
                                icon: const Icon(Icons.delete_outline),
                                color: Colors.red.shade600,
                                onPressed: () => _confirmDeleteReading(r),
                              ),
                            ),
                          );
                        },
                      ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
