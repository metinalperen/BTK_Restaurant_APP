import 'dart:convert';
import 'dart:io';
import '../config/app_config.dart';
import '../models/user.dart';
import '../services/auth_service.dart';

class UserService {
  // Authorization header'ı hazırla
  static Map<String, String> _getAuthHeaders() {
    final headers = <String, String>{
      'Content-Type': 'application/json',
    };
    
    // Token varsa Authorization header ekle
    final token = AuthService.authToken;
    if (token != null && token.isNotEmpty) {
      headers['Authorization'] = 'Bearer $token';
    }
    
    return headers;
  }

  // Tüm kullanıcıları getir
  static Future<List<User>> getAllUsers() async {
    try {
      // HttpClient oluştur (sertifika doğrulama devre dışı)
      final client = HttpClient();
      client.badCertificateCallback = (cert, host, port) => true;
      
      final request = await client.getUrl(Uri.parse('${AppConfig.baseUrl}${AppConfig.usersEndpoint}'));
      
      // Authorization header'ları ekle
      final headers = _getAuthHeaders();
      headers.forEach((key, value) {
        request.headers.set(key, value);
      });
      
      final response = await request.close();
      final responseBody = await response.transform(utf8.decoder).join();
      
      client.close();
      
      if (response.statusCode == 200) {
        try {
          final List<dynamic> jsonList = jsonDecode(responseBody);
          return jsonList.map((json) => User.fromJson(json)).toList();
        } catch (e) {
          throw Exception('Veri parse hatası: $e');
        }
      } else if (response.statusCode == 401) {
        throw Exception('Yetkilendirme hatası: Lütfen tekrar giriş yapın');
      } else {
        throw Exception('HTTP ${response.statusCode}: $responseBody');
      }
    } catch (e) {
      throw Exception('Bağlantı hatası: $e');
    }
  }

  // Kullanıcı ekle
  static Future<User> createUser({
    required String name,
    required String email,
    required String password,
    String? phoneNumber,
    required String roleName,
  }) async {
    try {
      final client = HttpClient();
      client.badCertificateCallback = (cert, host, port) => true;
      
      final request = await client.postUrl(Uri.parse('${AppConfig.baseUrl}${AppConfig.usersEndpoint}'));
      
      // Authorization header'ları ekle
      final headers = _getAuthHeaders();
      headers.forEach((key, value) {
        request.headers.set(key, value);
      });
      
      // Backend'e gönderilecek veri formatı
      final requestData = {
        'name': name,
        'email': email,
        'password': password,
        'phoneNumber': phoneNumber,
        'roleName': roleName,
      };
      
      request.write(jsonEncode(requestData));
      
      final response = await request.close();
      final responseBody = await response.transform(utf8.decoder).join();
      
      client.close();
      
      if (response.statusCode == 201 || response.statusCode == 200) {
        try {
          final json = jsonDecode(responseBody);
          return User.fromJson(json);
        } catch (e) {
          throw Exception('Veri parse hatası: $e');
        }
      } else if (response.statusCode == 401) {
        throw Exception('Yetkilendirme hatası: Lütfen tekrar giriş yapın');
      } else {
        throw Exception('HTTP ${response.statusCode}: $responseBody');
      }
    } catch (e) {
      throw Exception('Bağlantı hatası: $e');
    }
  }

