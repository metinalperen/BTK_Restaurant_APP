import 'package:flutter/material.dart';

class WaiterStockScreen extends StatefulWidget {
  const WaiterStockScreen({Key? key}) : super(key: key);

  @override
  State<WaiterStockScreen> createState() => _WaiterStockScreenState();
}

class _WaiterStockScreenState extends State<WaiterStockScreen> {
  List<Map<String, dynamic>> _products = [];
  List<Map<String, dynamic>> _stockMovements = [];
  String _selectedCategory = 'Tümü';
  String _searchQuery = '';

  @override
  void initState() {
    super.initState();
    _loadMockData();
  }

  void _loadMockData() {
    // Mock ürün verileri
    _products = [
      {
        'id': 1,
        'name': 'Karışık Pizza',
        'category': 'Ana Yemek',
        'price': 45.0,
        'description': 'Sucuk, sosis, mantar, biber ile',
        'currentStock': 25,
        'minStock': 10,
        'status': 'normal'
      },
      {
        'id': 2,
        'name': 'Hamburger',
        'category': 'Ana Yemek',
        'price': 35.0,
        'description': 'Dana eti, marul, domates, soğan',
        'currentStock': 18,
        'minStock': 15,
        'status': 'normal'
      },
      {
        'id': 3,
        'name': 'Salata',
        'category': 'Salata',
        'price': 25.0,
        'description': 'Marul, domates, salatalık, zeytin',
        'currentStock': 30,
        'minStock': 20,
        'status': 'normal'
      },
      {
        'id': 4,
        'name': 'Patates',
        'category': 'Yan Ürün',
        'price': 15.0,
        'description': 'Çıtır patates kızartması',
        'currentStock': 5,
        'minStock': 20,
        'status': 'critical'
      },
      {
        'id': 5,
        'name': 'Cola',
        'category': 'İçecek',
        'price': 8.0,
        'description': 'Soğuk içecek',
        'currentStock': 50,
        'minStock': 30,
        'status': 'normal'
      },
      {
        'id': 6,
        'name': 'Su',
        'category': 'İçecek',
        'price': 5.0,
        'description': 'Doğal kaynak suyu',
        'currentStock': 100,
        'minStock': 50,
        'status': 'normal'
      },
      {
        'id': 7,
        'name': 'Köfte',
        'category': 'Ana Yemek',
        'price': 40.0,
        'description': 'Izgara köfte, bulgur pilavı ile',
        'currentStock': 12,
        'minStock': 15,
        'status': 'low'
      },
      {
        'id': 8,
        'name': 'Çorba',
        'category': 'Çorba',
        'price': 20.0,
        'description': 'Günlük çorba',
        'currentStock': 35,
        'minStock': 25,
        'status': 'normal'
      },
    ];

    // Mock stok hareket verileri
    _stockMovements = [
      {
        'id': 1,
        'productName': 'Karışık Pizza',
        'change': 50,
        'reason': 'PURCHASE',
        'note': 'Yeni stok alımı',
        'timestamp': '2025-01-20 10:00'
      },
      {
        'id': 2,
        'productName': 'Hamburger',
        'change': -2,
        'reason': 'SALE',
        'note': 'Sipariş',
        'timestamp': '2025-01-20 11:30'
      },
      {
        'id': 3,
        'productName': 'Patates',
        'change': 100,
        'reason': 'PURCHASE',
        'note': 'Stok yenileme',
        'timestamp': '2025-01-20 09:15'
      },
    ];
  }

  List<Map<String, dynamic>> get _filteredProducts {
    return _products.where((product) {
      final matchesCategory = _selectedCategory == 'Tümü' || product['category'] == _selectedCategory;
      final matchesSearch = product['name'].toLowerCase().contains(_searchQuery.toLowerCase()) ||
                           product['description'].toLowerCase().contains(_searchQuery.toLowerCase());
      return matchesCategory && matchesSearch;
    }).toList();
  }

