class SummaryStatus {
  final Map<String, dynamic> statusData;

  SummaryStatus({required this.statusData});

  factory SummaryStatus.fromJson(Map<String, dynamic> json) {
    return SummaryStatus(statusData: json);
  }

  Map<String, dynamic> toJson() {
    return statusData;
  }

  // Status anahtarlarını getir
  List<String> get statusKeys {
    return statusData.keys.toList();
  }

  // Belirli bir status verisini getir
  dynamic getStatusData(String key) {
    return statusData[key];
  }

  // Toplam status girişi sayısı
  int get totalStatusEntries {
    return statusData.length;
  }

  // Tüm status verilerini getir
  Map<String, dynamic> getAllData() {
    return Map<String, dynamic>.from(statusData);
  }

  // Status bazında sıralanmış veri
  List<MapEntry<String, dynamic>> getSortedData() {
    final entries = statusData.entries.toList();
    // String anahtarlara göre sırala
    entries.sort((a, b) => a.key.compareTo(b.key));
    return entries;
  }

  // Boş mu kontrol et
  bool get isEmpty {
    return statusData.isEmpty;
  }

  // Boş değil mi kontrol et
  bool get isNotEmpty {
    return statusData.isNotEmpty;
  }

  // String representation
  @override
  String toString() {
    return 'SummaryStatus(${statusData.toString()})';
  }
}
