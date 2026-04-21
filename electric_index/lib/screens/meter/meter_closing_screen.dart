import 'package:flutter/material.dart';
import 'package:electric_index/db/dao/database_helper.dart';

class MeterClosingScreen extends StatefulWidget {
  final String meterId;

  const MeterClosingScreen({super.key, required this.meterId});

  @override
  State<MeterClosingScreen> createState() => _MeterClosingScreenState();
}

class _MeterClosingScreenState extends State<MeterClosingScreen> {
  bool _loading = true;
  bool _saving = false;

  Map<String, dynamic>? _meter;
  List<Map<String, dynamic>> _companies = [];

  int? _selectedCompanyId;
  final TextEditingController _closingCtrl = TextEditingController();

  String _billingMonth = _currentMonth();

  static String _currentMonth() {
    final now = DateTime.now();
    return '${now.year}-${now.month.toString().padLeft(2, '0')}';
  }

  @override
  void initState() {
    super.initState();
    _load();
  }

  @override
  void dispose() {
    _closingCtrl.dispose();
    super.dispose();
  }

  Future<void> _load() async {
    try {
      final db = await DatabaseHelper.instance.database;

      final meter = await db.rawQuery(
        '''
        SELECT
          m.*,
          c.company_name,
          (
            SELECT r.index_last_month
            FROM readings r
            WHERE r.meter_id = m.meter_id
            ORDER BY
              COALESCE(r.created_at, '9999-12-31 23:59:59') DESC,
              r.reading_id DESC
            LIMIT 1
          ) AS latest_index_last
        FROM meters m
        LEFT JOIN companies c ON c.company_id = m.company_id
        WHERE m.meter_id = ?
        LIMIT 1
        ''',
        [widget.meterId],
      );

      final companies = await db.query(
        'companies',
        orderBy: 'company_name ASC',
      );

      if (!mounted) return;

      setState(() {
        _meter = meter.isNotEmpty ? meter.first : null;
        _companies = companies;
        _loading = false;
      });
    } catch (e) {
      if (!mounted) return;
      setState(() => _loading = false);
      _snack('Load dữ liệu lỗi: $e');
    }
  }

  Future<void> _save() async {
    final currentCompanyId = _toInt(_meter?['company_id']);
    final latestLast = _toInt(_meter?['latest_index_last']) ?? 0;
    final closingIndex = int.tryParse(_closingCtrl.text.trim());

    if (_selectedCompanyId == null) {
      _snack('Chọn công ty mới');
      return;
    }

    if (currentCompanyId != null && _selectedCompanyId == currentCompanyId) {
      _snack('Công ty mới phải khác công ty hiện tại');
      return;
    }

    if (closingIndex == null) {
      _snack('Nhập số chốt hợp lệ');
      return;
    }

    if (closingIndex < 0) {
      _snack('Số chốt phải >= 0');
      return;
    }

    if (closingIndex < latestLast) {
      _snack('Số chốt không được nhỏ hơn chỉ số gần nhất ($latestLast)');
      return;
    }

    setState(() => _saving = true);

    try {
      final db = await DatabaseHelper.instance.database;

      await db.transaction((txn) async {
        await txn.update(
          'meters',
          {
            'company_id': _selectedCompanyId,
            'initial_index': closingIndex,
            'first_billing_month': _billingMonth,
          },
          where: 'meter_id = ?',
          whereArgs: [widget.meterId],
        );
      });

      if (!mounted) return;
      Navigator.pop(context, true);
    } catch (e) {
      if (!mounted) return;
      _snack('Lỗi: $e');
    } finally {
      if (mounted) {
        setState(() => _saving = false);
      }
    }
  }

  int? _toInt(dynamic value) {
    if (value == null) return null;
    if (value is int) return value;
    if (value is num) return value.toInt();
    return int.tryParse(value.toString());
  }