  // Kullanıcı güncelle
  static Future<User> updateUser({
    required int id,
    String? name,
    String? email,
    String? phoneNumber,
    String? roleName,
    bool? isActive,
  }) async {
    try {
      final client = HttpClient();
      client.badCertificateCallback = (cert, host, port) => true;
      
      final request = await client.putUrl(Uri.parse('${AppConfig.baseUrl}${AppConfig.usersEndpoint}/$id'));
      
      // Authorization header'ları ekle
      final headers = _getAuthHeaders();
      headers.forEach((key, value) {
        request.headers.set(key, value);
      });
      
      final updateData = <String, dynamic>{};
      if (name != null) updateData['name'] = name;
      if (email != null) updateData['email'] = email;
      if (phoneNumber != null) updateData['phoneNumber'] = phoneNumber;
      if (roleName != null) updateData['roleName'] = roleName;
      if (isActive != null) updateData['isActive'] = isActive;
      
      request.write(jsonEncode(updateData));
      
      final response = await request.close();
      final responseBody = await response.transform(utf8.decoder).join();
      
      client.close();
      
      if (response.statusCode == 200) {
        try {
          final json = jsonDecode(responseBody);
          return User.fromJson(json);
        } catch (e) {
          throw Exception('Veri parse hatası: $e');
        }
      } else if (response.statusCode == 401) {
        throw Exception('Yetkilendirme hatası: Lütfen tekrar giriş yapın');
      } else {
        throw Exception('HTTP ${response.statusCode}: $responseBody');
      }
    } catch (e) {
      throw Exception('Bağlantı hatası: $e');
    }
  }

  // Kullanıcı sil
  static Future<bool> deleteUser(int id) async {
    try {
      final client = HttpClient();
      client.badCertificateCallback = (cert, host, port) => true;
      
      final request = await client.deleteUrl(Uri.parse('${AppConfig.baseUrl}${AppConfig.usersEndpoint}/$id'));
      
      // Authorization header'ları ekle
      final headers = _getAuthHeaders();
      headers.forEach((key, value) {
        request.headers.set(key, value);
      });
      
      final response = await request.close();
      
      client.close();
      
      if (response.statusCode == 401) {
        throw Exception('Yetkilendirme hatası: Lütfen tekrar giriş yapın');
      }
      
      return response.statusCode == 200 || response.statusCode == 204;
    } catch (e) {
      throw Exception('Bağlantı hatası: $e');
    }
  }

  // Role göre kullanıcıları getir
  static Future<List<User>> getUsersByRole(int roleId) async {
    try {
      final allUsers = await getAllUsers();
      return allUsers.where((user) => user.roles.contains(roleId)).toList();
    } catch (e) {
      throw Exception('Role göre filtreleme hatası: $e');
    }
  }

  // Aktif kullanıcıları getir
  static Future<List<User>> getActiveUsers() async {
    try {
      final allUsers = await getAllUsers();
      return allUsers.where((user) => user.isActive).toList();
    } catch (e) {
      throw Exception('Aktif kullanıcı filtreleme hatası: $e');
    }
  }

  // Pasif kullanıcıları getir
  static Future<List<User>> getInactiveUsers() async {
    try {
      final allUsers = await getAllUsers();
      return allUsers.where((user) => !user.isActive).toList();
    } catch (e) {
      throw Exception('Pasif kullanıcı filtreleme hatası: $e');
    }
  }

  // Kullanıcı aktiflik durumunu güncelle
  static Future<User> updateUserActiveStatus(int id, bool isActive) async {
    try {
      final client = HttpClient();
      client.badCertificateCallback = (cert, host, port) => true;
      
      // Alternatif 1: Query parameter ile dene
      final url = '${AppConfig.baseUrl}${AppConfig.usersActiveEndpoint.replaceAll('{id}', id.toString())}?active=$isActive';
      print('🔍 DEBUG: PATCH URL with query param: $url');
      
      final request = await client.patchUrl(Uri.parse(url));
      
      // Authorization header'ları ekle
      final headers = _getAuthHeaders();
      headers.forEach((key, value) {
        request.headers.set(key, value);
      });
      
      // Debug: Header'ları yazdır
      print('🔍 DEBUG: Headers: $headers');
      
      // Alternatif 2: Boş body gönder (query parameter kullanıyoruz)
      request.write('{}');
      
      final response = await request.close();
      final responseBody = await response.transform(utf8.decoder).join();
      
      print('🔍 DEBUG: Response Status: ${response.statusCode}');
      print('🔍 DEBUG: Response Body: $responseBody');
      
      client.close();
      
      if (response.statusCode == 200) {
        try {
          // Backend'den güncellenmiş user bilgisi dönüyor
          final json = jsonDecode(responseBody);
          return User.fromJson(json);
        } catch (e) {
          throw Exception('Veri parse hatası: $e');
        }
      } else if (response.statusCode == 400) {
        // 400 hatası - parametre sorunu, alternatif yaklaşım dene
        throw Exception('Parametre hatası: Backend "active" parametresini bulamıyor. Response: $responseBody');
      } else if (response.statusCode == 401) {
        throw Exception('Yetkilendirme hatası: Lütfen tekrar giriş yapın');
      } else {
        throw Exception('HTTP ${response.statusCode}: $responseBody');
      }
    } catch (e) {
      throw Exception('Bağlantı hatası: $e');
    }
  }

