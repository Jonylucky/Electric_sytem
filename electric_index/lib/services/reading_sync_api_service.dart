import 'dart:convert';
import 'package:http/http.dart' as http;

import '../services/api_config.dart';
import '../models/pull_month_response.dart';

class ReadingSyncApiService {
  Future<PullMonthResponse> pullMonth(String month) async {
    final uri = Uri.parse(
      '${ApiConfig.baseUrl}/api/v1/readings/pull-month',
    ).replace(queryParameters: {'month': month});

    final res = await http.get(
      uri,
      headers: {'Content-Type': 'application/json'},
    );

    if (res.statusCode != 200) {
      throw Exception('pullMonth failed: ${res.statusCode} ${res.body}');
    }

    final jsonMap = jsonDecode(res.body) as Map<String, dynamic>;

    return PullMonthResponse.fromJson(jsonMap);
  }
}
