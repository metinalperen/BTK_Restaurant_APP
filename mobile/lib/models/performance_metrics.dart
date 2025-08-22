class PerformanceMetrics {
  final Map<String, dynamic> metricsData;

  PerformanceMetrics({required this.metricsData});

  factory PerformanceMetrics.fromJson(Map<String, dynamic> json) {
    return PerformanceMetrics(metricsData: json);
  }

  Map<String, dynamic> toJson() {
    return metricsData;
  }

  // Metrik anahtarlarını getir
  List<String> get metricsKeys {
    return metricsData.keys.toList();
  }

  // Belirli bir metrik verisini getir
  dynamic getMetricData(String key) {
    return metricsData[key];
  }

  // Toplam metrik sayısı
  int get totalMetrics {
    return metricsData.length;
  }

  // Tüm metrik verilerini getir
  Map<String, dynamic> getAllData() {
    return Map<String, dynamic>.from(metricsData);
  }

  // Metrik bazında sıralanmış veri
  List<MapEntry<String, dynamic>> getSortedData() {
    final entries = metricsData.entries.toList();
    // String anahtarlara göre sırala
    entries.sort((a, b) => a.key.compareTo(b.key));
    return entries;
  }

  // Boş mu kontrol et
  bool get isEmpty {
    return metricsData.isEmpty;
  }

  // Boş değil mi kontrol et
  bool get isNotEmpty {
    return metricsData.isNotEmpty;
  }

  // String representation
  @override
  String toString() {
    return 'PerformanceMetrics(${metricsData.toString()})';
  }
}
