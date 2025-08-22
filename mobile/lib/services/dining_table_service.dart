import 'dart:convert';
import 'dart:io';
import 'package:http/http.dart' as http;

class DiningTableService {
  static const String baseUrl = 'https://192.168.232.113:8080';
  
  // Tüm masaları getir
  static Future<List<Map<String, dynamic>>> getAllTables() async {
    try {
      HttpClient client = HttpClient()
        ..badCertificateCallback = (X509Certificate cert, String host, int port) => true;
      
      final request = await client.getUrl(Uri.parse('$baseUrl/api/dining-tables'));
      final response = await request.close();
      final responseBody = await response.transform(utf8.decoder).join();
      
      if (response.statusCode == 200) {
        final List<dynamic> data = jsonDecode(responseBody);
        return data.cast<Map<String, dynamic>>();
      } else {
        throw Exception('HTTP ${response.statusCode}: $responseBody');
      }
    } catch (e) {
      throw Exception('Bağlantı hatası: $e');
    }
  }
  
  // Yeni masa ekle
  static Future<Map<String, dynamic>> createTable(Map<String, dynamic> tableData) async {
    try {
      HttpClient client = HttpClient()
        ..badCertificateCallback = (X509Certificate cert, String host, int port) => true;
      
      final request = await client.postUrl(Uri.parse('$baseUrl/api/dining-tables'));
      request.headers.set('Content-Type', 'application/json');
      request.write(jsonEncode(tableData));
      
      final response = await request.close();
      final responseBody = await response.transform(utf8.decoder).join();
      
      if (response.statusCode == 201 || response.statusCode == 200) {
        return jsonDecode(responseBody);
      } else {
        throw Exception('HTTP ${response.statusCode}: $responseBody');
      }
    } catch (e) {
      throw Exception('Bağlantı hatası: $e');
    }
  }
  
  // Masa güncelle
  static Future<Map<String, dynamic>> updateTable(int tableId, Map<String, dynamic> tableData) async {
    try {
      HttpClient client = HttpClient()
        ..badCertificateCallback = (X509Certificate cert, String host, int port) => true;
      
      final request = await client.putUrl(Uri.parse('$baseUrl/api/dining-tables/$tableId'));
      request.headers.set('Content-Type', 'application/json');
      request.write(jsonEncode(tableData));
      
      final response = await request.close();
      final responseBody = await response.transform(utf8.decoder).join();
      
      if (response.statusCode == 200) {
        return jsonDecode(responseBody);
      } else {
        throw Exception('HTTP ${response.statusCode}: $responseBody');
      }
    } catch (e) {
      throw Exception('Bağlantı hatası: $e');
    }
  }
  
  // Masa sil
  static Future<bool> deleteTable(int tableId) async {
    try {
      HttpClient client = HttpClient()
        ..badCertificateCallback = (X509Certificate cert, String host, int port) => true;
      
      final request = await client.deleteUrl(Uri.parse('$baseUrl/api/dining-tables/$tableId'));
      final response = await request.close();
      
      return response.statusCode == 200 || response.statusCode == 204;
    } catch (e) {
      throw Exception('Bağlantı hatası: $e');
    }
  }
}
