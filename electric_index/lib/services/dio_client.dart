import 'package:dio/dio.dart';
import 'api_config.dart';

class DioClient {
  final Dio dio;

  DioClient({String? baseUrl})
    : dio = Dio(
        BaseOptions(
          baseUrl: baseUrl ?? ApiConfig.baseUrl,
          connectTimeout: const Duration(seconds: 15),
          receiveTimeout: const Duration(seconds: 120),
          headers: {'Accept': 'application/json'},
        ),
      );
}
