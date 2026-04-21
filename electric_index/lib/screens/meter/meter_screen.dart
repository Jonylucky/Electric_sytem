import 'package:flutter/material.dart';
import 'package:electric_index/db/dao/meter_dao.dart';
import 'package:electric_index/db/dao/locations_dao.dart';
import 'package:electric_index/db/dao/company_dao.dart';
import '../../services/qr_excel_exporter.dart';
import 'package:share_plus/share_plus.dart';

class MeterScreen extends StatefulWidget {
  final String? meterId; // null => create, not null => edit
  const MeterScreen({super.key, this.meterId});

  @override
  State<MeterScreen> createState() => _MeterScreenState();
}

class _MeterScreenState extends State<MeterScreen> {
  final _meterIdCtrl = TextEditingController();
  final _meterNameCtrl = TextEditingController();

  final _companyDao = CompanyDao();
  final _locationDao = LocationsDao();
  final _meterDao = MeterDao();

  List<Map<String, dynamic>> _companies = [];
  List<Map<String, dynamic>> _locations = [];

  int? _selectedCompanyId;
  int? _selectedLocationId;

  bool _loading = true;
  bool _saving = false;

  bool get isEdit => widget.meterId != null;

  String get _meterId => _meterIdCtrl.text.trim();

  @override
  void initState() {
    super.initState();
    if (isEdit) {
      _meterIdCtrl.text = widget.meterId!;
    }
    _init();
  }

  Future<void> _init() async {
    final companies = await _companyDao.getAllCompanies();
    final locations = await _locationDao.getAllLocations();

    if (isEdit) {
      final meter = await _meterDao.getMeterById(_meterId);
      if (meter != null) {
        _meterNameCtrl.text = (meter['meter_name'] ?? '') as String;
        _selectedCompanyId = meter['company_id'] as int;
        _selectedLocationId = meter['location_id'] as int?;
      }
    } else {
      if (companies.isNotEmpty) {
        _selectedCompanyId = companies.first['company_id'] as int;
      }
      _selectedLocationId = null;
    }

    if (!mounted) return;
    setState(() {
      _companies = companies;
      _locations = locations;
      _loading = false;
    });
  }

  @override
  void dispose() {
    _meterIdCtrl.dispose();
    _meterNameCtrl.dispose();
    super.dispose();
  }

  String _getSelectedCompanyName() {
    if (_selectedCompanyId == null) {
      return 'Company';
    }

    final c = _companies.firstWhere(
      (e) => e['company_id'] == _selectedCompanyId,
      orElse: () => const {},
    );

    final name = (c['company_name'] ?? '').toString().trim();
    return name.isNotEmpty ? name : 'Company_${_selectedCompanyId!}';
  }

