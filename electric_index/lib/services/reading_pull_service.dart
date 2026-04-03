import 'package:electric_index/db/dao/reading_dao.dart';
import 'package:electric_index/services/reading_sync_api_service.dart';
import '../utils/month_helper.dart';

class ReadingPullService {
  final ReadingSyncApiService api;
  final ReadingDao dao;

  ReadingPullService({required this.api, required this.dao});

  Future<int> pullMonthToLocal(String month) async {
    final resp = await api.pullMonth(month);
    print('SERVER resp.count = ${resp.count}');
    print('SERVER resp.items.length = ${resp.items.length}');
    if (!resp.success) {
      throw Exception('Server pull month not success');
    }
    int count = 0;
    for (final item in resp.items) {
      print(
        'PULL item meter=${item.meterId}, month=${item.month}, prev=${item.indexPrevMonth}, last=${item.indexLastMonth}',
      );

      await dao.upsertServerReading(
        meterId: item.meterId,
        month: item.month,
        indexPrevMonth: item.indexPrevMonth,
        indexLastMonth: item.indexLastMonth,
        indexConsumption: item.indexConsumption,
        createdAt: item.createdAt,
      );
      count++;
    }
    print('TOTAL READINGS PULLED: $count');

    return resp.count;
  }

  Future<void> ensureAllMetersHavePreviousMonth({
    required String selectedMonth,
  }) async {
    print('===== ensureAllMetersHavePreviousMonth START =====');
    print('STEP 1: selectedMonth = $selectedMonth');

    final prevMonth = previousMonth(selectedMonth);
    print('STEP 2: prevMonth = $prevMonth');

    final totalRequiredMeters = await dao.countMetersRequiringPreviousMonth(
      selectedMonth,
    );

    print('STEP 3: totalRequiredMeters = $totalRequiredMeters');

    // Không có meter nào cần check previous month
    if (totalRequiredMeters == 0) {
      print('OK: No meters require previous month check');
      return;
    }

    final localCount = await dao
        .countMetersHavingOfficialReadingForMonthRequiredOnly(
          selectedMonth: selectedMonth,
          prevMonth: prevMonth,
        );

    print('STEP 4: localCount = $localCount');

    if (localCount >= totalRequiredMeters) {
      print('OK: Local data already complete');
      return;
    }

    print('STEP 5: Pulling data from server for month = $prevMonth');
    await pullMonthToLocal(prevMonth);

    final missingAfterPull = await dao.findMissingMeterIdsForMonth(
      selectedMonth: selectedMonth,
      prevMonth: prevMonth,
    );

    print('STEP 6: missingAfterPull = $missingAfterPull');

    if (missingAfterPull.isNotEmpty) {
      for (final meterId in missingAfterPull) {
        print('MISSING meterId=$meterId for prevMonth=$prevMonth');
      }

      print('STOP: Missing meters after pull');
      throw Exception(
        'Thiếu dữ liệu tháng trước ($prevMonth) cho ${missingAfterPull.length} đồng hồ: ${missingAfterPull.join(", ")}',
      );
    }

    print('SUCCESS: All required meters have previous month reading');
    print('===== ensureAllMetersHavePreviousMonth END =====');
  }
}
