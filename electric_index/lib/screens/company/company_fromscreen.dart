import 'package:flutter/material.dart';
import 'package:electric_index/db/dao/company_dao.dart';
import 'package:electric_index/db/dao/meter_dao.dart';
import 'package:electric_index/db/dao/locations_dao.dart';
import 'package:uuid/uuid.dart';

class CompanyFormScreen extends StatefulWidget {
  final int? companyId;
  final String? initialName;

  const CompanyFormScreen({super.key, this.companyId, this.initialName});

  @override
  State<CompanyFormScreen> createState() => _CompanyFormScreenState();
}

class _CompanyFormScreenState extends State<CompanyFormScreen> {
  final _nameCtrl = TextEditingController();
  final _meterIdCtrl = TextEditingController();
  final _meterNameCtrl = TextEditingController();

  final _companyDao = CompanyDao();
  final _meterDao = MeterDao();
  final _locationDao = LocationsDao();

  String autoCode = const Uuid().v4();

  List<Map<String, dynamic>> _locations = [];
  int? _selectedLocationId;

  bool _saving = false;
  bool _loading = true;

  bool get isEdit => widget.companyId != null;
  String get _meterId => _meterIdCtrl.text.trim();

  @override
  void initState() {
    super.initState();
    _nameCtrl.text = widget.initialName ?? '';
    _init();
  }

  Future<void> _init() async {
    final locations = await _locationDao.getAllLocations();

    if (!mounted) return;
    setState(() {
      _locations = locations;
      _loading = false;
    });
  }

  @override
  void dispose() {
    _nameCtrl.dispose();
    _meterIdCtrl.dispose();
    _meterNameCtrl.dispose();
    super.dispose();
  }

  InputDecoration _inputDecoration({
    required String label,
    required IconData icon,
  }) {
    return InputDecoration(
      labelText: label,
      prefixIcon: Icon(icon, color: const Color(0xFF1565C0)),
      filled: true,
      fillColor: Colors.white,
      border: OutlineInputBorder(
        borderRadius: BorderRadius.circular(14),
        borderSide: BorderSide.none,
      ),
    );
  }