  Future<void> _save() async {
    final meterName = _meterNameCtrl.text.trim();

    if (_selectedCompanyId == null) {
      ScaffoldMessenger.of(
        context,
      ).showSnackBar(const SnackBar(content: Text('Chọn company nha')));
      return;
    }

    if (!isEdit && _meterId.isEmpty) {
      ScaffoldMessenger.of(
        context,
      ).showSnackBar(const SnackBar(content: Text('Nhập Meter ID')));
      return;
    }

    if (_saving) return;
    setState(() => _saving = true);

    try {
      // 1) Save meter
      if (isEdit) {
        await _meterDao.updateMeter(
          meterId: _meterId,
          companyId: _selectedCompanyId!,
          locationId: _selectedLocationId,
          meterName: meterName.isEmpty ? null : meterName,
        );
      } else {
        await _meterDao.insertMeter(
          meterId: _meterId,
          companyId: _selectedCompanyId!,
          locationId: _selectedLocationId,
          meterName: meterName.isEmpty ? null : meterName,
        );
      }

      // 2) ✅ Chỉ Export + Share khi TẠO MỚI (không share khi edit)
      if (!isEdit) {
        final exporter = QrExcelExporter(meterDao: _meterDao);
        final companyName = _getSelectedCompanyName();

        final filePath = await exporter.exportMetersQrExcel(
          companyId: _selectedCompanyId!,
          companyName: companyName,
          highlightMeterId: _meterId, // ✅ đánh dấu NEW meter vừa tạo
        );

        if (!mounted) return;

        await Share.shareXFiles(
          [XFile(filePath)],
          text: 'QR meters – $companyName',
          subject: 'QR Excel – $companyName',
        );
      }

      if (!mounted) return;
      Navigator.pop(context, true);
    } catch (e) {
      if (!mounted) return;
      ScaffoldMessenger.of(
        context,
      ).showSnackBar(SnackBar(content: Text('❌ Save/Export/Share lỗi: $e')));
    } finally {
      if (mounted) setState(() => _saving = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    if (_loading) {
      return const Scaffold(body: Center(child: CircularProgressIndicator()));
    }

    return Scaffold(
      backgroundColor: const Color(0xFFE3F2FD),
      appBar: AppBar(
        title: Text(isEdit ? 'Edit Meter' : 'Create Meter'),
        backgroundColor: const Color(0xFF1565C0),
        foregroundColor: Colors.white,
        elevation: 0,
        bottom: PreferredSize(
          preferredSize: const Size.fromHeight(30),
          child: Padding(
            padding: const EdgeInsets.only(left: 16, right: 16, bottom: 10),
            child: Align(
              alignment: Alignment.centerLeft,
              child: Text(
                _meterId.isNotEmpty ? 'ID: $_meterId' : 'ID: (nhập bên dưới)',
                style: Theme.of(context).textTheme.bodySmall?.copyWith(
                  color: Colors.white70,
                  fontWeight: FontWeight.w600,
                ),
              ),
            ),
          ),
        ),
      ),

      body: SafeArea(
        child: LayoutBuilder(
          builder: (context, constraints) {
            return SingleChildScrollView(
              padding: EdgeInsets.only(
                left: 16,
                right: 16,
                top: 16,
                bottom: 16 + MediaQuery.of(context).viewInsets.bottom,
              ),
              child: ConstrainedBox(
                constraints: BoxConstraints(minHeight: constraints.maxHeight),
                child: IntrinsicHeight(
                  child: Column(
                    children: [
                      Card(
                        elevation: 3,
                        shadowColor: Colors.blue.withOpacity(0.15),
                        shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(18),
                        ),
                        child: Padding(
                          padding: const EdgeInsets.all(14),
                          child: Column(
                            children: [
                              // ===== Meter ID (user nhập khi tạo mới) =====
                              TextField(
                                controller: _meterIdCtrl,
                                readOnly: isEdit,
                                onChanged: (_) => setState(() {}),
                                textInputAction: TextInputAction.next,
                                decoration: InputDecoration(
                                  labelText: isEdit ? 'Meter ID' : 'Meter ID *',
                                  prefixIcon: const Icon(
                                    Icons.tag,
                                    color: Color(0xFF1565C0),
                                  ),
                                  filled: true,
                                  fillColor: isEdit
                                      ? Colors.grey.shade100
                                      : Colors.white,
                                  border: OutlineInputBorder(
                                    borderRadius: BorderRadius.circular(14),
                                    borderSide: BorderSide.none,
                                  ),
                                ),
                              ),

                              const SizedBox(height: 12),

                              // ===== Meter name =====
                              TextField(
                                controller: _meterNameCtrl,
                                textInputAction: TextInputAction.next,
                                decoration: InputDecoration(
                                  labelText: 'Meter name (optional)',
                                  prefixIcon: const Icon(
                                    Icons.electric_meter,
                                    color: Color(0xFF1565C0),
                                  ),
                                  filled: true,
                                  fillColor: Colors.white,
                                  border: OutlineInputBorder(
                                    borderRadius: BorderRadius.circular(14),
                                    borderSide: BorderSide.none,
                                  ),
                                ),
                              ),

                              const SizedBox(height: 12),

                              // ===== Company: ô đóng dùng ellipsis tránh tràn =====
                              DropdownButtonFormField<int>(
                                value: _selectedCompanyId,
                                isExpanded: true,
                                selectedItemBuilder: (context) {
                                  return _companies.map((c) {
                                    final name = c['company_name'] as String;
                                    return Text(
                                      name,
                                      maxLines: 1,
                                      overflow: TextOverflow.ellipsis,
                                      style: Theme.of(context)
                                          .textTheme
                                          .bodyMedium
                                          ?.copyWith(
                                            color: const Color(0xFF0D47A1),
                                          ),
                                    );
                                  }).toList();
                                },
                                items: _companies.map((c) {
                                  final id = c['company_id'] as int;
                                  final name = c['company_name'] as String;
                                  return DropdownMenuItem<int>(
                                    value: id,
                                    child: Text(
                                      name,
                                      softWrap: true,
                                      maxLines: 3,
                                      overflow: TextOverflow.ellipsis,
                                      style: Theme.of(context)
                                          .textTheme
                                          .bodyMedium
                                          ?.copyWith(
                                            color: const Color(0xFF0D47A1),
                                          ),
                                    ),
                                  );
                                }).toList(),
                                onChanged: (v) =>
                                    setState(() => _selectedCompanyId = v),
                                decoration: InputDecoration(
                                  labelText: 'Company *',
                                  prefixIcon: const Icon(
                                    Icons.apartment,
                                    color: Color(0xFF1565C0),
                                  ),
                                  filled: true,
                                  fillColor: Colors.white,
                                  border: OutlineInputBorder(
                                    borderRadius: BorderRadius.circular(14),
                                    borderSide: BorderSide.none,
                                  ),
                                ),
                              ),

                              const SizedBox(height: 12),

                              // ===== Location: ô đóng dùng ellipsis tránh tràn =====
                              DropdownButtonFormField<int?>(
                                value: _selectedLocationId,
                                isExpanded: true,
                                selectedItemBuilder: (context) {
                                  return [
                                    const Text(
                                      'No location',
                                      maxLines: 1,
                                      overflow: TextOverflow.ellipsis,
                                    ),
                                    ..._locations.map((l) {
                                      final name = l['location_name'] as String;
                                      final floor = l['floor'] as int;
                                      return Text(
                                        'Floor $floor - $name',
                                        maxLines: 1,
                                        overflow: TextOverflow.ellipsis,
                                        style: Theme.of(context)
                                            .textTheme
                                            .bodyMedium
                                            ?.copyWith(
                                              color: const Color(0xFF0D47A1),
                                            ),
                                      );
                                    }),
                                  ];
                                },
                                items: [
                                  const DropdownMenuItem<int?>(
                                    value: null,
                                    child: Text('No location'),
                                  ),
                                  ..._locations.map((l) {
                                    final id = l['location_id'] as int;
                                    final name = l['location_name'] as String;
                                    final floor = l['floor'] as int;
                                    return DropdownMenuItem<int?>(
                                      value: id,
                                      child: Text(
                                        'Floor $floor - $name',
                                        softWrap: true,
                                        maxLines: 3,
                                        overflow: TextOverflow.ellipsis,
                                        style: Theme.of(context)
                                            .textTheme
                                            .bodyMedium
                                            ?.copyWith(
                                              color: const Color(0xFF0D47A1),
                                            ),
                                      ),
                                    );
                                  }),
                                ],
                                onChanged: (v) =>
                                    setState(() => _selectedLocationId = v),
                                decoration: InputDecoration(
                                  labelText: 'Location (optional)',
                                  prefixIcon: const Icon(
                                    Icons.location_on,
                                    color: Color(0xFF1565C0),
                                  ),
                                  filled: true,
                                  fillColor: Colors.white,
                                  border: OutlineInputBorder(
                                    borderRadius: BorderRadius.circular(14),
                                    borderSide: BorderSide.none,
                                  ),
                                ),
                              ),
                            ],
                          ),
                        ),
                      ),

                      const Spacer(),

                      SizedBox(
                        width: double.infinity,
                        height: 56,
                        child: FilledButton(
                          style: FilledButton.styleFrom(
                            backgroundColor: const Color(0xFF1565C0),
                            foregroundColor: Colors.white,
                            shape: RoundedRectangleBorder(
                              borderRadius: BorderRadius.circular(16),
                            ),
                            textStyle: const TextStyle(
                              fontSize: 17,
                              fontWeight: FontWeight.bold,
                            ),
                          ),
                          onPressed: _saving ? null : _save,
                          child: _saving
                              ? const SizedBox(
                                  width: 22,
                                  height: 22,
                                  child: CircularProgressIndicator(
                                    strokeWidth: 2,
                                    color: Colors.white,
                                  ),
                                )
                              : const Row(
                                  mainAxisAlignment: MainAxisAlignment.center,
                                  children: [
                                    Icon(Icons.check_circle_outline),
                                    SizedBox(width: 8),
                                    Text('Save'),
                                  ],
                                ),
                        ),
                      ),
                    ],
                  ),
                ),
              ),
            );
          },
        ),
      ),
    );
  }
}
