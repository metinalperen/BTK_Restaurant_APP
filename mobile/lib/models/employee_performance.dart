class EmployeePerformance {
  final Map<String, dynamic> performanceData;

  EmployeePerformance({required this.performanceData});

  factory EmployeePerformance.fromJson(Map<String, dynamic> json) {
    return EmployeePerformance(performanceData: json);
  }

  Map<String, dynamic> toJson() {
    return performanceData;
  }

  // Performans anahtarlarını getir
  List<String> get performanceKeys {
    return performanceData.keys.toList();
  }

  // Belirli bir performans verisini getir
  dynamic getPerformanceData(String key) {
    return performanceData[key];
  }

  // Toplam performans girişi sayısı
  int get totalPerformanceEntries {
    return performanceData.length;
  }

  // Tüm performans verilerini getir
  Map<String, dynamic> getAllData() {
    return Map<String, dynamic>.from(performanceData);
  }

  // Performans bazında sıralanmış veri
  List<MapEntry<String, dynamic>> getSortedData() {
    final entries = performanceData.entries.toList();
    // String anahtarlara göre sırala
    entries.sort((a, b) => a.key.compareTo(b.key));
    return entries;
  }

  // Boş mu kontrol et
  bool get isEmpty {
    return performanceData.isEmpty;
  }

  // Boş değil mi kontrol et
  bool get isNotEmpty {
    return performanceData.isNotEmpty;
  }

  // String representation
  @override
  String toString() {
    return 'EmployeePerformance(${performanceData.toString()})';
  }
}