  void _snack(String msg) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(content: Text(msg)),
    );
  }

  Future<void> _pickMonth() async {
    final now = DateTime.now();

    final picked = await showDatePicker(
      context: context,
      initialDate: DateTime(now.year, now.month, 1),
      firstDate: DateTime(2020),
      lastDate: DateTime(2100),
      helpText: 'Chọn tháng billing',
    );

    if (picked == null) return;

    setState(() {
      _billingMonth =
          '${picked.year}-${picked.month.toString().padLeft(2, '0')}';
    });
  }

  @override
  Widget build(BuildContext context) {
    final companyName = _meter?['company_name']?.toString() ?? '—';
    final latestLast = _toInt(_meter?['latest_index_last']) ?? 0;
    final currentCompanyId = _toInt(_meter?['company_id']);

    return Scaffold(
      backgroundColor: const Color(0xFFE3F2FD),
      appBar: AppBar(
        title: const Text('Chốt số bàn giao'),
        backgroundColor: const Color(0xFF1565C0),
        foregroundColor: Colors.white,
        elevation: 0,
      ),
      body: _loading
          ? const Center(child: CircularProgressIndicator())
          : SafeArea(
              child: LayoutBuilder(
                builder: (context, constraints) {
                  return SingleChildScrollView(
                    padding: const EdgeInsets.all(16),
                    child: ConstrainedBox(
                      constraints: BoxConstraints(
                        minHeight: constraints.maxHeight - 32,
                      ),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.stretch,
                        children: [
                          Card(
                            elevation: 3,
                            shadowColor: Colors.blue.withOpacity(0.15),
                            shape: RoundedRectangleBorder(
                              borderRadius: BorderRadius.circular(18),
                            ),
                            child: Padding(
                              padding: const EdgeInsets.symmetric(
                                horizontal: 16,
                                vertical: 14,
                              ),
                              child: Column(
                                crossAxisAlignment: CrossAxisAlignment.start,
                                children: [
                                  Text(
                                    'Meter: ${widget.meterId}',
                                    style: const TextStyle(
                                      fontWeight: FontWeight.w700,
                                      color: Color(0xFF0D47A1),
                                      fontSize: 16,
                                    ),
                                  ),
                                  const SizedBox(height: 6),
                                  Text(
                                    'Công ty hiện tại: $companyName',
                                    style: const TextStyle(
                                      color: Color(0xFF1565C0),
                                      fontWeight: FontWeight.w600,
                                    ),
                                  ),
                                  const SizedBox(height: 6),
                                  Text(
                                    'Chỉ số gần nhất: $latestLast',
                                    style: const TextStyle(
                                      color: Colors.black54,
                                    ),
                                  ),
                                ],
                              ),
                            ),
                          ),

                          const SizedBox(height: 14),

                          DropdownButtonFormField<int>(
                            value: _selectedCompanyId,
                            isExpanded: true,
                            decoration: const InputDecoration(
                              labelText: 'Công ty mới',
                              border: OutlineInputBorder(),
                              filled: true,
                              fillColor: Colors.white,
                            ),
                            items: _companies
                                .where((c) {
                                  final cid = _toInt(c['company_id']);
                                  return cid != null && cid != currentCompanyId;
                                })
                                .map((c) {
                                  return DropdownMenuItem<int>(
                                    value: _toInt(c['company_id']),
                                    child: Text(
                                      c['company_name']?.toString() ?? '',
                                      maxLines: 1,
                                      overflow: TextOverflow.ellipsis,
                                    ),
                                  );
                                })
                                .toList(),
                            selectedItemBuilder: (context) {
                              final filtered = _companies.where((c) {
                                final cid = _toInt(c['company_id']);
                                return cid != null && cid != currentCompanyId;
                              }).toList();

                              return filtered.map((c) {
                                return Align(
                                  alignment: Alignment.centerLeft,
                                  child: Text(
                                    c['company_name']?.toString() ?? '',
                                    maxLines: 1,
                                    overflow: TextOverflow.ellipsis,
                                  ),
                                );
                              }).toList();
                            },
                            onChanged: (v) {
                              setState(() => _selectedCompanyId = v);
                            },
                          ),

                          const SizedBox(height: 12),

                          TextField(
                            controller: _closingCtrl,
                            keyboardType: TextInputType.number,
                            decoration: InputDecoration(
                              labelText: 'Số chốt',
                              hintText: '>= $latestLast',
                              border: const OutlineInputBorder(),
                              filled: true,
                              fillColor: Colors.white,
                            ),
                          ),

                          const SizedBox(height: 12),

                          InkWell(
                            onTap: _pickMonth,
                            borderRadius: BorderRadius.circular(8),
                            child: InputDecorator(
                              decoration: const InputDecoration(
                                labelText: 'Tháng bắt đầu billing',
                                border: OutlineInputBorder(),
                                filled: true,
                                fillColor: Colors.white,
                              ),
                              child: Text(_billingMonth),
                            ),
                          ),

                          const SizedBox(height: 24),

                          SizedBox(
                            width: double.infinity,
                            height: 52,
                            child: FilledButton(
                              style: FilledButton.styleFrom(
                                backgroundColor: const Color(0xFF1565C0),
                                foregroundColor: Colors.white,
                              ),
                              onPressed: _saving ? null : _save,
                              child: _saving
                                  ? const SizedBox(
                                      width: 22,
                                      height: 22,
                                      child: CircularProgressIndicator(
                                        strokeWidth: 2.4,
                                        color: Colors.white,
                                      ),
                                    )
                                  : const Text('Lưu'),
                            ),
                          ),
                        ],
                      ),
                    ),
                  );
                },
              ),
            ),
    );
  }
}