import 'package:flutter/material.dart';
import 'package:electric_index/db/dao/database_helper.dart';
import 'package:electric_index/db/dao/meter_reset_dao.dart';

class ResetScreen extends StatefulWidget {
  final String meterId;
  final String initialLastBeforeReset;
  final MeterResetReadingService service;

  const ResetScreen({
    super.key,
    required this.meterId,
    required this.initialLastBeforeReset,
    required this.service,
  });

  @override
  State<ResetScreen> createState() => _ResetScreenState();
}

class _ResetScreenState extends State<ResetScreen> {
  late final TextEditingController _lastBeforeResetCtrl;
  late final TextEditingController _noteCtrl;

  DateTime _resetDate = DateTime.now();
  bool _saving = false;

  static const Color bluePrimary = Color(0xFF1565C0);
  static const Color blueLight = Color(0xFFE3F2FD);

  @override
  void initState() {
    super.initState();
    _lastBeforeResetCtrl = TextEditingController(
      text: widget.initialLastBeforeReset,
    );
    _noteCtrl = TextEditingController();
  }

  @override
  void dispose() {
    _lastBeforeResetCtrl.dispose();
    _noteCtrl.dispose();
    super.dispose();
  }

  Future<void> _pickResetDate() async {
    final pickedDate = await showDatePicker(
      context: context,
      initialDate: _resetDate,
      firstDate: DateTime(2000),
      lastDate: DateTime(2100),
    );
    if (pickedDate == null || !mounted) return;

    final pickedTime = await showTimePicker(
      context: context,
      initialTime: TimeOfDay.fromDateTime(_resetDate),
    );
    if (pickedTime == null || !mounted) return;

    setState(() {
      _resetDate = DateTime(
        pickedDate.year,
        pickedDate.month,
        pickedDate.day,
        pickedTime.hour,
        pickedTime.minute,
      );
    });
  }

  Future<void> _submit() async {
    final messenger = ScaffoldMessenger.of(context);
    final navigator = Navigator.of(context);

    final lastBeforeReset = int.tryParse(_lastBeforeResetCtrl.text.trim());
    final note = _noteCtrl.text.trim().isEmpty ? null : _noteCtrl.text.trim();

    if (lastBeforeReset == null) {
      messenger.showSnackBar(
        const SnackBar(
          content: Text('Chỉ số trước khi reset phải là số hợp lệ'),
        ),
      );
      return;
    }

    if (lastBeforeReset < 0) {
      messenger.showSnackBar(
        const SnackBar(content: Text('Chỉ số trước khi reset không được âm')),
      );
      return;
    }

    FocusScope.of(context).unfocus();

    setState(() => _saving = true);

    try {
      final db = await DatabaseHelper.instance.database;
      if (!mounted) return;

      await widget.service.createResetAtMonth(
        db: db,
        meterId: widget.meterId,
        resetDate: _resetDate,
        lastIndexBeforeReset: lastBeforeReset,
        note: note,
      );

      if (!mounted) return;
      navigator.pop(true);
    } catch (e) {
      if (!mounted) return;
      setState(() => _saving = false);
      messenger.showSnackBar(SnackBar(content: Text('Reset lỗi: $e')));
    }
  }