  Future<void> _save() async {
    final companyName = _nameCtrl.text.trim();
    final meterName = _meterNameCtrl.text.trim();

    if (companyName.isEmpty) {
      ScaffoldMessenger.of(
        context,
      ).showSnackBar(const SnackBar(content: Text('Nhập company name nha')));
      return;
    }

    if (!isEdit && meterName.isEmpty) {
      ScaffoldMessenger.of(
        context,
      ).showSnackBar(const SnackBar(content: Text('Nhập meter name nha')));
      return;
    }
    if (!isEdit && _meterId.isEmpty) {
      ScaffoldMessenger.of(
        context,
      ).showSnackBar(const SnackBar(content: Text('Nhập meter ID nha')));
      return;
    }

    if (_saving) return;
    setState(() => _saving = true);

    try {
      if (isEdit) {
        // EDIT company only
        await _companyDao.updateCompany(
          companyId: widget.companyId!,
          companyCode: autoCode,
          companyName: companyName,
        );

        if (!mounted) return;
        ScaffoldMessenger.of(
          context,
        ).showSnackBar(const SnackBar(content: Text('✅ Updated company')));
        Navigator.pop(context, true);
        return;
      }

      // CREATE company first
      final companyId = await _companyDao.insertCompany(
        companyName: companyName,
        companyCode: autoCode,
      );

      // then CREATE first meter
      await _meterDao.insertMeter(
        meterId: _meterId,
        companyId: companyId,
        locationId: _selectedLocationId,
        meterName: meterName,
      );

      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(
            '✅ Created company_id=$companyId và meter_id=$_meterId',
          ),
        ),
      );
      Navigator.pop(context, true);
    } catch (e) {
      if (!mounted) return;
      ScaffoldMessenger.of(
        context,
      ).showSnackBar(SnackBar(content: Text('❌ Save failed: $e')));
      setState(() => _saving = false);
    }
  }

  Future<void> _delete() async {
    if (!isEdit) return;

    final ok = await showDialog<bool>(
      context: context,
      builder: (_) => AlertDialog(
        title: const Text('Delete company?'),
        content: const Text(
          'Xóa công ty sẽ xóa luôn meters/readings (do cascade).',
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context, false),
            child: const Text('Cancel'),
          ),
          FilledButton(
            onPressed: () => Navigator.pop(context, true),
            child: const Text('Delete'),
          ),
        ],
      ),
    );

    if (ok != true) return;

    setState(() => _saving = true);

    try {
      await _companyDao.deleteCompany(widget.companyId!);

      if (!mounted) return;
      ScaffoldMessenger.of(
        context,
      ).showSnackBar(const SnackBar(content: Text('🗑️ Deleted')));
      Navigator.pop(context, true);
    } catch (e) {
      if (!mounted) return;
      ScaffoldMessenger.of(
        context,
      ).showSnackBar(SnackBar(content: Text('❌ Delete failed: $e')));
      setState(() => _saving = false);
    }
  }

  Widget _sectionTitle(String text, IconData icon) {
    return Row(
      children: [
        Icon(icon, color: const Color(0xFF1565C0)),
        const SizedBox(width: 8),
        Text(
          text,
          style: const TextStyle(
            fontSize: 16,
            fontWeight: FontWeight.w700,
            color: Color(0xFF0D47A1),
          ),
        ),
      ],
    );
  }

  Widget _buildCard({required List<Widget> children}) {
    return Card(
      elevation: 3,
      shadowColor: Colors.blue.withOpacity(0.12),
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(18)),
      child: Padding(
        padding: const EdgeInsets.all(14),
        child: Column(children: children),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    if (_loading) {
      return const Scaffold(body: Center(child: CircularProgressIndicator()));
    }

    return Scaffold(
      backgroundColor: const Color(0xFFE3F2FD),
      appBar: AppBar(
        title: Text(isEdit ? 'Edit Company' : 'Create Company + Meter'),
        backgroundColor: const Color(0xFF1565C0),
        foregroundColor: Colors.white,
        elevation: 0,
        actions: [
          if (isEdit)
            IconButton(
              icon: const Icon(Icons.delete_outline),
              onPressed: _saving ? null : _delete,
            ),
        ],
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
                    crossAxisAlignment: CrossAxisAlignment.stretch,
                    children: [
                      _buildCard(
                        children: [
                          _sectionTitle('Company info', Icons.apartment),
                          const SizedBox(height: 14),
                          TextField(
                            controller: _nameCtrl,
                            textInputAction: TextInputAction.next,
                            decoration: _inputDecoration(
                              label: 'Company name *',
                              icon: Icons.business,
                            ),
                          ),
                        ],
                      ),

                      const SizedBox(height: 16),

                      if (!isEdit)
                        _buildCard(
                          children: [
                            _sectionTitle('First meter', Icons.electric_meter),
                            const SizedBox(height: 8),
                            TextField(
                              controller: _meterIdCtrl,
                              textInputAction: TextInputAction.next,
                              decoration: _inputDecoration(
                                label: 'Meter ID *',
                                icon: Icons.tag,
                              ),
                            ),
                            const SizedBox(height: 12),
                            TextField(
                              controller: _meterNameCtrl,
                              textInputAction: TextInputAction.next,
                              decoration: _inputDecoration(
                                label: 'Meter name *',
                                icon: Icons.speed,
                              ),
                            ),
                            const SizedBox(height: 12),
                            DropdownButtonFormField<int?>(
                              value: _selectedLocationId,
                              isExpanded: true,
                              items: [
                                const DropdownMenuItem<int?>(
                                  value: null,
                                  child: Text('No location'),
                                ),
                                ..._locations.map((l) {
                                  final id = l['location_id'] as int;
                                  final name = (l['location_name'] ?? '')
                                      .toString();
                                  final floor = l['floor'];
                                  final text = floor == null
                                      ? name
                                      : 'Floor $floor - $name';

                                  return DropdownMenuItem<int?>(
                                    value: id,
                                    child: Text(
                                      text,
                                      maxLines: 2,
                                      overflow: TextOverflow.ellipsis,
                                    ),
                                  );
                                }),
                              ],
                              onChanged: (v) {
                                setState(() => _selectedLocationId = v);
                              },
                              decoration: _inputDecoration(
                                label: 'Location (optional)',
                                icon: Icons.location_on,
                              ),
                            ),
                          ],
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
                              : Row(
                                  mainAxisAlignment: MainAxisAlignment.center,
                                  children: [
                                    const Icon(Icons.check_circle_outline),
                                    const SizedBox(width: 8),
                                    Text(
                                      isEdit
                                          ? 'Update Company'
                                          : 'Create Company + Meter',
                                    ),
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
