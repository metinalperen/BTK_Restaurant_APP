import 'dart:convert';
import 'dart:io';
import '../config/app_config.dart';
import '../models/top_product.dart';
import '../models/top_products_summary.dart';
import '../models/category_sales.dart';
import '../models/revenue.dart';
import '../models/performance_metrics.dart';
import '../models/employee_performance.dart';
import '../models/summary_status.dart';
import '../services/auth_service.dart';

class AnalyticsService {
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

  // En çok satan ürünleri getir
  static Future<List<TopProduct>> getTopProducts() async {
    try {
      // HttpClient oluştur (sertifika doğrulama devre dışı)
      final client = HttpClient();
      client.badCertificateCallback = (cert, host, port) => true;
      
      final request = await client.getUrl(Uri.parse('${AppConfig.baseUrl}${AppConfig.analyticsTopProductsEndpoint}'));
      
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
          return jsonList.map((json) => TopProduct.fromJson(json)).toList();
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

  // Ciro analitiklerini getir
  static Future<Revenue> getRevenue() async {
    try {
      // HttpClient oluştur (sertifika doğrulama devre dışı)
      final client = HttpClient();
      client.badCertificateCallback = (cert, host, port) => true;
      
      final request = await client.getUrl(Uri.parse('${AppConfig.baseUrl}${AppConfig.analyticsRevenueEndpoint}'));
      
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
          final Map<String, dynamic> jsonData = jsonDecode(responseBody);
          return Revenue.fromJson(jsonData);
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

  // Performans metriklerini getir
  static Future<PerformanceMetrics> getPerformanceMetrics() async {
    try {
      // HttpClient oluştur (sertifika doğrulama devre dışı)
      final client = HttpClient();
      client.badCertificateCallback = (cert, host, port) => true;
      
      final request = await client.getUrl(Uri.parse('${AppConfig.baseUrl}${AppConfig.analyticsPerformanceMetricsEndpoint}'));
      
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
          final Map<String, dynamic> jsonData = jsonDecode(responseBody);
          return PerformanceMetrics.fromJson(jsonData);
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

  // Çalışan performansını getir
  static Future<EmployeePerformance> getEmployeePerformance() async {
    try {
      // HttpClient oluştur (sertifika doğrulama devre dışı)
      final client = HttpClient();
      client.badCertificateCallback = (cert, host, port) => true;
      
      final request = await client.getUrl(Uri.parse('${AppConfig.baseUrl}${AppConfig.analyticsEmployeePerformanceEndpoint}'));
      
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
          final Map<String, dynamic> jsonData = jsonDecode(responseBody);
          return EmployeePerformance.fromJson(jsonData);
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

  // Özet durumunu kontrol et
  static Future<SummaryStatus> getSummaryStatus() async {
    try {
      // HttpClient oluştur (sertifika doğrulama devre dışı)
      final client = HttpClient();
      client.badCertificateCallback = (cert, host, port) => true;
      
      final request = await client.getUrl(Uri.parse('${AppConfig.baseUrl}${AppConfig.analyticsSummaryStatusEndpoint}'));
      
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
          final Map<String, dynamic> jsonData = jsonDecode(responseBody);
          return SummaryStatus.fromJson(jsonData);
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

  // Kategori bazında satış verilerini getir
  static Future<CategorySales> getCategorySales() async {
    try {
      // HttpClient oluştur (sertifika doğrulama devre dışı)
      final client = HttpClient();
      client.badCertificateCallback = (cert, host, port) => true;
      
      final request = await client.getUrl(Uri.parse('${AppConfig.baseUrl}${AppConfig.analyticsCategorySalesEndpoint}'));
      
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
          final Map<String, dynamic> jsonData = jsonDecode(responseBody);
          return CategorySales.fromJson(jsonData);
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

  // Belirli bir dönem için en çok satan ürünleri getir
  static Future<List<TopProduct>> getTopProductsByPeriod(String period) async {
    try {
      final client = HttpClient();
      client.badCertificateCallback = (cert, host, port) => true;
      
      final url = '${AppConfig.baseUrl}${AppConfig.analyticsTopProductsEndpoint}/$period';
      final request = await client.getUrl(Uri.parse(url));
      
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
          return jsonList.map((json) => TopProduct.fromJson(json)).toList();
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

  // Tüm dönemler için en çok satan ürünleri getir (SUMMARY)
  static Future<TopProductsSummary> getTopProductsSummary() async {
    try {
      final client = HttpClient();
      client.badCertificateCallback = (cert, host, port) => true;
      
      final url = '${AppConfig.baseUrl}${AppConfig.analyticsTopProductsSummaryEndpoint}';
      final request = await client.getUrl(Uri.parse(url));
      
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
          final Map<String, dynamic> jsonData = jsonDecode(responseBody);
          return TopProductsSummary.fromJson(jsonData);
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

  // Belirli tarih aralığı için satış raporu oluştur - DÜZELTİLDİ: POST method kullanılıyor
  static Future<Map<String, dynamic>?> getDateRangeSalesSummary(String startDate, String endDate, String reportType) async {
    try {
      final client = HttpClient();
      client.badCertificateCallback = (cert, host, port) => true;
      
      final url = '${AppConfig.baseUrl}/daily-sales-summary/generate/date-range';
      final request = await client.postUrl(Uri.parse(url)); // ✅ GET yerine POST kullanılıyor
      
      // Authorization header'ları ekle
      final headers = _getAuthHeaders();
      headers.forEach((key, value) {
        request.headers.set(key, value);
      });
      
      // Request body'yi hazırla
      final requestBody = {
        'startDate': startDate,
        'endDate': endDate,
        'reportType': reportType,
      };
      
      // JSON body'yi gönder
      request.write(jsonEncode(requestBody));
      final response = await request.close();
      final responseBody = await response.transform(utf8.decoder).join();
      
      client.close();
      
      if (response.statusCode == 200) {
        try {
          final Map<String, dynamic> jsonData = jsonDecode(responseBody);
          return jsonData;
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

  // Tarih aralığına göre satış özetlerini getir
  static Future<List<Map<String, dynamic>>> getSalesSummariesByDateRange(String startDate, String endDate) async {
    try {
      final client = HttpClient();
      client.badCertificateCallback = (cert, host, port) => true;
      
      final url = '${AppConfig.baseUrl}/daily-sales-summary/date-range?startDate=$startDate&endDate=$endDate';
      final request = await client.getUrl(Uri.parse(url));
      
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
          return jsonList.cast<Map<String, dynamic>>();
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

  // Günlük satış özetlerini getir
  static Future<List<Map<String, dynamic>>> getAllDailySalesSummaries() async {
    try {
      final client = HttpClient();
      client.badCertificateCallback = (cert, host, port) => true;
      
      final url = '${AppConfig.baseUrl}/daily-sales-summary/daily';
      final request = await client.getUrl(Uri.parse(url));
      
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
          return jsonList.cast<Map<String, dynamic>>();
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

  // Belirli bir gün için satış özeti getir
  static Future<Map<String, dynamic>?> getDailySalesSummary(String date) async {
    try {
      final client = HttpClient();
      client.badCertificateCallback = (cert, host, port) => true;
      
      final url = '${AppConfig.baseUrl}/daily-sales-summary/daily/$date';
      final request = await client.getUrl(Uri.parse(url));
      
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
          final Map<String, dynamic> jsonData = jsonDecode(responseBody);
          return jsonData;
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

  // Haftalık satış özeti getir
  static Future<Map<String, dynamic>?> getWeeklySalesSummary(String endDate) async {
    try {
      final client = HttpClient();
      client.badCertificateCallback = (cert, host, port) => true;
      
      final url = '${AppConfig.baseUrl}/daily-sales-summary/weekly/$endDate';
      final request = await client.getUrl(Uri.parse(url));
      
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
          final Map<String, dynamic> jsonData = jsonDecode(responseBody);
          return jsonData;
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

  // Kategori bazında satış verilerini getir
  static Future<Map<String, dynamic>> getSalesByCategory(String startDate, [String? endDate]) async {
    try {
      final client = HttpClient();
      client.badCertificateCallback = (cert, host, port) => true;
      
      String url = '${AppConfig.baseUrl}/daily-sales-summary/sales-by-category?startDate=$startDate';
      if (endDate != null) {
        url += '&endDate=$endDate';
      }
      
      final request = await client.getUrl(Uri.parse(url));
      
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
          final Map<String, dynamic> jsonData = jsonDecode(responseBody);
          return jsonData;
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

  // Dashboard özet verilerini getir (günlük sipariş, ciro, rezervasyon)
  static Future<Map<String, dynamic>> getDashboardSummary() async {
    try {
      final client = HttpClient();
      client.badCertificateCallback = (cert, host, port) => true;
      
      final url = '${AppConfig.baseUrl}${AppConfig.dashboardSummaryEndpoint}';
      final request = await client.getUrl(Uri.parse(url));
      
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
          final Map<String, dynamic> jsonData = jsonDecode(responseBody);
          return jsonData;
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

  // Günlük ciro verilerini getir
  static Future<Map<String, dynamic>> getDailyRevenue() async {
    try {
      final client = HttpClient();
      client.badCertificateCallback = (cert, host, port) => true;

      final url = '${AppConfig.baseUrl}/api/analytics/revenue';
      final request = await client.getUrl(Uri.parse(url));

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
          final Map<String, dynamic> jsonData = jsonDecode(responseBody);
          return jsonData;
        } catch (e) {
          print('Daily revenue JSON parse hatası: $e');
          return {};
        }
      } else {
        print('Daily revenue API hatası: ${response.statusCode} $responseBody');
        return {};
      }
    } catch (e) {
      print('Daily revenue service hatası: $e');
      return {};
    }
  }

  // Günlük sipariş sayısını getir
  static Future<int> getDailyOrdersCount() async {
    try {
      final client = HttpClient();
      client.badCertificateCallback = (cert, host, port) => true;

      final url = '${AppConfig.baseUrl}/api/analytics/orders-daily-count';
      final request = await client.getUrl(Uri.parse(url));

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
          final Map<String, dynamic> jsonData = jsonDecode(responseBody);
          return jsonData['count'] ?? 0;
        } catch (e) {
          print('Daily orders count JSON parse hatası: $e');
          return 0;
        }
      } else {
        print('Daily orders count API hatası: ${response.statusCode} $responseBody');
        return 0;
      }
    } catch (e) {
      print('Daily orders count service hatası: $e');
      return 0;
    }
  }

  // Aktif rezervasyon sayısını getir
  static Future<int> getActiveReservationsCount() async {
    try {
      final client = HttpClient();
      client.badCertificateCallback = (cert, host, port) => true;
      
      final url = '${AppConfig.baseUrl}/api/analytics/reservations-active-count';
      final request = await client.getUrl(Uri.parse(url));
      
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
          final Map<String, dynamic> jsonData = jsonDecode(responseBody);
          return jsonData['count'] ?? 0;
        } catch (e) {
          print('Active reservations count JSON parse hatası: $e');
          return 0;
        }
      } else {
        print('Active reservations count API hatası: ${response.statusCode} $responseBody');
        return 0;
      }
    } catch (e) {
      print('Active reservations count service hatası: $e');
      return 0;
    }
  }







  // En çok satan ürünleri getir (Map formatında)
  static Future<List<Map<String, dynamic>>> getTopProductsMap() async {
    try {
      final client = HttpClient();
      client.badCertificateCallback = (cert, host, port) => true;
      
      final url = '${AppConfig.baseUrl}/api/analytics/top-products';
      final request = await client.getUrl(Uri.parse(url));
      
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
          final List<dynamic> jsonData = jsonDecode(responseBody);
          return jsonData.cast<Map<String, dynamic>>();
        } catch (e) {
          print('Top products JSON parse hatası: $e');
          return [];
        }
      } else {
        print('Top products API hatası: ${response.statusCode} $responseBody');
        return [];
      }
    } catch (e) {
      print('Top products service hatası: $e');
      return [];
    }
  }

  // Belirli bir sebep (reason) için stok hareketlerini getir
  static Future<List<Map<String, dynamic>>> getStockMovementsByReason(String reason) async {
    try {
      final client = HttpClient();
      client.badCertificateCallback = (cert, host, port) => true;

      final url = '${AppConfig.baseUrl}/api/stock-movements/reason/$reason';
      final request = await client.getUrl(Uri.parse(url));

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
          final List<dynamic> jsonData = jsonDecode(responseBody);
          return jsonData.cast<Map<String, dynamic>>();
        } catch (e) {
          print('Stock movements by reason JSON parse hatası: $e');
          return [];
        }
      } else {
        print('Stock movements by reason API hatası: ${response.statusCode} ${response.statusCode}');
        return [];
      }
    } catch (e) {
      print('Stock movements by reason service hatası: $e');
      return [];
    }
  }

  // Yeni ürün oluştur
  static Future<void> createProduct(Map<String, dynamic> productData) async {
    try {
      final client = HttpClient();
      client.badCertificateCallback = (cert, host, port) => true;

      final url = '${AppConfig.baseUrl}/api/products';
      final request = await client.postUrl(Uri.parse(url));

      // Authorization header'ları ekle
      final headers = _getAuthHeaders();
      headers.forEach((key, value) {
        request.headers.set(key, value);
      });

      // Request body'yi JSON olarak gönder
      final jsonBody = jsonEncode(productData);
      request.write(jsonBody);

      final response = await request.close();
      final responseBody = await response.transform(utf8.decoder).join();

      client.close();

      if (response.statusCode == 201 || response.statusCode == 200) {
        print('Ürün başarıyla oluşturuldu');
      } else {
        print('Ürün oluşturma API hatası: ${response.statusCode} $responseBody');
        throw Exception('Ürün oluşturulamadı: HTTP ${response.statusCode}');
      }
    } catch (e) {
      print('Ürün oluşturma service hatası: $e');
      throw Exception('Ürün oluşturulamadı: $e');
    }
  }

  // Ürün güncelle
  static Future<void> updateProduct(int productId, Map<String, dynamic> productData) async {
    try {
      final client = HttpClient();
      client.badCertificateCallback = (cert, host, port) => true;

      final url = '${AppConfig.baseUrl}/api/products/$productId';
      final request = await client.putUrl(Uri.parse(url));

      // Authorization header'ları ekle
      final headers = _getAuthHeaders();
      headers.forEach((key, value) {
        request.headers.set(key, value);
      });

      // Request body'yi JSON olarak gönder
      final jsonBody = jsonEncode(productData);
      request.write(jsonBody);

      final response = await request.close();
      final responseBody = await response.transform(utf8.decoder).join();

      client.close();

      if (response.statusCode == 200) {
        print('Ürün başarıyla güncellendi');
      } else {
        print('Ürün güncelleme API hatası: ${response.statusCode} $responseBody');
        throw Exception('Ürün güncellenemedi: HTTP ${response.statusCode}');
      }
    } catch (e) {
      print('Ürün güncelleme service hatası: $e');
      throw Exception('Ürün güncellenemedi: $e');
    }
  }

  // Ürün sil
  static Future<void> deleteProduct(int productId) async {
    try {
      final client = HttpClient();
      client.badCertificateCallback = (cert, host, port) => true;

      final url = '${AppConfig.baseUrl}/api/products/$productId';
      final request = await client.deleteUrl(Uri.parse(url));

      // Authorization header'ları ekle
      final headers = _getAuthHeaders();
      headers.forEach((key, value) {
        request.headers.set(key, value);
      });

      final response = await request.close();
      final responseBody = await response.transform(utf8.decoder).join();

      client.close();

      if (response.statusCode == 200 || response.statusCode == 204) {
        print('Ürün başarıyla silindi');
      } else {
        print('Ürün silme API hatası: ${response.statusCode} $responseBody');
        throw Exception('Ürün silinemedi: HTTP ${response.statusCode}');
      }
    } catch (e) {
      print('Ürün silme service hatası: $e');
      throw Exception('Ürün silinemedi: $e');
    }
  }

  // Tüm ürünleri getir
  static Future<List<Map<String, dynamic>>> getAllProducts() async {
    try {
      final client = HttpClient();
      client.badCertificateCallback = (cert, host, port) => true;

      final url = '${AppConfig.baseUrl}/api/products';
      final request = await client.getUrl(Uri.parse(url));

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
          final List<dynamic> jsonData = jsonDecode(responseBody);
          return jsonData.cast<Map<String, dynamic>>();
        } catch (e) {
          print('All products JSON parse hatası: $e');
          return [];
        }
      } else {
        print('All products API hatası: ${response.statusCode} $responseBody');
        return [];
      }
    } catch (e) {
      print('All products service hatası: $e');
      return [];
    }
  }

  // Ürün miktarlarını getir
  static Future<List<Map<String, dynamic>>> getProductQuantities() async {
    try {
      final client = HttpClient();
      client.badCertificateCallback = (cert, host, port) => true;

      final url = '${AppConfig.baseUrl}/api/products/available-quantities';
      final request = await client.getUrl(Uri.parse(url));

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
          final List<dynamic> jsonData = jsonDecode(responseBody);
          return jsonData.cast<Map<String, dynamic>>();
        } catch (e) {
          print('Product quantities JSON parse hatası: $e');
          return [];
        }
      } else {
        print('Product quantities API hatası: ${response.statusCode} $responseBody');
        return [];
      }
    } catch (e) {
      print('Product quantities service hatası: $e');
      return [];
    }
  }

  // Tüm stok hareketlerini getir
  static Future<List<Map<String, dynamic>>> getAllStockMovements() async {
    try {
      final client = HttpClient();
      client.badCertificateCallback = (cert, host, port) => true;

      final url = '${AppConfig.baseUrl}/api/stock-movements';
      final request = await client.getUrl(Uri.parse(url));

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
          final List<dynamic> jsonData = jsonDecode(responseBody);
          return jsonData.cast<Map<String, dynamic>>();
        } catch (e) {
          print('All stock movements JSON parse hatası: $e');
          return [];
        }
      } else {
        print('All stock movements API hatası: ${response.statusCode} $responseBody');
        return [];
      }
    } catch (e) {
      print('All stock movements service hatası: $e');
      return [];
    }
  }



  // Belirli bir tarih aralığındaki stok hareketlerini getir
  static Future<List<Map<String, dynamic>>> getStockMovementsByDateRange(String startDate, String endDate) async {
    try {
      final client = HttpClient();
      client.badCertificateCallback = (cert, host, port) => true;
      
      final url = '${AppConfig.baseUrl}/api/stock-movements/date-range?startDate=$startDate&endDate=$endDate';
      final request = await client.getUrl(Uri.parse(url));
      
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
          final List<dynamic> jsonData = jsonDecode(responseBody);
          return jsonData.cast<Map<String, dynamic>>();
        } catch (e) {
          print('Stock movements by date range JSON parse hatası: $e');
          return [];
        }
      } else {
        print('Stock movements by date range API hatası: ${response.statusCode} ${response.statusCode}');
        return [];
      }
    } catch (e) {
      print('Stock movements by date range service hatası: $e');
      return [];
    }
  }




}
