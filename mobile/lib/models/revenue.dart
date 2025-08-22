class Revenue {
  final Map<String, dynamic> revenueData;

  Revenue({required this.revenueData});

  factory Revenue.fromJson(Map<String, dynamic> json) {
    return Revenue(revenueData: json);
  }

  Map<String, dynamic> toJson() {
    return revenueData;
  }

  // Revenue anahtarlarını getir
  List<String> get revenueKeys {
    return revenueData.keys.toList();
  }

  // Belirli bir revenue verisini getir
  dynamic getRevenueData(String key) {
    return revenueData[key];
  }

  // Toplam revenue sayısı
  int get totalRevenueEntries {
    return revenueData.length;
  }

  // Tüm revenue verilerini getir
  Map<String, dynamic> getAllData() {
    return Map<String, dynamic>.from(revenueData);
  }

  // Revenue bazında sıralanmış veri
  List<MapEntry<String, dynamic>> getSortedData() {
    final entries = revenueData.entries.toList();
    // String anahtarlara göre sırala
    entries.sort((a, b) => a.key.compareTo(b.key));
    return entries;
  }

  // Boş mu kontrol et
  bool get isEmpty {
    return revenueData.isEmpty;
  }

  // Boş değil mi kontrol et
  bool get isNotEmpty {
    return revenueData.isNotEmpty;
  }

  // String representation
  @override
  String toString() {
    return 'Revenue(${revenueData.toString()})';
  }
}
