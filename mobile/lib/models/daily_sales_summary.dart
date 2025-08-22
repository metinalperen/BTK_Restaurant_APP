class DailySalesSummary {
  final String reportDate;
  final double totalRevenue;
  final int totalOrders;
  final double averageOrderValue;
  final int totalCustomers;
  final String mostPopularItemName;
  final String leastPopularItemName;
  final int totalReservations;
  final String reportType;
  final Map<String, String> salesByCategory;
  final String employeePerformance;

  DailySalesSummary({
    required this.reportDate,
    required this.totalRevenue,
    required this.totalOrders,
    required this.averageOrderValue,
    required this.totalCustomers,
    required this.mostPopularItemName,
    required this.leastPopularItemName,
    required this.totalReservations,
    required this.reportType,
    required this.salesByCategory,
    required this.employeePerformance,
  });

  factory DailySalesSummary.fromJson(Map<String, dynamic> json) {
    return DailySalesSummary(
      reportDate: json['reportDate'] ?? '',
      totalRevenue: (json['totalRevenue'] ?? 0).toDouble(),
      totalOrders: json['totalOrders'] ?? 0,
      averageOrderValue: (json['averageOrderValue'] ?? 0).toDouble(),
      totalCustomers: json['totalCustomers'] ?? 0,
      mostPopularItemName: json['mostPopularItemName'] ?? '',
      leastPopularItemName: json['leastPopularItemName'] ?? '',
      totalReservations: json['totalReservations'] ?? 0,
      reportType: json['reportType'] ?? '',
      salesByCategory: Map<String, String>.from(json['salesByCategory'] ?? {}),
      employeePerformance: json['employeePerformance'] ?? '',
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'reportDate': reportDate,
      'totalRevenue': totalRevenue,
      'totalOrders': totalOrders,
      'averageOrderValue': averageOrderValue,
      'totalCustomers': totalCustomers,
      'mostPopularItemName': mostPopularItemName,
      'leastPopularItemName': leastPopularItemName,
      'totalReservations': totalReservations,
      'reportType': reportType,
      'salesByCategory': salesByCategory,
      'employeePerformance': employeePerformance,
    };
  }

  // Formatlanmış gelir (₺)
  String get formattedTotalRevenue {
    return '₺${totalRevenue.toStringAsFixed(2)}';
  }

  // Formatlanmış ortalama sipariş tutarı (₺)
  String get formattedAverageOrderValue {
    return '₺${averageOrderValue.toStringAsFixed(2)}';
  }

  // Kategori bazında satış verilerini getir
  List<MapEntry<String, String>> get sortedSalesByCategory {
    final entries = salesByCategory.entries.toList();
    entries.sort((a, b) => a.key.compareTo(b.key));
    return entries;
  }

  // Toplam kategori sayısı
  int get totalCategories {
    return salesByCategory.length;
  }

  // En yüksek satış yapan kategori
  String? get topCategory {
    if (salesByCategory.isEmpty) return null;
    
    String? topCategory;
    double maxRevenue = 0;
    
    for (final entry in salesByCategory.entries) {
      final revenue = double.tryParse(entry.value) ?? 0;
      if (revenue > maxRevenue) {
        maxRevenue = revenue;
        topCategory = entry.key;
      }
    }
    
    return topCategory;
  }

  // En yüksek satış yapan kategorinin geliri
  double get topCategoryRevenue {
    if (topCategory == null) return 0;
    return double.tryParse(salesByCategory[topCategory!] ?? '0') ?? 0;
  }

  // Formatlanmış en yüksek kategori geliri
  String get formattedTopCategoryRevenue {
    return '₺${topCategoryRevenue.toStringAsFixed(2)}';
  }

  // Rapor tipini Türkçe olarak getir
  String get reportTypeInTurkish {
    switch (reportType.toUpperCase()) {
      case 'DAILY':
        return 'Günlük';
      case 'WEEKLY':
        return 'Haftalık';
      case 'MONTHLY':
        return 'Aylık';
      case 'YEARLY':
        return 'Yıllık';
      default:
        return reportType;
    }
  }

  // Tarih formatını düzenle
  String get formattedDate {
    try {
      final date = DateTime.parse(reportDate);
      return '${date.day.toString().padLeft(2, '0')}/${date.month.toString().padLeft(2, '0')}/${date.year}';
    } catch (e) {
      return reportDate;
    }
  }

  // Boş mu kontrol et
  bool get isEmpty {
    return reportDate.isEmpty && totalRevenue == 0 && totalOrders == 0;
  }

  // Boş değil mi kontrol et
  bool get isNotEmpty {
    return !isEmpty;
  }

  // String representation
  @override
  String toString() {
    return 'DailySalesSummary(date: $reportDate, revenue: $totalRevenue, orders: $totalOrders)';
  }
}
