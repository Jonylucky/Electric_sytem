import 'package:flutter/material.dart';
import 'package:electric_index/db/dao/company_dao.dart';
import 'package:electric_index/screens/company/company_fromscreen.dart';

class CompanyListScreen extends StatefulWidget {
  const CompanyListScreen({super.key});

  @override
  State<CompanyListScreen> createState() => _CompanyListScreenState();
}

class _CompanyListScreenState extends State<CompanyListScreen> {
  final _dao = CompanyDao();
  List<Map<String, dynamic>> _companies = [];
  bool _loading = true;

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    setState(() => _loading = true);
    final data = await _dao.getAllCompanies();
    setState(() {
      _companies = data;
      _loading = false;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFFE3F2FD), // 🔵 nền xanh nhạt

      appBar: AppBar(
        title: const Text('Companies'),
        backgroundColor: const Color(0xFF1565C0),
        foregroundColor: Colors.white,
        elevation: 0,
      ),

      floatingActionButton: FloatingActionButton(
        backgroundColor: const Color(0xFF1565C0),
        foregroundColor: Colors.white,
        child: const Icon(Icons.add),
        onPressed: () async {
          final changed = await Navigator.push(
            context,
            MaterialPageRoute(builder: (_) => const CompanyFormScreen()),
          );
          if (changed == true) _load();
        },
      ),

      body: _loading
          ? const Center(child: CircularProgressIndicator())
          : _companies.isEmpty
          ? const Center(
              child: Text(
                'Chưa có company nào',
                style: TextStyle(color: Colors.black54),
              ),
            )
          : ListView.separated(
              padding: const EdgeInsets.all(16),
              itemCount: _companies.length,
              separatorBuilder: (_, __) => const SizedBox(height: 12),
              itemBuilder: (context, i) {
                final c = _companies[i];
                final id = c['company_id'] as int;
                final name = c['company_name'] as String;

                return Card(
                  elevation: 3,
                  shadowColor: Colors.blue.withOpacity(0.15),
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(18),
                  ),
                  child: ListTile(
                    contentPadding: const EdgeInsets.symmetric(
                      horizontal: 16,
                      vertical: 10,
                    ),

                    leading: Container(
                      padding: const EdgeInsets.all(10),
                      decoration: BoxDecoration(
                        color: const Color(0xFFBBDEFB),
                        borderRadius: BorderRadius.circular(14),
                      ),
                      child: const Icon(
                        Icons.apartment,
                        color: Color(0xFF1565C0),
                        size: 26,
                      ),
                    ),

                    title: Text(
                      name,
                      style: const TextStyle(
                        fontWeight: FontWeight.w700,
                        color: Color(0xFF0D47A1),
                        fontSize: 16,
                      ),
                    ),

                    subtitle: Padding(
                      padding: const EdgeInsets.only(top: 4),
                      child: Text(
                        'ID: $id',
                        style: const TextStyle(color: Colors.black54),
                      ),
                    ),

                    trailing: const Icon(Icons.edit, color: Color(0xFF1565C0)),

                    onTap: () async {
                      final changed = await Navigator.push(
                        context,
                        MaterialPageRoute(
                          builder: (_) => CompanyFormScreen(
                            companyId: id,
                            initialName: name,
                          ),
                        ),
                      );
                      if (changed == true) _load();
                    },
                  ),
                );
              },
            ),
    );
  }
}
