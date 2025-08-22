import 'package:flutter/material.dart';
import '../../services/analytics_service.dart';

class ReportScreen extends StatefulWidget {
  const ReportScreen({super.key});

  @override
  State<ReportScreen> createState() => _ReportScreenState();
}

class _ReportScreenState extends State<ReportScreen> {
  String _selectedPeriod = 'Günlük';
  String _selectedMonth = 'Ağustos';
  DateTime _selectedDate = DateTime.now();

  final List<String> _periods = ['Günlük', 'Haftalık', 'Aylık', 'Yıllık'];
  final List<String> _months = [
    'Ocak', 'Şubat', 'Mart', 'Nisan', 'Mayıs', 'Haziran',
    'Temmuz', 'Ağustos', 'Eylül', 'Ekim', 'Kasım', 'Aralık'
  ];

  // Backend'den gelecek veriler
  int _dailyOrdersCount = 0;
  double _dailyRevenue = 0.0;
  int _activeReservationsCount = 0;
  bool _isLoadingSummary = true;
  String? _summaryError;

  @override
  void initState() {
    super.initState();
    _loadDashboardSummary();
    _loadRevenueData();
    _loadSummaryStatus();
    _loadProductStock();           // YENİ: Ürün stok verilerini yükle
    _loadProductQuantities();      // YENİ: Ürün miktarlarını yükle
    _loadStockMovements();         // YENİ: Stok hareketlerini yükle
    _loadTopProducts();            // YENİ: En çok satan ürünleri yükle
    _loadSalesByCategory();        // YENİ: Kategori bazlı satışları yükle
  }

  // Dashboard özet verilerini yükle
  Future<void> _loadDashboardSummary() async {
    try {
      setState(() {
        _isLoadingSummary = true;
        _summaryError = null;
      });

      // Dashboard summary endpoint'ini kullan
      final dashboardData = await AnalyticsService.getDashboardSummary();
      
      if (dashboardData != null) {
        setState(() {
          _dailyOrdersCount = dashboardData['dailyOrdersCount'] ?? 0;
          _dailyRevenue = (dashboardData['dailyRevenue'] ?? 0.0).toDouble();
          _activeReservationsCount = dashboardData['activeReservationsCount'] ?? 0;
          _isLoadingSummary = false;
        });
      } else {
        // Fallback olarak eski yöntemi kullan
        final results = await Future.wait([
          AnalyticsService.getDailyOrdersCount(),
          AnalyticsService.getDailyRevenue(),
          AnalyticsService.getActiveReservationsCount(),
        ]);

        setState(() {
          _dailyOrdersCount = results[0] as int;
          _dailyRevenue = (results[1] as Map<String, dynamic>)['total'] ?? 0.0;
          _activeReservationsCount = results[2] as int;
          _isLoadingSummary = false;
        });
      }
    } catch (e) {
      setState(() {
        _summaryError = e.toString();
        _isLoadingSummary = false;
      });
      print('Dashboard summary yüklenirken hata: $e');
    }
  }

  // Revenue verilerini yükle
  Future<void> _loadRevenueData() async {
    try {
      setState(() {
        _isLoadingRevenue = true;
        _revenueError = null;
      });

      final revenueData = await AnalyticsService.getDailyRevenue();
      
      // Swagger'dan gelen veriyi parse et
      if (revenueData.containsKey('additionalProp1')) {
        // Bu örnek veri, gerçek API'den gelen veriyi burada parse edeceğiz
        setState(() {
          _dailySales = [
            {'date': 'Bugün', 'orders': 0, 'revenue': 0, 'customers': 0},
          ];
          _isLoadingRevenue = false;
        });
      } else {
        // Gerçek veri geldiğinde bu kısım çalışacak
        setState(() {
          _dailySales = [];
          _isLoadingRevenue = false;
        });
      }
    } catch (e) {
      setState(() {
        _revenueError = e.toString();
        _isLoadingRevenue = false;
      });
      print('Revenue verileri yüklenirken hata: $e');
    }
  }

  // Summary Status verilerini yükle
  Future<void> _loadSummaryStatus() async {
    try {
      setState(() {
        _isLoadingSummary = true;
        _summaryError = null;
      });

      final summaryData = await AnalyticsService.getSummaryStatus();
      
      // Swagger'dan gelen veriyi parse et
      if (summaryData != null) {
        // SummaryStatus model'inden gelen veriyi parse et
        setState(() {
          // Burada SummaryStatus model'inden gelen verileri kullanacağız
          // Şimdilik örnek veri olarak kullanıyoruz
          _dailyOrdersCount = 0;
          _dailyRevenue = 0.0;
          _activeReservationsCount = 0;
          _isLoadingSummary = false;
        });
      } else {
        // Veri gelmediğinde
        setState(() {
          _dailyOrdersCount = 0;
          _dailyRevenue = 0.0;
          _activeReservationsCount = 0;
          _isLoadingSummary = false;
        });
      }
    } catch (e) {
      setState(() {
        _summaryError = e.toString();
        _isLoadingSummary = false;
      });
      print('Summary status yüklenirken hata: $e');
    }
  }

  // Ürün stok verilerini yükle
  Future<void> _loadProductStock() async {
    try {
      setState(() {
        _isLoadingProductStock = true;
        _productStockError = null;
      });

      final products = await AnalyticsService.getAllProducts();
      
      setState(() {
        _productStock = products;
        _isLoadingProductStock = false;
      });
    } catch (e) {
      setState(() {
        _productStockError = e.toString();
        _isLoadingProductStock = false;
      });
      print('Ürün stok verileri yüklenirken hata: $e');
    }
  }