  @override
  Widget build(BuildContext context) {
    final bottom = MediaQuery.of(context).viewInsets.bottom;

    return Scaffold(
      backgroundColor: blueLight,
      appBar: AppBar(
        title: const Text('Reset đồng hồ'),
        backgroundColor: bluePrimary,
        foregroundColor: Colors.white,
      ),
      body: SafeArea(
        child: SingleChildScrollView(
          padding: EdgeInsets.fromLTRB(16, 16, 16, bottom + 16),
          child: Column(
            children: [
              Card(
                elevation: 2,
                color: Colors.white,
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(14),
                ),
                child: Padding(
                  padding: const EdgeInsets.all(14),
                  child: Column(
                    children: [
                      TextField(
                        controller: _lastBeforeResetCtrl,
                        keyboardType: TextInputType.number,
                        enabled: !_saving,
                        decoration: InputDecoration(
                          labelText: 'Chỉ số trước khi reset',
                          helperText: 'Lưu vào last_index_before_reset',
                          labelStyle: const TextStyle(color: bluePrimary),
                          prefixIcon: const Icon(
                            Icons.restart_alt,
                            color: bluePrimary,
                          ),
                          filled: true,
                          fillColor: Colors.white,
                          border: OutlineInputBorder(
                            borderRadius: BorderRadius.circular(12),
                          ),
                          focusedBorder: OutlineInputBorder(
                            borderRadius: BorderRadius.circular(12),
                            borderSide: const BorderSide(
                              color: bluePrimary,
                              width: 1.5,
                            ),
                          ),
                        ),
                      ),
                      const SizedBox(height: 12),
                      InkWell(
                        borderRadius: BorderRadius.circular(12),
                        onTap: _saving ? null : _pickResetDate,
                        child: Container(
                          width: double.infinity,
                          padding: const EdgeInsets.symmetric(
                            horizontal: 12,
                            vertical: 14,
                          ),
                          decoration: BoxDecoration(
                            color: Colors.white,
                            borderRadius: BorderRadius.circular(12),
                            border: Border.all(
                              color: bluePrimary.withOpacity(0.3),
                            ),
                          ),
                          child: Row(
                            children: [
                              const Icon(
                                Icons.calendar_today,
                                size: 20,
                                color: bluePrimary,
                              ),
                              const SizedBox(width: 8),
                              Expanded(
                                child: Text(
                                  'Ngày reset: ${_resetDate.toString().substring(0, 16)}',
                                  style: const TextStyle(
                                    color: Color(0xFF0D47A1),
                                    fontWeight: FontWeight.w600,
                                  ),
                                  overflow: TextOverflow.ellipsis,
                                ),
                              ),
                              const Icon(
                                Icons.edit_calendar,
                                color: bluePrimary,
                              ),
                            ],
                          ),
                        ),
                      ),
                      const SizedBox(height: 12),
                      TextField(
                        controller: _noteCtrl,
                        maxLines: 2,
                        enabled: !_saving,
                        decoration: InputDecoration(
                          labelText: 'Ghi chú (optional)',
                          helperText: 'Lưu vào note',
                          labelStyle: const TextStyle(color: bluePrimary),
                          prefixIcon: const Icon(
                            Icons.note_alt_outlined,
                            color: bluePrimary,
                          ),
                          filled: true,
                          fillColor: Colors.white,
                          border: OutlineInputBorder(
                            borderRadius: BorderRadius.circular(12),
                          ),
                          focusedBorder: OutlineInputBorder(
                            borderRadius: BorderRadius.circular(12),
                            borderSide: const BorderSide(
                              color: bluePrimary,
                              width: 1.5,
                            ),
                          ),
                        ),
                      ),
                    ],
                  ),
                ),
              ),
              const SizedBox(height: 12),
              Container(
                width: double.infinity,
                padding: const EdgeInsets.all(12),
                decoration: BoxDecoration(
                  color: Colors.white,
                  borderRadius: BorderRadius.circular(12),
                  border: Border.all(color: bluePrimary.withOpacity(0.15)),
                ),
                child: const Text(
                  'Reset sẽ không tạo đồng hồ mới. Hệ thống sẽ lưu reset_date, reset_month, last_index_before_reset và tự tính offset_after_reset để các tháng sau vẫn tính consumption đúng.',
                  style: TextStyle(color: Color(0xFF0D47A1), fontSize: 13),
                ),
              ),
              const SizedBox(height: 16),
              SizedBox(
                width: double.infinity,
                height: 52,
                child: FilledButton.icon(
                  style: FilledButton.styleFrom(
                    backgroundColor: bluePrimary,
                    foregroundColor: Colors.white,
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(14),
                    ),
                  ),
                  onPressed: _saving ? null : _submit,
                  icon: _saving
                      ? const SizedBox(
                          width: 18,
                          height: 18,
                          child: CircularProgressIndicator(strokeWidth: 2),
                        )
                      : const Icon(Icons.check),
                  label: Text(_saving ? 'Đang lưu...' : 'Xác nhận Reset'),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
