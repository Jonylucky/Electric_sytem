import 'package:flutter/material.dart';
import 'package:electric_index/db/dao/meter_dao.dart';
import 'package:electric_index/screens/meter/meter_screen.dart';

class ListMeterScreen extends StatefulWidget {
  const ListMeterScreen({super.key});

  @override
  State<ListMeterScreen> createState() => _ListMeterScreenState();
}

class _ListMeterScreenState extends State<ListMeterScreen> {
  final _dao = MeterDao();

  bool _loading = true;
  List<Map<String, dynamic>> _items = [];

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    setState(() => _loading = true);
    final data = await _dao.getMetersView();
    setState(() {
      _items = data;
      _loading = false;
    });
  }

  Future<void> _delete(String meterId) async {
    await _dao.deleteMeter(meterId);
    await _load();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFFE3F2FD), // 🔵 nền xanh nhạt

      appBar: AppBar(
        title: const Text('Meters'),
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
            MaterialPageRoute(builder: (_) => const MeterScreen()),
          );
          if (changed == true) _load();
        },
      ),

      body: _loading
          ? const Center(child: CircularProgressIndicator())
          : _items.isEmpty
          ? const Center(
              child: Text(
                'Chưa có meter nào',
                style: TextStyle(color: Colors.black54),
              ),
            )
          : ListView.separated(
              padding: const EdgeInsets.all(16),
              itemCount: _items.length,
              separatorBuilder: (_, __) => const SizedBox(height: 12),
              itemBuilder: (context, i) {
                final x = _items[i];
                final meterId = x['meter_id'] as String;
                final meterName = (x['meter_name'] ?? '') as String;
                final companyName = x['company_name'] as String;

                final locName = x['location_name'] as String?;
                final floor = x['floor'] as int?;

                final locText = (locName == null || floor == null)
                    ? 'No location'
                    : 'Floor $floor - $locName';

                return Dismissible(
                  key: ValueKey(meterId),
                  direction: DismissDirection.endToStart,

                  background: Container(
                    decoration: BoxDecoration(
                      color: Colors.red.shade600,
                      borderRadius: BorderRadius.circular(18),
                    ),
                    alignment: Alignment.centerRight,
                    padding: const EdgeInsets.only(right: 18),
                    child: const Row(
                      mainAxisAlignment: MainAxisAlignment.end,
                      children: [
                        Icon(Icons.delete_outline, color: Colors.white),
                        SizedBox(width: 6),
                        Text(
                          'Delete',
                          style: TextStyle(
                            color: Colors.white,
                            fontWeight: FontWeight.w700,
                          ),
                        ),
                      ],
                    ),
                  ),

                  confirmDismiss: (_) async {
                    return await showDialog<bool>(
                          context: context,
                          builder: (_) => AlertDialog(
                            title: const Text('Delete meter?'),
                            content: Text(meterId),
                            actions: [
                              TextButton(
                                onPressed: () => Navigator.pop(context, false),
                                child: const Text('Cancel'),
                              ),
                              FilledButton(
                                style: FilledButton.styleFrom(
                                  backgroundColor: Colors.red.shade600,
                                  foregroundColor: Colors.white,
                                ),
                                onPressed: () => Navigator.pop(context, true),
                                child: const Text('Delete'),
                              ),
                            ],
                          ),
                        ) ??
                        false;
                  },

                  onDismissed: (_) => _delete(meterId),

                  child: Card(
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
                          Icons.electric_meter,
                          color: Color(0xFF1565C0),
                          size: 26,
                        ),
                      ),

                      title: Text(
                        meterName.isEmpty ? meterId : '$meterName',
                        style: const TextStyle(
                          fontWeight: FontWeight.w700,
                          color: Color(0xFF0D47A1),
                        ),
                      ),

                      subtitle: Padding(
                        padding: const EdgeInsets.only(top: 4),
                        child: Text(
                          '$companyName • $locText',
                          style: const TextStyle(color: Colors.black54),
                        ),
                      ),

                      trailing: const Icon(
                        Icons.edit,
                        color: Color(0xFF1565C0),
                      ),

                      onTap: () async {
                        final changed = await Navigator.push(
                          context,
                          MaterialPageRoute(
                            builder: (_) => MeterScreen(meterId: meterId),
                          ),
                        );
                        if (changed == true) _load();
                      },
                    ),
                  ),
                );
              },
            ),
    );
  }
}
