import 'package:shared_preferences/shared_preferences.dart';

class ApiConfig {
  static const String defaultBaseUrl = 'http://192.168.10.111:8081';
  static const String _prefKey = 'api_base_url';

  static String _baseUrl = defaultBaseUrl;

  static String get baseUrl => _baseUrl;

  static Future<void> init() async {
    final prefs = await SharedPreferences.getInstance();
    _baseUrl = prefs.getString(_prefKey) ?? defaultBaseUrl;
  }

  static Future<void> setBaseUrl(String url) async {
    final normalized = _normalize(url);
    _baseUrl = normalized;
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString(_prefKey, normalized);
  }

  static String _normalize(String url) {
    var value = url.trim();
    if (value.isEmpty) {
      throw ArgumentError('URL không được để trống');
    }
    if (!value.startsWith('http://') && !value.startsWith('https://')) {
      value = 'http://$value:8081';
    }
    while (value.endsWith('/')) {
      value = value.substring(0, value.length - 1);
    }
    return value;
  }
}