  // Ürün miktarlarını yükle
  Future<void> _loadProductQuantities() async {
    try {
      setState(() {
        _isLoadingProductQuantities = true;
        _productQuantitiesError = null;
      });

      final quantities = await AnalyticsService.getProductQuantities();
      
      setState(() {
        _productQuantities = quantities;
        _isLoadingProductQuantities = false;
      });
    } catch (e) {
      setState(() {
        _productQuantitiesError = e.toString();
        _isLoadingProductQuantities = false;
      });
      print('Ürün miktarları yüklenirken hata: $e');
    }
  }

  // Stok hareketlerini yükle
  Future<void> _loadStockMovements() async {
    try {
      setState(() {
        _isLoadingStockMovements = true;
        _stockMovementsError = null;
      });

      final movements = await AnalyticsService.getAllStockMovements();
      
      setState(() {
        _stockMovements = movements;
        _isLoadingStockMovements = false;
      });
    } catch (e) {
      setState(() {
        _stockMovementsError = e.toString();
        _isLoadingStockMovements = false;
      });
      print('Stok hareketleri yüklenirken hata: $e');
    }
  }

  // En çok satan ürünleri yükle
  Future<void> _loadTopProducts() async {
    try {
      setState(() {
        _isLoadingTopProducts = true;
        _topProductsError = null;
      });

      final products = await AnalyticsService.getTopProductsMap();
      
      setState(() {
        _topProducts = products;
        _isLoadingTopProducts = false;
      });
    } catch (e) {
      setState(() {
        _topProductsError = e.toString();
        _isLoadingTopProducts = false;
      });
      print('Top products yüklenirken hata: $e');
    }
  }

  // Kategori bazlı satışları yükle
  Future<void> _loadSalesByCategory() async {
    try {
      setState(() {
        _isLoadingCategorySales = true;
        _categorySalesError = null;
      });

      final sales = await AnalyticsService.getSalesByCategory(DateTime.now().subtract(Duration(days: 7)).toIso8601String().split('T')[0]);
      
      setState(() {
        // API'den gelen veriyi listeye dönüştür
        if (sales.containsKey('data') && sales['data'] is List) {
          _categorySales = List<Map<String, dynamic>>.from(sales['data']);
        } else {
          _categorySales = [];
        }
        _isLoadingCategorySales = false;
      });
    } catch (e) {
      setState(() {
        _categorySalesError = e.toString();
        _isLoadingCategorySales = false;
      });
      print('Category sales yüklenirken hata: $e');
    }
  }

  // Backend'den gelecek veriler
  List<Map<String, dynamic>> _dailySales = [];
  List<Map<String, dynamic>> _topProducts = [];
  List<Map<String, dynamic>> _categorySales = [];
  List<Map<String, dynamic>> _employeePerformance = [];
  
  // Loading state'leri
  bool _isLoadingRevenue = true;
  bool _isLoadingTopProducts = true;
  bool _isLoadingCategorySales = true;
  bool _isLoadingEmployeePerformance = true;
  
  // Error state'leri
  String? _revenueError;
  String? _topProductsError;
  String? _categorySalesError;
  String? _employeePerformanceError;

  // Ürün stok verileri
  List<Map<String, dynamic>> _productStock = [];
  List<Map<String, dynamic>> _productQuantities = [];
  List<Map<String, dynamic>> _stockMovements = [];
  Map<String, dynamic>? _selectedStockMovement;
  
  // Loading state'leri
  bool _isLoadingProductStock = true;
  bool _isLoadingProductQuantities = true;
  bool _isLoadingStockMovements = true;
  