  Color _getStatusColor(String status) {
    switch (status) {
      case 'normal':
        return Colors.green;
      case 'low':
        return Colors.orange;
      case 'critical':
        return Colors.red;
      default:
        return Colors.grey;
    }
  }

  String _getStatusText(String status) {
    switch (status) {
      case 'normal':
        return 'Normal';
      case 'low':
        return 'Düşük Stok';
      case 'critical':
        return 'Kritik Stok';
      default:
        return 'Bilinmiyor';
    }
  }

  String _getReasonText(String reason) {
    switch (reason) {
      case 'PURCHASE':
        return 'Alım';
      case 'SALE':
        return 'Satış';
      case 'WASTE':
        return 'Fire';
      case 'ADJUSTMENT':
        return 'Düzeltme';
      default:
        return reason;
    }
  }

  @override
  Widget build(BuildContext context) {
    final categories = ['Tümü', ..._products.map((p) => p['category']).toSet().toList()];
    final totalProducts = _products.length;
    final normalStock = _products.where((p) => p['status'] == 'normal').length;
    final lowStock = _products.where((p) => p['status'] == 'low').length;
    final criticalStock = _products.where((p) => p['status'] == 'critical').length;

    return Scaffold(
      backgroundColor: Colors.grey[50],
      appBar: AppBar(
        title: Text('Stok Durumu'),
        backgroundColor: Colors.orange,
        foregroundColor: Colors.white,
        actions: [
          IconButton(
            icon: Icon(Icons.refresh),
            onPressed: _loadMockData,
          ),
        ],
      ),
      body: Column(
        children: [
          // Arama ve filtreleme
          Container(
            padding: EdgeInsets.all(16),
            color: Colors.white,
            child: Column(
              children: [
                // Arama çubuğu
                TextField(
                  decoration: InputDecoration(
                    hintText: 'Ürün ara...',
                    prefixIcon: Icon(Icons.search),
                    border: OutlineInputBorder(
                      borderRadius: BorderRadius.circular(12),
                    ),
                    filled: true,
                    fillColor: Colors.grey[100],
                  ),
                  onChanged: (value) => setState(() => _searchQuery = value),
                ),
                SizedBox(height: 16),
                // Kategori filtresi
                Row(
                  children: [
                    Text('Kategori: ', style: TextStyle(fontWeight: FontWeight.bold)),
                    SizedBox(width: 8),
                    Expanded(
                      child: DropdownButtonFormField<String>(
                        value: _selectedCategory,
                        decoration: InputDecoration(
                          border: OutlineInputBorder(borderRadius: BorderRadius.circular(8)),
                          contentPadding: EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                        ),
                        items: categories.map((category) => DropdownMenuItem<String>(
                          value: category,
                          child: Text(category),
                        )).toList(),
                        onChanged: (value) => setState(() => _selectedCategory = value!),
                      ),
                    ),
                  ],
                ),
              ],
            ),
          ),

          // Özet kartları
          Container(
            padding: EdgeInsets.symmetric(horizontal: 16),
            child: Column(
              children: [
                Row(
                  children: [
                    Expanded(
                      child: _buildSummaryCard(
                        'Toplam Ürün',
                        '$totalProducts',
                        Icons.inventory,
                        Colors.blue,
                      ),
                    ),
                    SizedBox(width: 12),
                    Expanded(
                      child: _buildSummaryCard(
                        'Normal Stok',
                        '$normalStock',
                        Icons.check_circle,
                        Colors.green,
                      ),
                    ),
                  ],
                ),
                SizedBox(height: 12),
                Row(
                  children: [
                    Expanded(
                      child: _buildSummaryCard(
                        'Düşük Stok',
                        '$lowStock',
                        Icons.warning,
                        Colors.orange,
                      ),
                    ),
                    SizedBox(width: 12),
                    Expanded(
                      child: _buildSummaryCard(
                        'Kritik Stok',
                        '$criticalStock',
                        Icons.error,
                        Colors.red,
                      ),
                    ),
                  ],
                ),
              ],
            ),
          ),

          SizedBox(height: 16),

          // Ürün listesi
          Expanded(
            child: ListView.builder(
              padding: EdgeInsets.symmetric(horizontal: 16),
              itemCount: _filteredProducts.length,
              itemBuilder: (context, index) {
                final product = _filteredProducts[index];
                return Card(
                  margin: EdgeInsets.only(bottom: 12),
                  child: ListTile(
                    leading: Container(
                      width: 40,
                      height: 40,
                      decoration: BoxDecoration(
                        color: _getStatusColor(product['status']),
                        shape: BoxShape.circle,
                      ),
                      child: Icon(
                        Icons.inventory,
                        color: Colors.white,
                        size: 20,
                      ),
                    ),
                    title: Text(
                      product['name'],
                      style: TextStyle(fontWeight: FontWeight.bold),
                    ),
                    subtitle: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text('${product['category']} - ${product['price']} ₺'),
                        Text(product['description']),
                        SizedBox(height: 4),
                        Text(
                          'Stok: ${product['currentStock']} adet / Min: ${product['minStock']} adet',
                          style: TextStyle(fontWeight: FontWeight.w500),
                        ),
                      ],
                    ),
                    trailing: Column(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        Container(
                          padding: EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                          decoration: BoxDecoration(
                            color: _getStatusColor(product['status']),
                            borderRadius: BorderRadius.circular(12),
                          ),
                          child: Text(
                            _getStatusText(product['status']),
                            style: TextStyle(
                              color: Colors.white,
                              fontSize: 12,
                              fontWeight: FontWeight.bold,
                            ),
                          ),
                        ),
                        SizedBox(height: 4),
                        Icon(Icons.arrow_forward_ios, size: 16, color: Colors.grey),
                      ],
                    ),
                    onTap: () => _showProductDetails(product),
                  ),
                );
              },
            ),
          ),
        ],
      ),
    );
  }

  void _showProductDetails(Map<String, dynamic> product) {
    final movements = _stockMovements.where((m) => m['productName'] == product['name']).toList();
    
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: Text(product['name']),
        content: Container(
          width: double.maxFinite,
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text('Kategori: ${product['category']}'),
              Text('Fiyat: ${product['price']} ₺'),
              Text('Açıklama: ${product['description']}'),
              SizedBox(height: 16),
              Text('Stok Bilgileri:', style: TextStyle(fontWeight: FontWeight.bold)),
              Text('Mevcut Stok: ${product['currentStock']} adet'),
              Text('Minimum Stok: ${product['minStock']} adet'),
              Text('Durum: ${_getStatusText(product['status'])}'),
              if (movements.isNotEmpty) ...[
                SizedBox(height: 16),
                Text('Son Hareketler:', style: TextStyle(fontWeight: FontWeight.bold)),
                ...movements.take(3).map((movement) => Text(
                  '• ${_getReasonText(movement['reason'])}: ${movement['change'] > 0 ? '+' : ''}${movement['change']} (${movement['timestamp']})',
                )),
              ],
            ],
          ),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: Text('Kapat'),
          ),
        ],
      ),
    );
  }

  Widget _buildSummaryCard(String title, String value, IconData icon, Color color) {
    return Container(
      padding: EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(16),
        boxShadow: [
          BoxShadow(
            color: Colors.grey.withOpacity(0.2),
            spreadRadius: 2,
            blurRadius: 6,
            offset: Offset(0, 3),
          ),
        ],
      ),
      child: Column(
        children: [
          Icon(icon, color: color, size: 36),
          SizedBox(height: 12),
          Text(
            value,
            style: TextStyle(
              fontSize: 28,
              fontWeight: FontWeight.bold,
              color: color,
            ),
          ),
          Text(
            title,
            style: TextStyle(
              fontSize: 14,
              color: Colors.grey[600],
              fontWeight: FontWeight.w500,
            ),
            textAlign: TextAlign.center,
          ),
        ],
      ),
    );
  }
}
