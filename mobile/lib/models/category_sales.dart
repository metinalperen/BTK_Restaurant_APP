class CategorySales {
  final Map<String, dynamic> salesData;

  CategorySales({required this.salesData});

  factory CategorySales.fromJson(Map<String, dynamic> json) {
    return CategorySales(salesData: json);
  }

  Map<String, dynamic> toJson() {
    return salesData;
  }

  // Kategori adlarını getir
  List<String> get categoryNames {
    return salesData.keys.toList();
  }

  // Belirli bir kategorinin satış verisini getir
  dynamic getCategoryData(String categoryName) {
    return salesData[categoryName];
  }

  // Toplam kategori sayısı
  int get totalCategories {
    return salesData.length;
  }

  // Tüm kategorilerin toplam satış verisi
  Map<String, dynamic> getAllData() {
    return Map<String, dynamic>.from(salesData);
  }

  // Kategori bazında sıralanmış veri
  List<MapEntry<String, dynamic>> getSortedData() {
    final entries = salesData.entries.toList();
    // String değerlere göre sırala
    entries.sort((a, b) => a.key.compareTo(b.key));
    return entries;
  }

  // Boş mu kontrol et
  bool get isEmpty {
    return salesData.isEmpty;
  }

  // Boş değil mi kontrol et
  bool get isNotEmpty {
    return salesData.isNotEmpty;
  }

  // String representation
  @override
  String toString() {
    return 'CategorySales(${salesData.toString()})';
  }
}