  // Kullanıcı telefon numarasını güncelle
  static Future<bool> updateUserPhone(int id, String phoneNumber) async {
    try {
      final client = HttpClient();
      client.badCertificateCallback = (cert, host, port) => true;
      
      final request = await client.patchUrl(Uri.parse('${AppConfig.baseUrl}${AppConfig.usersEndpoint}/$id/phone'));
      
      // Authorization header'ları ekle
      final headers = _getAuthHeaders();
      headers.forEach((key, value) {
        request.headers.set(key, value);
      });
      
      request.write(jsonEncode({'phoneNumber': phoneNumber}));
      
      final response = await request.close();
      
      client.close();
      
      if (response.statusCode == 401) {
        throw Exception('Yetkilendirme hatası: Lütfen tekrar giriş yapın');
      }
      
      return response.statusCode == 200;
    } catch (e) {
      throw Exception('Bağlantı hatası: $e');
    }
  }

  // Kullanıcı fotoğrafını güncelle
  static Future<User> updateUserPhoto(int id, File photoFile) async {
    try {
      final client = HttpClient();
      client.badCertificateCallback = (cert, host, port) => true;
      
      final url = '${AppConfig.baseUrl}${AppConfig.usersPhotoEndpoint.replaceAll('{id}', id.toString())}';
      print('🔍 DEBUG: Photo Upload URL: $url');
      
      final request = await client.postUrl(Uri.parse(url));
      
      // Authorization header'ı ekle (Content-Type header'ı ekleme, multipart için)
      final token = AuthService.authToken;
      if (token != null && token.isNotEmpty) {
        request.headers.set('Authorization', 'Bearer $token');
      }
      
      // Multipart boundary oluştur
      final boundary = '----WebKitFormBoundary${DateTime.now().millisecondsSinceEpoch}';
      request.headers.set('Content-Type', 'multipart/form-data; boundary=$boundary');
      
      // Fotoğraf dosyasını multipart olarak gönder
      final photoBytes = await photoFile.readAsBytes();
      final photoName = photoFile.path.split('/').last;
      
      final multipartData = StringBuffer();
      multipartData.writeln('--$boundary');
      multipartData.writeln('Content-Disposition: form-data; name="photo"; filename="$photoName"');
      multipartData.writeln('Content-Type: image/jpeg');
      multipartData.writeln();
      multipartData.write(String.fromCharCodes(photoBytes));
      multipartData.writeln();
      multipartData.writeln('--$boundary--');
      
      print('🔍 DEBUG: Photo file: $photoName, Size: ${photoBytes.length} bytes');
      
      request.write(multipartData.toString());
      
      final response = await request.close();
      final responseBody = await response.transform(utf8.decoder).join();
      
      print('🔍 DEBUG: Photo Upload Response Status: ${response.statusCode}');
      print('🔍 DEBUG: Photo Upload Response Body: $responseBody');
      
      client.close();
      
      if (response.statusCode == 200) {
        try {
          // Backend'den güncellenmiş user bilgisi dönüyor
          final json = jsonDecode(responseBody);
          return User.fromJson(json);
        } catch (e) {
          throw Exception('Veri parse hatası: $e');
        }
      } else if (response.statusCode == 400) {
        throw Exception('Fotoğraf upload hatası: $responseBody');
      } else if (response.statusCode == 401) {
        throw Exception('Yetkilendirme hatası: Lütfen tekrar giriş yapın');
      } else {
        throw Exception('HTTP ${response.statusCode}: $responseBody');
      }
    } catch (e) {
      throw Exception('Fotoğraf upload hatası: $e');
    }
  }
}
