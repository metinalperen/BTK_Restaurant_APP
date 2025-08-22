import 'dart:convert';
import 'dart:io';
import '../config/app_config.dart';

class AuthService {
  // Token'ı sakla
  static String? _authToken;
  
  // Token getter
  static String? get authToken => _authToken;
  
  // Token setter
  static void setAuthToken(String token) {
    _authToken = token;
  }
  
  // Token'ı temizle (logout için)
  static void clearAuthToken() {
    _authToken = null;
  }

  static Future<Map<String, dynamic>> login(String email, String password) async {
    try {
      // HttpClient oluştur (sertifika doğrulama devre dışı)
      final client = HttpClient();
      client.badCertificateCallback = (cert, host, port) => true;
      
      final request = await client.postUrl(Uri.parse('${AppConfig.baseUrl}${AppConfig.loginEndpoint}'));
      request.headers.set('Content-Type', 'application/json');
      request.write(jsonEncode({
        'email': email,
        'password': password,
      }));
      
      final response = await request.close();
      final responseBody = await response.transform(utf8.decoder).join();
      
      client.close();
      
      if (response.statusCode == 200) {
        try {
          final data = jsonDecode(responseBody);
          
          // Token'ı sakla
          if (data['token'] != null) {
            setAuthToken(data['token']);
          }
          
          return data;
        } catch (e) {
          return {
            'success': false,
            'message': 'Veri parse hatası: $e',
          };
        }
      } else {
        // HTTP error durumunda
        try {
          final errorData = jsonDecode(responseBody);
          return {
            'success': false,
            'message': errorData['message'] ?? 'Giriş başarısız',
          };
        } catch (e) {
          return {
            'success': false,
            'message': 'HTTP ${response.statusCode}: $responseBody',
          };
        }
      }
    } catch (e) {
      // Network error durumunda
      return {
        'success': false,
        'message': 'Bağlantı hatası: $e',
      };
    }
  }

  static Future<Map<String, dynamic>> forgotPassword(String email) async {
    try {
      // HttpClient oluştur (sertifika doğrulama devre dışı)
      final client = HttpClient();
      client.badCertificateCallback = (cert, host, port) => true;
      
      final request = await client.postUrl(Uri.parse('${AppConfig.baseUrl}${AppConfig.forgotPasswordEndpoint}'));
      request.headers.set('Content-Type', 'application/json');
      request.write(jsonEncode({
        'email': email,
      }));
      
      final response = await request.close();
      final responseBody = await response.transform(utf8.decoder).join();
      
      client.close();
      
      if (response.statusCode == 200) {
        try {
          final data = jsonDecode(responseBody);
          return {
            'success': true,
            'message': data['message'] ?? 'Şifre sıfırlama bağlantısı gönderildi',
            'data': data,
          };
        } catch (e) {
          return {
            'success': true,
            'message': 'Şifre sıfırlama bağlantısı gönderildi',
            'data': responseBody,
          };
        }
      } else {
        // HTTP error durumunda
        try {
          final errorData = jsonDecode(responseBody);
          return {
            'success': false,
            'message': errorData['message'] ?? 'Şifre sıfırlama başarısız',
          };
        } catch (e) {
          return {
            'success': false,
            'message': 'HTTP ${response.statusCode}: $responseBody',
          };
        }
      }
    } catch (e) {
      // Network error durumunda
      return {
        'success': false,
        'message': 'Bağlantı hatası: $e',
      };
    }
  }
}
