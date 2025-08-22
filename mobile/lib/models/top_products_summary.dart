import 'top_product.dart';

class TopProductsSummary {
  final List<TopProduct> daily;
  final List<TopProduct> weekly;
  final List<TopProduct> monthly;
  final List<TopProduct> yearly;

  TopProductsSummary({
    required this.daily,
    required this.weekly,
    required this.monthly,
    required this.yearly,
  });

  factory TopProductsSummary.fromJson(Map<String, dynamic> json) {
    return TopProductsSummary(
      daily: (json['daily'] as List<dynamic>?)
          ?.map((item) => TopProduct.fromJson(item))
          .toList() ?? [],
      weekly: (json['weekly'] as List<dynamic>?)
          ?.map((item) => TopProduct.fromJson(item))
          .toList() ?? [],
      monthly: (json['monthly'] as List<dynamic>?)
          ?.map((item) => TopProduct.fromJson(item))
          .toList() ?? [],
      yearly: (json['yearly'] as List<dynamic>?)
          ?.map((item) => TopProduct.fromJson(item))
          .toList() ?? [],
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'daily': daily.map((item) => item.toJson()).toList(),
      'weekly': weekly.map((item) => item.toJson()).toList(),
      'monthly': monthly.map((item) => item.toJson()).toList(),
      'yearly': yearly.map((item) => item.toJson()).toList(),
    };
  }

  // Günlük en çok satan ürünler
  List<TopProduct> get topDailyProducts => daily;
  
  // Haftalık en çok satan ürünler
  List<TopProduct> get topWeeklyProducts => weekly;
  
  // Aylık en çok satan ürünler
  List<TopProduct> get topMonthlyProducts => monthly;
  
  // Yıllık en çok satan ürünler
  List<TopProduct> get topYearlyProducts => yearly;

  // Belirli bir dönem için en çok satan ürünler
  List<TopProduct> getProductsByPeriod(String period) {
    switch (period.toLowerCase()) {
      case 'daily':
        return daily;
      case 'weekly':
        return weekly;
      case 'monthly':
        return monthly;
      case 'yearly':
        return yearly;
      default:
        return [];
    }
  }

  // Tüm dönemlerde en çok satan ürünler (birleştirilmiş)
  List<TopProduct> getAllPeriodsProducts() {
    final allProducts = <TopProduct>[];
    allProducts.addAll(daily);
    allProducts.addAll(weekly);
    allProducts.addAll(monthly);
    allProducts.addAll(yearly);
    return allProducts;
  }

  // En yüksek gelirli ürün (tüm dönemlerde)
  TopProduct? get highestRevenueProduct {
    final allProducts = getAllPeriodsProducts();
    if (allProducts.isEmpty) return null;
    
    return allProducts.reduce((a, b) => a.totalRevenue > b.totalRevenue ? a : b);
  }

  // En çok sipariş edilen ürün (tüm dönemlerde)
  TopProduct? get mostOrderedProduct {
    final allProducts = getAllPeriodsProducts();
    if (allProducts.isEmpty) return null;
    
    return allProducts.reduce((a, b) => a.orderCount > b.orderCount ? a : b);
  }

  // Toplam gelir (tüm dönemlerde)
  double get totalRevenue {
    final allProducts = getAllPeriodsProducts();
    return allProducts.fold(0.0, (sum, product) => sum + product.totalRevenue);
  }

  // Toplam sipariş sayısı (tüm dönemlerde)
  int get totalOrderCount {
    final allProducts = getAllPeriodsProducts();
    return allProducts.fold(0, (sum, product) => sum + product.orderCount);
  }

  // Boş mu kontrol et
  bool get isEmpty {
    return daily.isEmpty && weekly.isEmpty && monthly.isEmpty && yearly.isEmpty;
  }

  // Boş değil mi kontrol et
  bool get isNotEmpty {
    return !isEmpty;
  }

  // String representation
  @override
  String toString() {
    return 'TopProductsSummary(daily: ${daily.length}, weekly: ${weekly.length}, monthly: ${monthly.length}, yearly: ${yearly.length})';
  }
}
