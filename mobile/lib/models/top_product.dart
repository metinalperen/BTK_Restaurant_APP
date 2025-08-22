class TopProduct {
  final int productId;
  final String productName;
  final int totalQuantity;
  final int orderCount;
  final double totalRevenue;

  TopProduct({
    required this.productId,
    required this.productName,
    required this.totalQuantity,
    required this.orderCount,
    required this.totalRevenue,
  });

  factory TopProduct.fromJson(Map<String, dynamic> json) {
    return TopProduct(
      productId: json['productId'] ?? 0,
      productName: json['productName'] ?? '',
      totalQuantity: json['totalQuantity'] ?? 0,
      orderCount: json['orderCount'] ?? 0,
      totalRevenue: (json['totalRevenue'] ?? 0).toDouble(),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'productId': productId,
      'productName': productName,
      'totalQuantity': totalQuantity,
      'orderCount': orderCount,
      'totalRevenue': totalRevenue,
    };
  }

  // Ortalama sipariş tutarı
  double get averageOrderValue {
    return orderCount > 0 ? totalRevenue / orderCount : 0;
  }

  // Toplam gelir formatı (₺)
  String get formattedRevenue {
    return '₺${totalRevenue.toStringAsFixed(2)}';
  }

  // Ortalama sipariş tutarı formatı (₺)
  String get formattedAverageOrder {
    return '₺${averageOrderValue.toStringAsFixed(2)}';
  }
}
