import 'package:flutter/material.dart';
import 'package:electric_index/screens/company/company_list_screen.dart';
import 'package:electric_index/screens/locations/location_screen.dart';
import 'package:electric_index/screens/meter/meter_from_screen.dart';
import 'package:electric_index/screens/reading/readings_month_list_screen.dart';

import 'package:share_plus/share_plus.dart';
import '../../db/dao/company_dao.dart';
import '../../db/dao/meter_dao.dart';
import '../../services/all_companies_qr_excel_exporter.dart';
import '../../services/image_compress_service.dart';
import '../../services/month_sync_two_phase.dart';
import '../../services/zip_service.dart';
import '../../services/dio_client.dart';
import '../../services/readings_api.dart';
import '../../db/dao/reading_dao.dart';

class HomeDashboardScreen extends StatefulWidget {
  const HomeDashboardScreen({super.key});

  @override
  State<HomeDashboardScreen> createState() => _HomeDashboardScreenState();
}

class _HomeDashboardScreenState extends State<HomeDashboardScreen> {
  final TextEditingController monthController = TextEditingController(
    text: '2026-02',
  );

  @override
  void dispose() {
    monthController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFFE3F2FD),
      appBar: AppBar(
        title: const Text('Electric Index'),
        backgroundColor: const Color(0xFF1565C0),
        foregroundColor: Colors.white,
        elevation: 0,
      ),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          const _ActionCard(
            icon: Icons.apartment,
            title: 'List Company',
            subtitle: 'Tạo công ty mới',
            routeType: 1,
          ),
          const SizedBox(height: 14),
          const _ActionCard(
            icon: Icons.receipt_long,
            title: 'List Readings',
            subtitle: 'Xem lịch sử ghi điện',
            routeType: 2,
          ),
          const SizedBox(height: 14),
          const _ActionCard(
            icon: Icons.location_on,
            title: 'Locations',
            subtitle: 'Tạo / quản lý location',
            routeType: 3,
          ),
          const SizedBox(height: 14),
          const _ActionCard(
            icon: Icons.electric_meter,
            title: 'Meters',
            subtitle: 'Tạo / sửa / xóa meter',
            routeType: 4,
          ),
          const SizedBox(height: 14),
          const _ActionCard(
            icon: Icons.share,
            title: 'Export ALL QR',
            subtitle: 'Xuất Excel QR cho tất cả company',
            routeType: 5,
          ),

          const SizedBox(height: 14),

          TextField(
            controller: monthController,
            decoration: const InputDecoration(
              labelText: 'Month (YYYY-MM)',
              border: OutlineInputBorder(),
            ),
          ),

          const SizedBox(height: 14),

          _SyncActionCard(monthController: monthController),
        ],
      ),
    );
  }
}

class _ActionCard extends StatelessWidget {
  final IconData icon;
  final String title;
  final String subtitle;
  final int routeType;

  const _ActionCard({
    required this.icon,
    required this.title,
    required this.subtitle,
    required this.routeType,
  });