  // Error state'leri
  String? _productStockError;
  String? _productQuantitiesError;
  String? _stockMovementsError;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Raporlar'),
        backgroundColor: const Color(0xFF9C27B0),
        foregroundColor: Colors.white,
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh),
            onPressed: () {
              _loadDashboardSummary();
              _loadRevenueData();
              _loadSummaryStatus();
              _loadProductStock();           // YENİ
              _loadProductQuantities();      // YENİ
              _loadStockMovements();         // YENİ
              _loadTopProducts();            // YENİ
              _loadSalesByCategory();        // YENİ
            },
            tooltip: 'Yenile',
          ),
        ],
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          children: [
            // Üst bilgi kartları
            _buildSummaryCards(),
            const SizedBox(height: 20),
            
            // Satış grafiği
            _buildSalesGraphSection(),
            const SizedBox(height: 20),
            
            // YENİ: Ürün Stok Kısmı
            _buildProductStockSection(),
            const SizedBox(height: 20),
            
            // En çok satan ürünler
            _buildTopProductsSection(),
            const SizedBox(height: 20),
            
            // Kategori bazlı satışlar
            _buildCategorySalesSection(),
            const SizedBox(height: 20),
            
            // Çalışan performansı
            _buildEmployeePerformanceSection(),
            const SizedBox(height: 20),
            
            // Ciro detayları
            _buildRevenueDetailsSection(),
          ],
        ),
      ),
    );
  }

  Widget _buildSummaryCards() {
    if (_isLoadingSummary) {
      return Row(
        children: [
          Expanded(child: _buildLoadingCard()),
          const SizedBox(width: 16),
          Expanded(child: _buildLoadingCard()),
          const SizedBox(width: 16),
          Expanded(child: _buildLoadingCard()),
        ],
      );
    }

    if (_summaryError != null) {
      return Container(
        width: double.infinity,
        padding: const EdgeInsets.all(20),
        decoration: BoxDecoration(
          color: Colors.red[50],
          borderRadius: BorderRadius.circular(12),
          border: Border.all(color: Colors.red[200]!),
        ),
        child: Column(
          children: [
            Icon(Icons.error_outline, color: Colors.red[600], size: 32),
            const SizedBox(height: 8),
            Text(
              'Veri yüklenirken hata oluştu',
              style: TextStyle(color: Colors.red[700], fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 8),
            Text(
              _summaryError!,
              style: TextStyle(color: Colors.red[600], fontSize: 12),
              textAlign: TextAlign.center,
            ),
            const SizedBox(height: 8),
            ElevatedButton(
              onPressed: _loadDashboardSummary,
              style: ElevatedButton.styleFrom(
                backgroundColor: Colors.red[600],
                foregroundColor: Colors.white,
              ),
              child: const Text('Tekrar Dene'),
            ),
          ],
        ),
      );
    }

    return Row(
      children: [
        Expanded(
          child: _buildStatCard(
            'Bugünkü Sipariş',
            '$_dailyOrdersCount',
            Icons.bar_chart,
            Colors.green,
          ),
        ),
        const SizedBox(width: 16),
        Expanded(
          child: _buildStatCard(
            'Bugünkü Toplam Kazanç',
            '₺${_dailyRevenue.toStringAsFixed(2)}',
            Icons.account_balance_wallet,
            Colors.amber,
          ),
        ),
        const SizedBox(width: 16),
        Expanded(
          child: _buildStatCard(
            'Aktif Rezervasyon',
            '$_activeReservationsCount',
            Icons.calendar_today,
            Colors.blue,
          ),
        ),
      ],
    );
  }

  Widget _buildLoadingCard() {
    return Container(
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(12),
        boxShadow: [
          BoxShadow(
            color: Colors.grey.withOpacity(0.1),
            spreadRadius: 1,
            blurRadius: 10,
            offset: const Offset(0, 2),
          ),
        ],
      ),
      child: Column(
        children: [
          SizedBox(
            width: 32,
            height: 32,
            child: CircularProgressIndicator(
              strokeWidth: 3,
              valueColor: AlwaysStoppedAnimation<Color>(Colors.grey[400]!),
            ),
          ),
          const SizedBox(height: 12),
          Text(
            'Yükleniyor...',
            style: TextStyle(
              fontSize: 14,
              color: Colors.grey[600],
            ),
            textAlign: TextAlign.center,
          ),
        ],
      ),
    );
  }

  Widget _buildStatCard(String title, String value, IconData icon, Color color) {
    return Container(
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(12),
        boxShadow: [
          BoxShadow(
            color: Colors.grey.withOpacity(0.1),
            spreadRadius: 1,
            blurRadius: 10,
            offset: const Offset(0, 2),
          ),
        ],
      ),
      child: Column(
        children: [
          Icon(icon, color: color, size: 32),
          const SizedBox(height: 12),
          Text(
            value,
            style: const TextStyle(
              fontSize: 24,
              fontWeight: FontWeight.bold,
            ),
          ),
          const SizedBox(height: 8),
          Text(
            title,
            style: TextStyle(
              fontSize: 14,
              color: Colors.grey[600],
            ),
            textAlign: TextAlign.center,
          ),
        ],
      ),
    );
  }

  Widget _buildSalesGraphSection() {
    return Container(
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(12),
        boxShadow: [
          BoxShadow(
            color: Colors.grey.withOpacity(0.1),
            spreadRadius: 1,
            blurRadius: 10,
            offset: const Offset(0, 2),
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text(
            'Günlük Satış Grafiği',
            style: TextStyle(
              fontSize: 20,
              fontWeight: FontWeight.bold,
            ),
          ),
          const SizedBox(height: 16),
          
          // Loading state veya grafik
          if (_isLoadingRevenue)
            const Center(
              child: Padding(
                padding: EdgeInsets.all(40.0),
                child: CircularProgressIndicator(),
              ),
            )
          else if (_revenueError != null)
          Container(
              padding: const EdgeInsets.all(20),
              child: Text(
                'Revenue verisi yüklenirken hata: $_revenueError',
                style: const TextStyle(color: Colors.red),
              ),
            )
          else if (_dailySales.isEmpty)
            const Center(
              child: Padding(
                padding: EdgeInsets.all(40.0),
                child: Text(
                  'Henüz revenue verisi bulunamadı.',
                  style: TextStyle(color: Colors.grey),
                ),
              ),
            )
          else
          _buildBarChart(),
        ],
      ),
    );
  }

  Widget _buildTopProductsSection() {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(12),
        boxShadow: [
          BoxShadow(
            color: Colors.grey.withOpacity(0.1),
            spreadRadius: 1,
            blurRadius: 10,
            offset: const Offset(0, 2),
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text(
            'En Çok Satan Ürünler',
            style: TextStyle(
              fontSize: 18,
              fontWeight: FontWeight.bold,
            ),
          ),
          const SizedBox(height: 16),
          
          // Filtre butonları
          Wrap(
            spacing: 8,
            children: _periods.take(4).map((period) {
              final isSelected = period == _selectedPeriod;
              return ElevatedButton(
                onPressed: () {
                  setState(() {
                    _selectedPeriod = period;
                  });
                },
                style: ElevatedButton.styleFrom(
                  backgroundColor: isSelected ? const Color(0xFF9C27B0) : Colors.grey[200],
                  foregroundColor: isSelected ? Colors.white : Colors.black87,
                  padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                ),
                child: Text(period),
              );
            }).toList(),
          ),
          const SizedBox(height: 20),
          
          // Grafik başlığı
          const Text(
            'Günlük En Çok Satan Ürünler',
            style: TextStyle(
              fontSize: 16,
              fontWeight: FontWeight.w500,
            ),
          ),
          const SizedBox(height: 16),
          
          // Pasta grafik simülasyonu
          _buildPieChart(),
        ],
      ),
    );
  }

  Widget _buildCategorySalesSection() {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(12),
        boxShadow: [
          BoxShadow(
            color: Colors.grey.withOpacity(0.1),
            spreadRadius: 1,
            blurRadius: 10,
            offset: const Offset(0, 2),
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              const Icon(Icons.bar_chart, color: Color(0xFF9C27B0)),
              const SizedBox(width: 8),
              const Text(
                'Kategori Bazlı Satışlar',
                style: TextStyle(
                  fontSize: 18,
                  fontWeight: FontWeight.bold,
                ),
              ),
            ],
          ),
          const SizedBox(height: 16),
          
          // Tarih seçimi
          Row(
            children: [
              Expanded(
                child: Container(
                  padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
                  decoration: BoxDecoration(
                    color: Colors.grey[100],
                    borderRadius: BorderRadius.circular(8),
                  ),
                  child: Row(
                    children: [
                      const Icon(Icons.calendar_today, size: 20),
                      const SizedBox(width: 8),
                      Text('${_selectedDate.day}.${_selectedDate.month}.${_selectedDate.year}'),
                    ],
                  ),
                ),
              ),
              const SizedBox(width: 16),
              ElevatedButton(
                onPressed: _selectDate,
                style: ElevatedButton.styleFrom(
                  backgroundColor: const Color(0xFF9C27B0),
                  foregroundColor: Colors.white,
                ),
                child: const Text('Getir'),
              ),
            ],
          ),
          const SizedBox(height: 20),
          
          // Grafik başlığı
          Text(
            'Günlük Kategori Satışları (${_selectedDate.year}-${_selectedDate.month.toString().padLeft(2, '0')}-${_selectedDate.day.toString().padLeft(2, '0')})',
            style: const TextStyle(
              fontSize: 16,
              fontWeight: FontWeight.w500,
            ),
          ),
          const SizedBox(height: 16),
          
          // Pasta grafik
          SizedBox(
            height: 200,
            child: _buildCategoryPieChart(),
          ),
        ],
      ),
    );
  }

  Widget _buildEmployeePerformanceSection() {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(12),
        boxShadow: [
          BoxShadow(
            color: Colors.grey.withOpacity(0.1),
            spreadRadius: 1,
            blurRadius: 10,
            offset: const Offset(0, 2),
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              const Icon(Icons.people, color: Color(0xFF9C27B0)),
              const SizedBox(width: 8),
              const Text(
                'Çalışan Performans Raporu',
                style: TextStyle(
                  fontSize: 18,
                  fontWeight: FontWeight.bold,
                ),
              ),
            ],
          ),
          const SizedBox(height: 8),
          const Text(
            'Çalışan Performans Raporu',
            style: TextStyle(
              fontSize: 14,
              color: Colors.grey,
            ),
          ),
          const SizedBox(height: 20),
          
          // En iyi çalışan
          Container(
            padding: const EdgeInsets.all(16),
            decoration: BoxDecoration(
              color: Colors.amber[100],
              borderRadius: BorderRadius.circular(8),
            ),
            child: Row(
              children: [
                const Icon(Icons.emoji_events, color: Colors.amber),
                const SizedBox(width: 8),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      const Text(
                        'Admin User',
                        style: TextStyle(
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                      Text(
                        'Toplam Satış: ₺0 | Sipariş Sayısı: 0',
                        style: TextStyle(
                          fontSize: 12,
                          color: Colors.grey[600],
                        ),
                      ),
                    ],
                  ),
                ),
              ],
            ),
          ),
          const SizedBox(height: 16),
          
          // Performans tablosu - Daha temiz ve düzenli
          Container(
            decoration: BoxDecoration(
              border: Border.all(color: Colors.grey[300]!),
              borderRadius: BorderRadius.circular(8),
            ),
            child: Column(
              children: [
                // Tablo başlığı
                Container(
                  padding: const EdgeInsets.all(16),
                  decoration: BoxDecoration(
                    color: const Color(0xFF9C27B0),
                    borderRadius: const BorderRadius.only(
                      topLeft: Radius.circular(8),
                      topRight: Radius.circular(8),
                    ),
                  ),
                  child: const Row(
                    children: [
                      Expanded(
                        flex: 2,
                        child: Text(
                          'Çalışan', 
                          style: TextStyle(
                            fontWeight: FontWeight.bold,
                            color: Colors.white,
                            fontSize: 14,
                          )
                        )
                      ),
                      Expanded(
                        flex: 2,
                        child: Text(
                          'Toplam Satış', 
                          style: TextStyle(
                            fontWeight: FontWeight.bold,
                            color: Colors.white,
                            fontSize: 14,
                          )
                        )
                      ),
                      Expanded(
                        flex: 2,
                        child: Text(
                          'Sipariş Sayısı', 
                          style: TextStyle(
                            fontWeight: FontWeight.bold,
                            color: Colors.white,
                            fontSize: 14,
                          )
                        )
                      ),
                      Expanded(
                        flex: 2,
                        child: Text(
                          'Ortalama Sipariş', 
                          style: TextStyle(
                            fontWeight: FontWeight.bold,
                            color: Colors.white,
                            fontSize: 14,
                          )
                        )
                      ),
                      Expanded(
                        flex: 2,
                        child: Text(
                          'Müşteri Sayısı', 
                          style: TextStyle(
                            fontWeight: FontWeight.bold,
                            color: Colors.white,
                            fontSize: 14,
                          )
                        )
                      ),
                    ],
                  ),
                ),
                // Tablo satırları
                ..._employeePerformance.map((employee) => Container(
                  padding: const EdgeInsets.all(16),
                  decoration: BoxDecoration(
                    border: Border(top: BorderSide(color: Colors.grey[300]!)),
                  ),
                  child: Row(
                    children: [
                      Expanded(
                        flex: 2,
                        child: Row(
                          children: [
                            Expanded(
                              child: Text(
                                employee['name'],
                                style: const TextStyle(fontWeight: FontWeight.w500),
                                overflow: TextOverflow.ellipsis,
                              ),
                            ),
                            if (employee['isBest'] == true) ...[
                              const SizedBox(width: 8),
                              Container(
                                padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                                decoration: BoxDecoration(
                                  color: Colors.amber,
                                  borderRadius: BorderRadius.circular(12),
                                ),
                                child: const Text(
                                  'En İyi',
                                  style: TextStyle(
                                    fontSize: 10,
                                    color: Colors.white,
                                    fontWeight: FontWeight.bold,
                                  ),
                                ),
                              ),
                            ],
                          ],
                        ),
                      ),
                      Expanded(
                        flex: 2,
                        child: Text(
                          '₺${employee['totalSales']}',
                          style: const TextStyle(fontWeight: FontWeight.w500),
                        )
                      ),
                      Expanded(
                        flex: 2,
                        child: Text(
                          '${employee['orderCount']}',
                          style: const TextStyle(fontWeight: FontWeight.w500),
                        )
                      ),
                      Expanded(
                        flex: 2,
                        child: Text(
                          '₺${employee['avgOrder'].toStringAsFixed(2)}',
                          style: const TextStyle(fontWeight: FontWeight.w500),
                        )
                      ),
                      Expanded(
                        flex: 2,
                        child: Text(
                          '${employee['customerCount']}',
                          style: const TextStyle(fontWeight: FontWeight.w500),
                        )
                      ),
                    ],
                  ),
                )).toList(),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildRevenueDetailsSection() {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(12),
        boxShadow: [
          BoxShadow(
            color: Colors.grey.withOpacity(0.1),
            spreadRadius: 1,
            blurRadius: 10,
            offset: const Offset(0, 2),
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              const Icon(Icons.account_balance_wallet, color: Color(0xFF9C27B0)),
              const SizedBox(width: 8),
              const Text(
                'Ciro Detayları',
                style: TextStyle(
                  fontSize: 18,
                  fontWeight: FontWeight.bold,
                ),
              ),
            ],
          ),
          const SizedBox(height: 8),
          const Text(
            'Günlük ciro raporu',
            style: TextStyle(
              fontSize: 14,
              color: Colors.grey,
            ),
          ),
          const SizedBox(height: 20),
          
          // Toplam ciro kartı
          Container(
            width: double.infinity,
            padding: const EdgeInsets.all(24),
            decoration: BoxDecoration(
              color: Colors.green,
              borderRadius: BorderRadius.circular(12),
            ),
            child: Column(
              children: [
                Row(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    const Icon(Icons.account_balance_wallet, color: Colors.white, size: 32),
                    const SizedBox(width: 12),
                    const Text(
                      'Toplam Ciro',
                      style: TextStyle(
                        fontSize: 20,
                        fontWeight: FontWeight.bold,
                        color: Colors.white,
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 16),
                const Text(
                  '₺0,00',
                  style: TextStyle(
                    fontSize: 36,
                    fontWeight: FontWeight.bold,
                    color: Colors.white,
                  ),
                ),
                const SizedBox(height: 8),
                const Text(
                  'Günlük toplam gelir',
                  style: TextStyle(
                    fontSize: 14,
                    color: Colors.white70,
                  ),
                ),
              ],
            ),
          ),
          const SizedBox(height: 20),
          
          // Filtre butonları
          Row(
            mainAxisAlignment: MainAxisAlignment.end,
            children: _periods.take(3).map((period) {
              final isSelected = period == _selectedPeriod;
              return Padding(
                padding: const EdgeInsets.only(left: 8),
                child: ElevatedButton(
                  onPressed: () {
                    setState(() {
                      _selectedPeriod = period;
                    });
                  },
                  style: ElevatedButton.styleFrom(
                    backgroundColor: isSelected ? const Color(0xFF9C27B0) : Colors.grey[200],
                    foregroundColor: isSelected ? Colors.white : Colors.black87,
                    padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                  ),
                  child: Text(period),
                ),
              );
            }).toList(),
          ),
          const SizedBox(height: 20),
          
          // Ciro detay tablosu
          Container(
            decoration: BoxDecoration(
              border: Border.all(color: Colors.grey[300]!),
              borderRadius: BorderRadius.circular(8),
            ),
            child: Column(
              children: [
                // Tablo başlığı
                Container(
                  padding: const EdgeInsets.all(12),
                  decoration: BoxDecoration(
                    color: Colors.grey[100],
                    borderRadius: const BorderRadius.only(
                      topLeft: Radius.circular(8),
                      topRight: Radius.circular(8),
                    ),
                  ),
                  child: const Row(
                    children: [
                      Expanded(child: Text('Tarih', style: TextStyle(fontWeight: FontWeight.bold))),
                      Expanded(child: Text('Kategori', style: TextStyle(fontWeight: FontWeight.bold))),
                      Expanded(child: Text('Açıklama', style: TextStyle(fontWeight: FontWeight.bold))),
                      Expanded(child: Text('Tutar', style: TextStyle(fontWeight: FontWeight.bold))),
                      Expanded(child: Text('Ödeme Yöntemi', style: TextStyle(fontWeight: FontWeight.bold))),
                    ],
                  ),
                ),
                // Boş veri mesajı
                Container(
                  padding: const EdgeInsets.all(40),
                  child: const Text(
                    'Bugün için ciro verisi bulunamadı.',
                    style: TextStyle(
                      color: Colors.grey,
                      fontSize: 16,
                    ),
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildBarChart() {
    if (_dailySales.isEmpty) {
      return const Center(
        child: Text(
          'Henüz satış verisi bulunamadı.',
          style: TextStyle(color: Colors.grey),
        ),
      );
    }

    final maxRevenue = _dailySales.fold<double>(
      0.0,
      (max, item) => (item['revenue'] ?? 0.0) > max ? (item['revenue'] ?? 0.0) : max,
    );

    return Column(
      children: _dailySales.map((day) {
        final revenue = (day['revenue'] ?? 0.0).toDouble();
        final orders = day['orders'] ?? 0;
        final customers = day['customers'] ?? 0;
        final barHeight = maxRevenue > 0 ? (revenue / maxRevenue) * 100 : 0.0;

        return Container(
          margin: const EdgeInsets.only(bottom: 16),
            child: Row(
              children: [
              // Gün adı
                SizedBox(
                width: 120,
                child: Text(
                  day['date'] ?? '',
                  style: const TextStyle(fontSize: 12),
                  ),
                ),
                const SizedBox(width: 16),
              
                // Çubuk grafik
                Expanded(
                child: Column(
                  children: [
                                         SizedBox(
                       height: 80,
                  child: Row(
                        children: [
                          // Revenue çubuğu
                          Expanded(
                            flex: revenue > 0 ? (revenue / maxRevenue * 100).round() : 0,
                            child: Container(
                            decoration: BoxDecoration(
                                color: Colors.blue,
                              borderRadius: BorderRadius.circular(4),
                            ),
                              child: Center(
                            child: Text(
                                  '₺${revenue.toStringAsFixed(0)}',
                                  style: const TextStyle(
                                    color: Colors.white,
                                    fontSize: 10,
                                    fontWeight: FontWeight.bold,
                                  ),
                                ),
                              ),
                            ),
                          ),
                          // Boş alan
                          Expanded(
                            flex: revenue > 0 ? ((100 - (revenue / maxRevenue * 100)).round()) : 100,
                            child: Container(),
                          ),
                        ],
                      ),
                    ),
                    const SizedBox(height: 8),
                    
                    // Alt bilgiler
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceAround,
                      children: [
                        Text(
                          'Sipariş: $orders',
                          style: const TextStyle(fontSize: 10, color: Colors.grey),
                        ),
                        Text(
                          'Müşteri: $customers',
                          style: const TextStyle(fontSize: 10, color: Colors.grey),
                        ),
                      ],
                    ),
              ],
            ),
          ),
        ],
      ),
        );
      }).toList(),
    );
  }

  Widget _buildPieChart() {
    return SizedBox(
      height: 200,
      child: Row(
        children: [
          // Pasta grafik simülasyonu
          Expanded(
            child: Container(
              height: 150,
              decoration: BoxDecoration(
                shape: BoxShape.circle,
                color: Colors.blue,
              ),
              child: Stack(
                children: [
                  // İlk yarı (Cheeseburger)
                  Container(
                    decoration: const BoxDecoration(
                      shape: BoxShape.circle,
                      color: Colors.blue,
                    ),
                  ),
                  // İkinci yarı (Türk Kahvesi)
                  Container(
                    decoration: const BoxDecoration(
                      shape: BoxShape.circle,
                      color: Colors.pink,
                    ),
                    child: const ClipOval(
                      child: Align(
                        alignment: Alignment.centerRight,
                        child: SizedBox(
                          width: 75,
                          height: 150,
                          child: ColoredBox(color: Colors.transparent),
                        ),
                      ),
                    ),
                  ),
                ],
              ),
            ),
          ),
          const SizedBox(width: 20),
          // Lejant
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                _buildLegendItem('Türk Kahvesi', Colors.pink),
                const SizedBox(height: 8),
                _buildLegendItem('Cheeseburger', Colors.blue),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildCategoryPieChart() {
    return Row(
      children: [
        // Pasta grafik simülasyonu
        Expanded(
          child: Container(
            height: 150,
            decoration: BoxDecoration(
              shape: BoxShape.circle,
              color: Colors.blue,
            ),
            child: Stack(
              children: [
                // Ana yemekler (büyük dilim)
                Container(
                  decoration: const BoxDecoration(
                    shape: BoxShape.circle,
                    color: Colors.blue,
                  ),
                ),
                // Tatlılar (küçük dilim)
                Container(
                  decoration: const BoxDecoration(
                    shape: BoxShape.circle,
                    color: Colors.red,
                  ),
                  child: const ClipOval(
                    child: Align(
                      alignment: Alignment.topRight,
                      child: SizedBox(
                        width: 30,
                        height: 30,
                        child: ColoredBox(color: Colors.transparent),
                      ),
                    ),
                  ),
                ),
                // İçecekler (küçük dilim)
                Container(
                  decoration: const BoxDecoration(
                    shape: BoxShape.circle,
                    color: Colors.yellow,
                  ),
                  child: const ClipOval(
                    child: Align(
                      alignment: Alignment.bottomRight,
                      child: SizedBox(
                        width: 20,
                        height: 20,
                        child: ColoredBox(color: Colors.transparent),
                      ),
                    ),
                  ),
                ),
              ],
            ),
          ),
        ),
        const SizedBox(width: 20),
        // Lejant
        Expanded(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              _buildLegendItem('desserts', Colors.red),
              const SizedBox(height: 8),
              _buildLegendItem('main_dishes', Colors.blue),
              const SizedBox(height: 8),
              _buildLegendItem('drinks', Colors.yellow),
            ],
          ),
        ),
      ],
    );
  }

  Widget _buildLegendItem(String label, Color color) {
    return Row(
      children: [
        Container(
          width: 16,
          height: 16,
          decoration: BoxDecoration(
            color: color,
            shape: BoxShape.circle,
          ),
        ),
        const SizedBox(width: 8),
        Expanded(
          child: Text(
            label,
            style: const TextStyle(fontSize: 12),
            overflow: TextOverflow.ellipsis,
          ),
        ),
      ],
    );
  }

  Future<void> _selectDate() async {
    final DateTime? picked = await showDatePicker(
      context: context,
      initialDate: _selectedDate,
      firstDate: DateTime.now().subtract(const Duration(days: 365)),
      lastDate: DateTime.now(),
    );
    
    if (picked != null) {
      setState(() {
        _selectedDate = picked;
      });
    }
  }

  // Ürün stok kısmını oluştur
  Widget _buildProductStockSection() {
    return Container(
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(12),
        boxShadow: [
          BoxShadow(
            color: Colors.grey.withOpacity(0.1),
            spreadRadius: 1,
            blurRadius: 10,
            offset: const Offset(0, 2),
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              const Text(
                'Ürün Stok Durumu',
                style: TextStyle(
                  fontSize: 20,
                  fontWeight: FontWeight.bold,
                ),
              ),
              ElevatedButton.icon(
                onPressed: _showAddProductDialog,
                icon: const Icon(Icons.add),
                label: const Text('Yeni Ürün Ekle'),
                style: ElevatedButton.styleFrom(
                  backgroundColor: Colors.green,
                  foregroundColor: Colors.white,
                ),
              ),
            ],
          ),
          const SizedBox(height: 20),
          
          if (_isLoadingProductStock)
            const Center(child: CircularProgressIndicator())
          else if (_productStockError != null)
            Container(
              padding: const EdgeInsets.all(20),
              child: Text(
                'Hata: $_productStockError',
                style: const TextStyle(color: Colors.red),
              ),
            )
          else if (_productStock.isEmpty)
            const Center(
              child: Padding(
                padding: EdgeInsets.all(40.0),
                child: Text(
                  'Henüz ürün bulunamadı.',
                  style: TextStyle(color: Colors.grey),
                ),
              ),
            )
          else
            SingleChildScrollView(
              scrollDirection: Axis.horizontal,
              child: DataTable(
                columns: const [
                  DataColumn(label: Text('Ürün Adı')),
                  DataColumn(label: Text('Kategori')),
                  DataColumn(label: Text('Fiyat')),
                  DataColumn(label: Text('Stok Durumu')),
                  DataColumn(label: Text('İşlemler')),
                ],
                rows: _productStock.map((product) {
                  final quantity = _productQuantities.firstWhere(
                    (q) => q['productId'] == product['id'],
                    orElse: () => {'amount': 0},
                  )['amount'];
                  
                  return DataRow(
                    cells: [
                      DataCell(Text(product['name'] ?? '')),
                      DataCell(Text(product['category'] ?? '')),
                      DataCell(Text('₺${(product['price'] ?? 0.0).toStringAsFixed(2)}')),
                      DataCell(Text('$quantity')),
                      DataCell(
                        Row(
                          mainAxisSize: MainAxisSize.min,
                          children: [
                            IconButton(
                              icon: const Icon(Icons.edit, size: 20),
                              onPressed: () => _showEditProductDialog(product),
                              tooltip: 'Düzenle',
                            ),
                            IconButton(
                              icon: const Icon(Icons.delete, size: 20),
                              onPressed: () => _deleteProduct(product['id']),
                              tooltip: 'Sil',
                              color: Colors.red,
                            ),
                          ],
                        ),
                      ),
                    ],
                  );
                }).toList(),
              ),
            ),
        ],
      ),
    );
  }

  // Yeni ürün ekleme dialog'u
  void _showAddProductDialog() {
    showDialog(
      context: context,
      builder: (BuildContext context) {
        final TextEditingController nameController = TextEditingController();
        final TextEditingController descriptionController = TextEditingController();
        final TextEditingController priceController = TextEditingController();
        final TextEditingController categoryController = TextEditingController();

        return AlertDialog(
          title: const Text('Yeni Ürün Ekle'),
          content: SingleChildScrollView(
            child: ListBody(
              children: <Widget>[
                TextField(
                  controller: nameController,
                  decoration: const InputDecoration(labelText: 'Ürün Adı'),
                ),
                TextField(
                  controller: descriptionController,
                  decoration: const InputDecoration(labelText: 'Açıklama'),
                ),
                TextField(
                  controller: priceController,
                  keyboardType: TextInputType.number,
                  decoration: const InputDecoration(labelText: 'Fiyat'),
                ),
                TextField(
                  controller: categoryController,
                  decoration: const InputDecoration(labelText: 'Kategori'),
                ),
              ],
            ),
          ),
          actions: <Widget>[
            TextButton(
              child: const Text('İptal'),
              onPressed: () {
                Navigator.of(context).pop();
              },
            ),
            ElevatedButton(
              child: const Text('Ekle'),
              onPressed: () async {
                final name = nameController.text;
                final description = descriptionController.text;
                final price = double.tryParse(priceController.text) ?? 0.0;
                final category = categoryController.text;

                if (name.isNotEmpty && category.isNotEmpty) {
                  try {
                    final productData = {
                      'name': name,
                      'description': description,
                      'price': price,
                      'isActive': true,
                      'category': category,
                    };
                    
                    await AnalyticsService.createProduct(productData);
                    ScaffoldMessenger.of(context).showSnackBar(
                      const SnackBar(content: Text('Ürün başarıyla eklendi!')),
                    );
                    _loadProductStock();
                  } catch (e) {
                    ScaffoldMessenger.of(context).showSnackBar(
                      SnackBar(content: Text('Ürün eklenirken hata: $e')),
                    );
                  }
                } else {
                  ScaffoldMessenger.of(context).showSnackBar(
                    const SnackBar(content: Text('Lütfen ürün adı ve kategori alanlarını doldurun.')),
                  );
                }
                Navigator.of(context).pop();
              },
            ),
          ],
        );
      },
    );
  }

  // Ürün düzenleme dialog'u
  void _showEditProductDialog(Map<String, dynamic> product) {
    final TextEditingController nameController = TextEditingController(text: product['name']);
    final TextEditingController descriptionController = TextEditingController(text: product['description'] ?? '');
    final TextEditingController priceController = TextEditingController(text: (product['price'] ?? 0.0).toString());
    final TextEditingController categoryController = TextEditingController(text: product['category'] ?? '');

    showDialog(
      context: context,
      builder: (BuildContext context) {
        return AlertDialog(
          title: const Text('Ürün Düzenle'),
          content: SingleChildScrollView(
            child: ListBody(
              children: <Widget>[
                TextField(
                  controller: nameController,
                  decoration: const InputDecoration(labelText: 'Ürün Adı'),
                ),
                TextField(
                  controller: descriptionController,
                  decoration: const InputDecoration(labelText: 'Açıklama'),
                ),
                TextField(
                  controller: priceController,
                  keyboardType: TextInputType.number,
                  decoration: const InputDecoration(labelText: 'Fiyat'),
                ),
                TextField(
                  controller: categoryController,
                  decoration: const InputDecoration(labelText: 'Kategori'),
                ),
              ],
            ),
          ),
          actions: <Widget>[
            TextButton(
              child: const Text('İptal'),
              onPressed: () {
                Navigator.of(context).pop();
              },
            ),
            ElevatedButton(
              child: const Text('Kaydet'),
              onPressed: () async {
                final name = nameController.text;
                final description = descriptionController.text;
                final price = double.tryParse(priceController.text) ?? 0.0;
                final category = categoryController.text;

                if (name.isNotEmpty && category.isNotEmpty) {
                  try {
                    final productData = {
                      'name': name,
                      'description': description,
                      'price': price,
                      'isActive': true,
                      'category': category,
                    };
                    
                    await AnalyticsService.updateProduct(product['id'], productData);
                    ScaffoldMessenger.of(context).showSnackBar(
                      const SnackBar(content: Text('Ürün başarıyla güncellendi!')),
                    );
                    _loadProductStock();
                  } catch (e) {
                    ScaffoldMessenger.of(context).showSnackBar(
                      SnackBar(content: Text('Ürün güncellenirken hata: $e')),
                    );
                  }
                } else {
                  ScaffoldMessenger.of(context).showSnackBar(
                    const SnackBar(content: Text('Lütfen ürün adı ve kategori alanlarını doldurun.')),
                  );
                }
                Navigator.of(context).pop();
              },
            ),
          ],
        );
      },
    );
  }

  // Ürün silme
  Future<void> _deleteProduct(int productId) async {
    final bool? confirm = await showDialog<bool>(
      context: context,
      builder: (BuildContext context) {
        return AlertDialog(
          title: const Text('Ürün Sil'),
          content: const Text('Bu ürünü silmek istediğinize emin misiniz?'),
          actions: <Widget>[
            TextButton(
              child: const Text('İptal'),
              onPressed: () {
                Navigator.of(context).pop(false);
              },
            ),
            ElevatedButton(
              child: const Text('Sil'),
              onPressed: () {
                Navigator.of(context).pop(true);
              },
            ),
          ],
        );
      },
    );

    if (confirm == true) {
      try {
        await AnalyticsService.deleteProduct(productId);
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Ürün başarıyla silindi!')),
        );
        _loadProductStock();
      } catch (e) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Ürün silinirken hata: $e')),
        );
      }
    }
  }
}
