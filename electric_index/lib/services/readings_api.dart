import 'dart:io';
import 'package:dio/dio.dart';
import '../models/sync_payload.dart';
import '../models/sync_month_result.dart';
import 'dio_client.dart';

class ReadingsApi {
  final Dio _dio;
  ReadingsApi(DioClient client) : _dio = client.dio;

  /// Phase 1: JSON only - tạo/update record, trả readingId + meterId+month
  Future<SyncMonthResult> syncMonthData(List<SyncPayload> items) async {
    final res = await _dio.post(
      '/api/v1/readings/sync-month-data',
      data: items.map((e) => e.toJson()).toList(),
      options: Options(
        contentType: Headers.jsonContentType,
        responseType: ResponseType.json,
        connectTimeout: const Duration(seconds: 30),
        receiveTimeout: const Duration(minutes: 2),
      ),
    );
    return SyncMonthResult.fromJson(Map<String, dynamic>.from(res.data as Map));
  }

  /// Phase 2: upload 1 zip (<=10MB) - server unzip & update image_url by imageKey
  Future<SyncMonthResult> uploadImagesZip({
    required File zipFile,
    required int batch,
  }) async {
    print('==============================');
    print('🚀 uploadImagesZip START');
    print('batch=$batch');
    print('zipPath=${zipFile.path}');

    final exists = await zipFile.exists();
    print('zipExists=$exists');
    if (!exists) {
      throw Exception('ZIP file không tồn tại: ${zipFile.path}');
    }

    final size = await zipFile.length();
    print('zipSize=$size bytes');

    final form = FormData.fromMap({
      'file': await MultipartFile.fromFile(
        zipFile.path,
        filename: zipFile.uri.pathSegments.last,
      ),
      'batch': batch.toString(),
    });

    try {
      final res = await _dio.post(
        '/api/v1/readings/records/images',
        data: form,
        options: Options(
          contentType: 'multipart/form-data',
          responseType: ResponseType.json,

          sendTimeout: const Duration(minutes: 5),
          receiveTimeout: const Duration(minutes: 5),
        ),
        onSendProgress: (sent, total) {
          print('⬆️ batch=$batch sent=$sent / $total');
        },
      );

      print('✅ uploadImagesZip RESPONSE status=${res.statusCode}');
      return SyncMonthResult.fromJson(
        Map<String, dynamic>.from(res.data as Map),
      );
    } catch (e, s) {
      print('❌ uploadImagesZip ERROR');
      print(e);
      print(s);
      rethrow;
    }
  }
}
