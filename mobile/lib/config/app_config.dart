class AppConfig {
  // Backend konfigürasyonu - HTTPS kullan (self-signed sertifika sorunu için)
  static const String baseUrl = 'https://192.168.232.113:8080';
  
  // API Endpoints
  static const String loginEndpoint = '/api/auth/login';
  static const String forgotPasswordEndpoint = '/api/auth/forgot-password';
  static const String resetPasswordEndpoint = '/api/auth/reset-password';
  static const String usersEndpoint = '/api/users';
  static const String usersActiveEndpoint = '/api/users/{id}/active';
  static const String usersPhotoEndpoint = '/api/users/{id}/photo';
  static const String analyticsTopProductsEndpoint = '/api/analytics/top-products';
  static const String analyticsTopProductsSummaryEndpoint = '/api/analytics/top-products/summary';
  static const String analyticsCategorySalesEndpoint = '/api/analytics/category-sales';
  static const String analyticsRevenueEndpoint = '/api/analytics/revenue';
  static const String analyticsPerformanceMetricsEndpoint = '/api/analytics/performance-metrics';
  static const String analyticsEmployeePerformanceEndpoint = '/api/analytics/employee-performance';
  static const String analyticsSummaryStatusEndpoint = '/api/analytics/summary-status';
  
  // Yeni endpoint'ler
  static const String dashboardSummaryEndpoint = '/api/analytics/dashboard-summary';
  static const String ordersDailyCountEndpoint = '/api/orders/daily/count';
  static const String reservationsActiveCountEndpoint = '/api/reservations/active/count';
  
  // Timeout ayarları
  static const int connectionTimeout = 30000; // 30 saniye
  static const int receiveTimeout = 30000;
}
