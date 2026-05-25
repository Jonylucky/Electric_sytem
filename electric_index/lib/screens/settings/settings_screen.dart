import 'package:flutter/material.dart';

import 'package:electric_index/services/api_config.dart';

class SettingsScreen extends StatefulWidget {
  const SettingsScreen({super.key});

  @override
  State<SettingsScreen> createState() => _SettingsScreenState();
}

class _SettingsScreenState extends State<SettingsScreen> {
  final _controller = TextEditingController();
  bool _saving = false;

  @override
  void initState() {
    super.initState();
    _controller.text = ApiConfig.baseUrl;
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  InputDecoration _inputDecoration({
    required String label,
    required IconData icon,
    String? hint,
  }) {
    return InputDecoration(
      labelText: label,
      hintText: hint,
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
    setState(() => _saving = true);
    try {
      await ApiConfig.setBaseUrl(_controller.text);
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Đã lưu địa chỉ server')),
      );
    } on ArgumentError catch (e) {
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text(e.message ?? 'URL không hợp lệ')),
      );
    } finally {
      if (mounted) setState(() => _saving = false);
    }
  }

  void _resetDefault() {
    _controller.text = ApiConfig.defaultBaseUrl;
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

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFFE3F2FD),
      appBar: AppBar(
        title: const Text('Settings'),
        backgroundColor: const Color(0xFF1565C0),
        foregroundColor: Colors.white,
        elevation: 0,
      ),
      body: Padding(
        padding: const EdgeInsets.all(16),
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
                padding: const EdgeInsets.all(14),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    _sectionTitle('Địa chỉ server API', Icons.dns),
                    const SizedBox(height: 8),
                    const Text(
                      'Ví dụ: http://192.168.10.111:8081',
                      style: TextStyle(color: Colors.black54, fontSize: 13),
                    ),
                    const SizedBox(height: 14),
                    TextField(
                      controller: _controller,
                      keyboardType: TextInputType.url,
                      autocorrect: false,
                      enabled: !_saving,
                      textInputAction: TextInputAction.done,
                      decoration: _inputDecoration(
                        label: 'Base URL',
                        icon: Icons.link,
                        hint: ApiConfig.defaultBaseUrl,
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
                          Text('Lưu'),
                        ],
                      ),
              ),
            ),
            const SizedBox(height: 12),
            SizedBox(
              width: double.infinity,
              height: 48,
              child: OutlinedButton.icon(
                style: OutlinedButton.styleFrom(
                  foregroundColor: const Color(0xFF1565C0),
                  side: const BorderSide(color: Color(0xFF1565C0)),
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(16),
                  ),
                ),
                onPressed: _saving ? null : _resetDefault,
                icon: const Icon(Icons.restore),
                label: const Text('Khôi phục mặc định'),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
