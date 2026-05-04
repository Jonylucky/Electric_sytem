import 'package:flutter/material.dart';

import 'package:electric_index/screens/scan/scan_pr_screen.dart';
import 'package:electric_index/screens/settings/settings_screen.dart';
import 'package:electric_index/screens/home/home_dashboard_screen.dart';

import 'package:electric_index/services/reading_pull_service.dart';
import 'package:electric_index/services/reading_sync_api_service.dart';
import 'package:electric_index/db/dao/reading_dao.dart';
import 'package:electric_index/db/dao/database_helper.dart';
import 'package:electric_index/utils/month_helper.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  int _index = 0;
  bool _checking = false;

  late final ReadingPullService _pullService;

  final _pages = const [
    HomeDashboardScreen(),
    ScanQrScreen(),
    SettingsScreen(),
  ];

  @override
  void initState() {
    super.initState();

    _pullService = ReadingPullService(
      api: ReadingSyncApiService(),
      dao: ReadingDao(),
    );
    // thêm hàm này vào để update first_billing_month cho tất cả meters
    // _runFix();
  }

  Future<void> _runFix() async {
    await fixAllMetersFirstBillingMonth(fallbackMonth: '2026-01');
  }

  Future<void> fixAllMetersFirstBillingMonth({
    required String fallbackMonth, // ví dụ: '2026-01'
  }) async {
    final db = await DatabaseHelper.instance.database;

    print('===== FIX METERS START =====');

    // 1. Fix NULL
    final updatedNull = await db.update('meters', {
      'first_billing_month': fallbackMonth,
    }, where: 'first_billing_month IS NULL');

    print('Fixed NULL first_billing_month: $updatedNull');

    // 2. Fix meter bị set tương lai (>= selectedMonth)
    final now = DateTime.now();
    final currentMonth = '${now.year}-${now.month.toString().padLeft(2, '0')}';

    final updatedFuture = await db.update(
      'meters',
      {'first_billing_month': fallbackMonth},
      where: 'first_billing_month >= ?',
      whereArgs: [currentMonth],
    );

    print('Fixed FUTURE first_billing_month: $updatedFuture');

    print('===== FIX METERS END =====');
  }

  Future<void> debugCheckMetersForMonth(String selectedMonth) async {
    final db = await DatabaseHelper.instance.database;

    print('===== DEBUG METERS START =====');
    print('selectedMonth = $selectedMonth');

    final rows = await db.rawQuery('''
    SELECT meter_id, status, first_billing_month
    FROM meters
    ORDER BY meter_id
  ''');

    final validMeters = <String>[];
    final invalidMeters = <String>[];

    for (final r in rows) {
      final meterId = r['meter_id']?.toString() ?? '';
      final status = r['status']?.toString();
      final fbm = r['first_billing_month']?.toString();

      String reason = '';

      if (status != 'active') {
        reason = '❌ status != active ($status)';
      } else if (fbm == null || fbm.isEmpty) {
        reason = '❌ first_billing_month = NULL';
      } else if (fbm.compareTo(selectedMonth) >= 0) {
        reason = '❌ first_billing_month >= selectedMonth ($fbm)';
      }

      if (reason.isEmpty) {
        validMeters.add(meterId);
      } else {
        invalidMeters.add('$meterId → $reason');
      }
    }

    print('----- VALID METERS (should require prev month) -----');
    for (final m in validMeters) {
      print('✅ $m');
    }

    print('TOTAL VALID = ${validMeters.length}');

    print('----- INVALID METERS (NOT counted) -----');
    for (final m in invalidMeters) {
      print(m);
    }

    print('TOTAL INVALID = ${invalidMeters.length}');
    print('===== DEBUG METERS END =====');
  }

  /// kiểm tra dữ liệu tháng trước của ALL meters
  Future<void> _checkBeforeOpenScan() async {
    final String selectedMonth = await _getSelectedMonth();
    print('HOME selectedMonth = $selectedMonth');
    await debugCheckMetersForMonth('2026-04');
    await _pullService.ensureAllMetersHavePreviousMonth(
      selectedMonth: selectedMonth,
    );
  }

  Future<String> _getSelectedMonth() async {
    final now = DateTime.now();
    final mm = now.month.toString().padLeft(2, '0');
    return '${now.year}-$mm';
  }

  Future<void> _onTap(int i) async {
    if (i != 1) {
      setState(() => _index = i);
      return;
    }

    if (_checking) return;

    setState(() => _checking = true);

    try {
      await _checkBeforeOpenScan();

      if (!mounted) return;

      setState(() => _index = i);
    } catch (e) {
      if (!mounted) return;

      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text('Thiếu dữ liệu tháng trước. Không thể mở Scan.'),
        ),
      );
    } finally {
      if (mounted) {
        setState(() => _checking = false);
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Stack(
        children: [
          SafeArea(child: _pages[_index]),

          if (_checking)
            Container(
              color: Colors.black26,
              child: const Center(child: CircularProgressIndicator()),
            ),
        ],
      ),

      bottomNavigationBar: BottomNavigationBar(
        type: BottomNavigationBarType.fixed,
        currentIndex: _index,
        onTap: _onTap,

        backgroundColor: const Color(0xFF0D47A1),
        selectedItemColor: const Color(0xFFFFEB3B),
        unselectedItemColor: Colors.white70,

        selectedLabelStyle: const TextStyle(fontWeight: FontWeight.bold),

        showUnselectedLabels: true,
        selectedFontSize: 12,
        unselectedFontSize: 12,

        items: const [
          BottomNavigationBarItem(icon: Icon(Icons.home), label: 'Home'),
          BottomNavigationBarItem(
            icon: Icon(Icons.qr_code_scanner),
            label: 'Scan',
          ),
          BottomNavigationBarItem(
            icon: Icon(Icons.settings),
            label: 'Settings',
          ),
        ],
      ),
    );
  }
}