  @override
  Widget build(BuildContext context) {
    return Card(
      elevation: 3,
      shadowColor: Colors.blue.withOpacity(0.15),
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(18)),
      child: ListTile(
        contentPadding: const EdgeInsets.symmetric(
          horizontal: 18,
          vertical: 14,
        ),

        leading: Container(
          padding: const EdgeInsets.all(10),
          decoration: BoxDecoration(
            color: const Color(0xFFBBDEFB), // vòng xanh nhạt
            borderRadius: BorderRadius.circular(14),
          ),
          child: Icon(icon, size: 26, color: const Color(0xFF1565C0)),
        ),

        title: Text(
          title,
          style: const TextStyle(
            fontWeight: FontWeight.w700,
            fontSize: 16,
            color: Color(0xFF0D47A1),
          ),
        ),

        subtitle: subtitle.isEmpty
            ? null
            : Text(subtitle, style: const TextStyle(color: Colors.black54)),

        trailing: const Icon(Icons.chevron_right, color: Colors.blueGrey),

        onTap: () async {
          switch (routeType) {
            case 1:
              await Navigator.push(
                context,
                MaterialPageRoute(builder: (_) => const CompanyListScreen()),
              );
              break;
            case 2:
              await Navigator.push(
                context,
                MaterialPageRoute(
                  builder: (_) => const ReadingsMonthListScreen(),
                ),
              );
              break;
            case 3:
              await Navigator.push(
                context,
                MaterialPageRoute(builder: (_) => const LocationScreen()),
              );
              break;
            case 4:
              await Navigator.push(
                context,
                MaterialPageRoute(builder: (_) => const ListMeterScreen()),
              );
              break;
            case 5:
              try {
                final exporter = AllCompaniesQrExcelExporter(
                  companyDao: CompanyDao(),
                  meterDao: MeterDao(),
                );

                final filePath = await exporter.exportAllCompaniesQrExcel();

                await Share.shareXFiles(
                  [XFile(filePath)],
                  text: 'QR Excel - All Companies',
                  subject: 'QR Excel - All Companies',
                );
              } catch (e) {
                if (!context.mounted) return;
                ScaffoldMessenger.of(
                  context,
                ).showSnackBar(SnackBar(content: Text('Export/Share lỗi: $e')));
              }
              break;
            case 6:
              final monthController = TextEditingController(text: '2026-02');

              await showDialog(
                context: context,
                builder: (dialogContext) {
                  return AlertDialog(
                    title: const Text('Nhập tháng sync'),
                    content: TextField(
                      controller: monthController,
                      decoration: const InputDecoration(
                        labelText: 'Month (YYYY-MM)',
                        border: OutlineInputBorder(),
                      ),
                    ),
                    actions: [
                      TextButton(
                        onPressed: () => Navigator.pop(dialogContext),
                        child: const Text('Cancel'),
                      ),
                      ElevatedButton(
                        onPressed: () async {
                          try {
                            final month = monthController.text.trim();

                            final api = ReadingsApi(DioClient());
                            final service = MonthSyncTwoPhase(
                              api: api,
                              compress: ImageCompressService(),
                              zip: ZipService(),
                            );

                            final items = await ReadingDao()
                                .getReadingsToSyncByMonth(month);

                            final result = await service.sync(
                              month: month,
                              items: items,
                              maxZipBytes: 10 * 1024 * 1024,
                              compressQuality: 80,
                              compressMinSide: 1600,
                            );

                            if (dialogContext.mounted) {
                              Navigator.pop(dialogContext);
                            }

                            if (!context.mounted) return;
                            ScaffoldMessenger.of(context).showSnackBar(
                              SnackBar(
                                content: Text(
                                  result.success ? 'Sync OK' : 'Sync FAIL',
                                ),
                              ),
                            );
                          } catch (e) {
                            if (!context.mounted) return;
                            ScaffoldMessenger.of(context).showSnackBar(
                              SnackBar(content: Text('Sync lỗi: $e')),
                            );
                          }
                        },
                        child: const Text('SYNC'),
                      ),
                    ],
                  );
                },
              );
              break;
          }
        },
      ),
    );
  }
}

class _SyncActionCard extends StatelessWidget {
  final TextEditingController monthController;

  const _SyncActionCard({required this.monthController});

  @override
  Widget build(BuildContext context) {
    return Card(
      elevation: 3,
      shadowColor: Colors.blue.withOpacity(0.15),
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(18)),
      child: ListTile(
        contentPadding: const EdgeInsets.symmetric(
          horizontal: 18,
          vertical: 14,
        ),
        leading: Container(
          padding: const EdgeInsets.all(10),
          decoration: BoxDecoration(
            color: const Color(0xFFBBDEFB),
            borderRadius: BorderRadius.circular(14),
          ),
          child: const Icon(
            Icons.cloud_upload,
            size: 26,
            color: Color(0xFF1565C0),
          ),
        ),
        title: const Text(
          'Sync API',
          style: TextStyle(
            fontWeight: FontWeight.w700,
            fontSize: 16,
            color: Color(0xFF0D47A1),
          ),
        ),
        subtitle: const Text(
          'Upload readings lên server',
          style: TextStyle(color: Colors.black54),
        ),
        trailing: const Icon(Icons.chevron_right, color: Colors.blueGrey),
        onTap: () async {
          try {
            final month = monthController.text.trim();

            final api = ReadingsApi(DioClient());
            final service = MonthSyncTwoPhase(
              api: api,
              compress: ImageCompressService(),
              zip: ZipService(),
            );

            final items = await ReadingDao().getReadingsToSyncByMonth(month);

            final result = await service.sync(
              month: month,
              items: items,
              maxZipBytes: 10 * 1024 * 1024,
              compressQuality: 80,
              compressMinSide: 1600,
            );

            print(
              'Sync result success=${result.success} count=${result.count}',
            );

            if (!context.mounted) return;
            ScaffoldMessenger.of(context).showSnackBar(
              SnackBar(content: Text(result.success ? 'Sync OK' : 'Sync FAIL')),
            );
          } catch (e) {
            if (!context.mounted) return;
            ScaffoldMessenger.of(
              context,
            ).showSnackBar(SnackBar(content: Text('Sync lỗi: $e')));
          }
        },
      ),
    );
  }
}
