import 'package:flutter/material.dart';

class WaiterScreen extends StatefulWidget {
  const WaiterScreen({super.key});

  @override
  State<WaiterScreen> createState() => _WaiterScreenState();
}

class _WaiterScreenState extends State<WaiterScreen> {
  int _selectedIndex = 0;
  
  // Masa verileri
  final List<Map<String, dynamic>> _tables = [
    {
      'id': 1,
      'number': 1,
      'capacity': 4,
      'status': 'Dolu',
      'customerName': 'Ahmet Yılmaz',
      'orderCount': 2,
      'totalAmount': 145.0,
      'lastOrderTime': DateTime.now().subtract(const Duration(minutes: 15)),
      'notes': 'Müşteri acil istiyor',
    },
    {
      'id': 2,
      'number': 2,
      'capacity': 2,
      'status': 'Boş',
      'customerName': null,
      'orderCount': 0,
      'totalAmount': 0.0,
      'lastOrderTime': null,
      'notes': '',
    },
    {
      'id': 3,
      'number': 3,
      'capacity': 6,
      'status': 'Dolu',
      'customerName': 'Fatma Demir',
      'orderCount': 1,
      'totalAmount': 73.0,
      'lastOrderTime': DateTime.now().subtract(const Duration(minutes: 30)),
      'notes': '',
    },
    {
      'id': 4,
      'number': 4,
      'capacity': 4,
      'status': 'Rezervasyon',
      'customerName': 'Mehmet Kaya',
      'orderCount': 0,
      'totalAmount': 0.0,
      'lastOrderTime': null,
      'notes': '19:00 rezervasyon',
    },
    {
      'id': 5,
      'number': 5,
      'capacity': 2,
      'status': 'Dolu',
      'customerName': 'Ayşe Özkan',
      'orderCount': 3,
      'totalAmount': 87.0,
      'lastOrderTime': DateTime.now().subtract(const Duration(minutes: 5)),
      'notes': 'Çocuklu aile',
    },
    {
      'id': 6,
      'number': 6,
      'capacity': 8,
      'status': 'Boş',
      'customerName': null,
      'orderCount': 0,
      'totalAmount': 0.0,
      'lastOrderTime': null,
      'notes': '',
    },
  ];

  // Menü kategorileri
  final List<Map<String, dynamic>> _menuCategories = [
    {
      'name': 'Ana Yemekler',
      'items': [
        {'name': 'Cheeseburger', 'price': 45.0, 'description': 'Dana eti, cheddar peyniri, marul, domates'},
        {'name': 'Pizza Margherita', 'price': 65.0, 'description': 'Domates sosu, mozzarella peyniri, fesleğen'},
        {'name': 'Tavuk Şiş', 'price': 55.0, 'description': 'Marine edilmiş tavuk, sebzeler ile'},
        {'name': 'Pilav', 'price': 20.0, 'description': 'Tereyağlı özel pilav'},
      ],
    },
    {
      'name': 'İçecekler',
      'items': [
        {'name': 'Cola', 'price': 15.0, 'description': 'Soğuk gazlı içecek'},
        {'name': 'Su', 'price': 8.0, 'description': 'Doğal kaynak suyu'},
        {'name': 'Ayran', 'price': 12.0, 'description': 'Taze ayran'},
        {'name': 'Türk Kahvesi', 'price': 18.0, 'description': 'Geleneksel Türk kahvesi'},
      ],
    },
    {
      'name': 'Tatlılar',
      'items': [
        {'name': 'Tiramisu', 'price': 25.0, 'description': 'İtalyan tatlısı'},
        {'name': 'Cheesecake', 'price': 22.0, 'description': 'Krem peynirli tatlı'},
        {'name': 'Dondurma', 'price': 15.0, 'description': 'Vanilya, çikolata, çilek'},
      ],
    },
  ];

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Garson Paneli'),
        backgroundColor: Colors.green[700],
        foregroundColor: Colors.white,
        actions: [
          IconButton(
            icon: const Icon(Icons.notifications),
            onPressed: () {
              // Bildirimler
            },
          ),
          IconButton(
            icon: const Icon(Icons.logout),
            onPressed: () {
              // Çıkış
            },
          ),
        ],
      ),
      body: Row(
        children: [
          // Sol sidebar - Masa listesi
          Expanded(
            flex: 2,
            child: _buildTablesList(),
          ),
          // Sağ panel - Masa detayı ve sipariş
          Expanded(
            flex: 3,
            child: _buildTableDetails(),
          ),
        ],
      ),
      bottomNavigationBar: BottomNavigationBar(
        type: BottomNavigationBarType.fixed,
        currentIndex: _selectedIndex,
        onTap: (index) {
          setState(() {
            _selectedIndex = index;
          });
        },
        items: const [
          BottomNavigationBarItem(
            icon: Icon(Icons.table_restaurant),
            label: 'Masalar',
          ),
          BottomNavigationBarItem(
            icon: Icon(Icons.receipt_long),
            label: 'Siparişler',
          ),
          BottomNavigationBarItem(
            icon: Icon(Icons.people),
            label: 'Müşteriler',
          ),
          BottomNavigationBarItem(
            icon: Icon(Icons.settings),
            label: 'Ayarlar',
          ),
        ],
      ),
    );
  }

  Widget _buildTablesList() {
    return Container(
      decoration: BoxDecoration(
        border: Border(
          right: BorderSide(color: Colors.grey[300]!),
        ),
      ),
      child: Column(
        children: [
          // Başlık
          Container(
            padding: const EdgeInsets.all(16),
            decoration: BoxDecoration(
              color: Colors.grey[100],
              border: Border(
                bottom: BorderSide(color: Colors.grey[300]!),
              ),
            ),
            child: Row(
              children: [
                const Icon(Icons.table_restaurant, color: Colors.green),
                const SizedBox(width: 8),
                const Text(
                  'Masa Durumu',
                  style: TextStyle(
                    fontSize: 18,
                    fontWeight: FontWeight.bold,
                  ),
                ),
                const Spacer(),
                Text(
                  '${_tables.length} masa',
                  style: TextStyle(
                    color: Colors.grey[600],
                    fontSize: 14,
                  ),
                ),
              ],
            ),
          ),
          // Masa listesi
          Expanded(
            child: GridView.builder(
              padding: const EdgeInsets.all(8),
              gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
                crossAxisCount: 2,
                childAspectRatio: 1.2,
                crossAxisSpacing: 8,
                mainAxisSpacing: 8,
              ),
              itemCount: _tables.length,
              itemBuilder: (context, index) {
                final table = _tables[index];
                return _buildTableCard(table, index);
              },
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildTableCard(Map<String, dynamic> table, int index) {
    final statusColor = _getTableStatusColor(table['status']);
    final isSelected = _selectedIndex == index;
    
    return Card(
      elevation: isSelected ? 4 : 2,
      color: isSelected ? Colors.green[50] : Colors.white,
      child: InkWell(
        onTap: () {
          setState(() {
            _selectedIndex = index;
          });
        },
        child: Container(
          padding: const EdgeInsets.all(12),
          decoration: BoxDecoration(
            border: Border.all(
              color: isSelected ? Colors.green : Colors.grey[300]!,
              width: isSelected ? 2 : 1,
            ),
            borderRadius: BorderRadius.circular(8),
          ),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              // Masa numarası
              Text(
                'Masa ${table['number']}',
                style: const TextStyle(
                  fontSize: 18,
                  fontWeight: FontWeight.bold,
                ),
              ),
              const SizedBox(height: 4),
              
              // Kapasite
              Text(
                '${table['capacity']} kişilik',
                style: TextStyle(
                  color: Colors.grey[600],
                  fontSize: 12,
                ),
              ),
              const SizedBox(height: 8),
              
              // Durum
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                decoration: BoxDecoration(
                  color: statusColor.withOpacity(0.1),
                  borderRadius: BorderRadius.circular(12),
                ),
                child: Text(
                  table['status'],
                  style: TextStyle(
                    color: statusColor,
                    fontSize: 12,
                    fontWeight: FontWeight.w500,
                  ),
                ),
              ),
              
              // Müşteri bilgisi
              if (table['customerName'] != null) ...[
                const SizedBox(height: 8),
                Text(
                  table['customerName'],
                  style: const TextStyle(
                    fontSize: 12,
                    fontWeight: FontWeight.w500,
                  ),
                  textAlign: TextAlign.center,
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                ),
              ],
              
              // Sipariş bilgisi
              if (table['orderCount'] > 0) ...[
                const SizedBox(height: 4),
                Text(
                  '${table['orderCount']} sipariş',
                  style: TextStyle(
                    color: Colors.grey[500],
                    fontSize: 11,
                  ),
                ),
              ],
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildTableDetails() {
    if (_tables.isEmpty) {
      return const Center(
        child: Text('Henüz masa bulunmuyor'),
      );
    }

    final selectedTable = _tables[_selectedIndex];
    
    return Container(
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Masa başlığı
          Row(
            children: [
              Icon(
                Icons.table_restaurant,
                size: 32,
                color: Colors.green[700],
              ),
              const SizedBox(width: 12),
              Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    'Masa ${selectedTable['number']}',
                    style: const TextStyle(
                      fontSize: 24,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                  Text(
                    '${selectedTable['capacity']} kişilik',
                    style: TextStyle(
                      fontSize: 16,
                      color: Colors.grey[600],
                    ),
                  ),
                ],
              ),
              const Spacer(),
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                decoration: BoxDecoration(
                  color: _getTableStatusColor(selectedTable['status']).withOpacity(0.1),
                  borderRadius: BorderRadius.circular(20),
                ),
                child: Text(
                  selectedTable['status'],
                  style: TextStyle(
                    color: _getTableStatusColor(selectedTable['status']),
                    fontWeight: FontWeight.bold,
                  ),
                ),
              ),
            ],
          ),
          const SizedBox(height: 24),
          
          // Masa detayları
          Expanded(
            child: SingleChildScrollView(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  // Müşteri bilgileri
                  if (selectedTable['customerName'] != null) ...[
                    _buildCustomerInfo(selectedTable),
                    const SizedBox(height: 24),
                  ],
                  
                  // Notlar
                  if (selectedTable['notes'].isNotEmpty) ...[
                    _buildNotesSection(selectedTable),
                    const SizedBox(height: 24),
                  ],
                  
                  // Sipariş işlemleri
                  if (selectedTable['status'] == 'Dolu') ...[
                    _buildOrderSection(selectedTable),
                  ] else if (selectedTable['status'] == 'Boş') ...[
                    _buildTableActions(selectedTable),
                  ] else if (selectedTable['status'] == 'Rezervasyon') ...[
                    _buildReservationSection(selectedTable),
                  ],
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildCustomerInfo(Map<String, dynamic> table) {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.blue[50],
        borderRadius: BorderRadius.circular(8),
        border: Border.all(color: Colors.blue[200]!),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              const Icon(Icons.person, color: Colors.blue),
              const SizedBox(width: 8),
              const Text(
                'Müşteri Bilgileri',
                style: TextStyle(
                  fontSize: 18,
                  fontWeight: FontWeight.bold,
                ),
              ),
            ],
          ),
          const SizedBox(height: 12),
          Text(
            'Ad: ${table['customerName']}',
            style: const TextStyle(fontSize: 16),
          ),
          if (table['orderCount'] > 0) ...[
            const SizedBox(height: 4),
            Text(
              'Toplam Sipariş: ${table['orderCount']}',
              style: const TextStyle(fontSize: 16),
            ),
            const SizedBox(height: 4),
            Text(
              'Toplam Tutar: ₺${table['totalAmount'].toStringAsFixed(2)}',
              style: const TextStyle(
                fontSize: 16,
                fontWeight: FontWeight.bold,
                color: Colors.green,
              ),
            ),
          ],
        ],
      ),
    );
  }

  Widget _buildNotesSection(Map<String, dynamic> table) {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.orange[50],
        borderRadius: BorderRadius.circular(8),
        border: Border.all(color: Colors.orange[200]!),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              const Icon(Icons.note, color: Colors.orange),
              const SizedBox(width: 8),
              const Text(
                'Notlar',
                style: TextStyle(
                  fontSize: 18,
                  fontWeight: FontWeight.bold,
                ),
              ),
            ],
          ),
          const SizedBox(height: 12),
          Text(
            table['notes'],
            style: const TextStyle(fontSize: 16),
          ),
        ],
      ),
    );
  }

  Widget _buildOrderSection(Map<String, dynamic> table) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const Text(
          'Sipariş İşlemleri',
          style: TextStyle(
            fontSize: 18,
            fontWeight: FontWeight.bold,
          ),
        ),
        const SizedBox(height: 12),
        
        Row(
          children: [
            Expanded(
              child: ElevatedButton.icon(
                onPressed: () {
                  _showOrderDialog(table);
                },
                icon: const Icon(Icons.add_shopping_cart),
                label: const Text('Yeni Sipariş'),
                style: ElevatedButton.styleFrom(
                  backgroundColor: Colors.blue,
                  foregroundColor: Colors.white,
                  padding: const EdgeInsets.symmetric(vertical: 12),
                ),
              ),
            ),
            const SizedBox(width: 12),
            Expanded(
              child: ElevatedButton.icon(
                onPressed: () {
                  _showPaymentDialog(table);
                },
                icon: const Icon(Icons.payment),
                label: const Text('Ödeme Al'),
                style: ElevatedButton.styleFrom(
                  backgroundColor: Colors.green,
                  foregroundColor: Colors.white,
                  padding: const EdgeInsets.symmetric(vertical: 12),
                ),
              ),
            ),
          ],
        ),
        const SizedBox(height: 12),
        
        ElevatedButton.icon(
          onPressed: () {
            _clearTable(table['id']);
          },
          icon: const Icon(Icons.clear),
          label: const Text('Masayı Temizle'),
          style: ElevatedButton.styleFrom(
            backgroundColor: Colors.orange,
            foregroundColor: Colors.white,
            padding: const EdgeInsets.symmetric(vertical: 12),
          ),
        ),
      ],
    );
  }

  Widget _buildTableActions(Map<String, dynamic> table) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const Text(
          'Masa İşlemleri',
          style: TextStyle(
            fontSize: 18,
            fontWeight: FontWeight.bold,
          ),
        ),
        const SizedBox(height: 12),
        
        Row(
          children: [
            Expanded(
              child: ElevatedButton.icon(
                onPressed: () {
                  _showCustomerDialog(table);
                },
                icon: const Icon(Icons.person_add),
                label: const Text('Müşteri Ekle'),
                style: ElevatedButton.styleFrom(
                  backgroundColor: Colors.green,
                  foregroundColor: Colors.white,
                  padding: const EdgeInsets.symmetric(vertical: 12),
                ),
              ),
            ),
            const SizedBox(width: 12),
            Expanded(
              child: ElevatedButton.icon(
                onPressed: () {
                  _showReservationDialog(table);
                },
                icon: const Icon(Icons.event),
                label: const Text('Rezervasyon'),
                style: ElevatedButton.styleFrom(
                  backgroundColor: Colors.blue,
                  foregroundColor: Colors.white,
                  padding: const EdgeInsets.symmetric(vertical: 12),
                ),
              ),
            ),
          ],
        ),
      ],
    );
  }

  Widget _buildReservationSection(Map<String, dynamic> table) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const Text(
          'Rezervasyon Bilgileri',
          style: TextStyle(
            fontSize: 18,
            fontWeight: FontWeight.bold,
          ),
        ),
        const SizedBox(height: 12),
        
        Container(
          padding: const EdgeInsets.all(16),
          decoration: BoxDecoration(
            color: Colors.purple[50],
            borderRadius: BorderRadius.circular(8),
            border: Border.all(color: Colors.purple[200]!),
          ),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                'Müşteri: ${table['customerName']}',
                style: const TextStyle(fontSize: 16),
              ),
              const SizedBox(height: 8),
              Text(
                'Not: ${table['notes']}',
                style: const TextStyle(fontSize: 16),
              ),
            ],
          ),
        ),
        const SizedBox(height: 16),
        
        Row(
          children: [
            Expanded(
              child: ElevatedButton.icon(
                onPressed: () {
                  _confirmReservation(table['id']);
                },
                icon: const Icon(Icons.check),
                label: const Text('Rezervasyonu Onayla'),
                style: ElevatedButton.styleFrom(
                  backgroundColor: Colors.green,
                  foregroundColor: Colors.white,
                  padding: const EdgeInsets.symmetric(vertical: 12),
                ),
              ),
            ),
            const SizedBox(width: 12),
            Expanded(
              child: ElevatedButton.icon(
                onPressed: () {
                  _cancelReservation(table['id']);
                },
                icon: const Icon(Icons.cancel),
                label: const Text('İptal Et'),
                style: ElevatedButton.styleFrom(
                  backgroundColor: Colors.red,
                  foregroundColor: Colors.white,
                  padding: const EdgeInsets.symmetric(vertical: 12),
                ),
              ),
            ),
          ],
        ),
      ],
    );
  }

  Color _getTableStatusColor(String status) {
    switch (status) {
      case 'Boş':
        return Colors.green;
      case 'Dolu':
        return Colors.red;
      case 'Rezervasyon':
        return Colors.orange;
      case 'Temizleniyor':
        return Colors.yellow;
      default:
        return Colors.grey;
    }
  }

  void _showOrderDialog(Map<String, dynamic> table) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: Text('Masa ${table['number']} - Yeni Sipariş'),
        content: const Text('Sipariş ekranı açılacak...'),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(context).pop(),
            child: const Text('Tamam'),
          ),
        ],
      ),
    );
  }

  void _showPaymentDialog(Map<String, dynamic> table) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: Text('Masa ${table['number']} - Ödeme'),
        content: const Text('Ödeme ekranı açılacak...'),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(context).pop(),
            child: const Text('Tamam'),
          ),
        ],
      ),
    );
  }

  void _showCustomerDialog(Map<String, dynamic> table) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: Text('Masa ${table['number']} - Müşteri Ekle'),
        content: const Text('Müşteri ekleme ekranı açılacak...'),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(context).pop(),
            child: const Text('Tamam'),
          ),
        ],
      ),
    );
  }

  void _showReservationDialog(Map<String, dynamic> table) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: Text('Masa ${table['number']} - Rezervasyon'),
        content: const Text('Rezervasyon ekranı açılacak...'),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(context).pop(),
            child: const Text('Tamam'),
          ),
        ],
      ),
    );
  }

  void _clearTable(int tableId) {
    setState(() {
      final tableIndex = _tables.indexWhere((table) => table['id'] == tableId);
      if (tableIndex != -1) {
        _tables[tableIndex]['status'] = 'Boş';
        _tables[tableIndex]['customerName'] = null;
        _tables[tableIndex]['orderCount'] = 0;
        _tables[tableIndex]['totalAmount'] = 0.0;
        _tables[tableIndex]['lastOrderTime'] = null;
        _tables[tableIndex]['notes'] = '';
      }
    });
    
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(
        content: Text('Masa temizlendi'),
        backgroundColor: Colors.green,
      ),
    );
  }

  void _confirmReservation(int tableId) {
    setState(() {
      final tableIndex = _tables.indexWhere((table) => table['id'] == tableId);
      if (tableIndex != -1) {
        _tables[tableIndex]['status'] = 'Dolu';
      }
    });
    
    Navigator.of(context).pop();
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(
        content: Text('Rezervasyon onaylandı'),
        backgroundColor: Colors.green,
      ),
    );
  }

  void _cancelReservation(int tableId) {
    setState(() {
      final tableIndex = _tables.indexWhere((table) => table['id'] == tableId);
      if (tableIndex != -1) {
        _tables[tableIndex]['status'] = 'Boş';
        _tables[tableIndex]['customerName'] = null;
        _tables[tableIndex]['notes'] = '';
      }
    });
    
    Navigator.of(context).pop();
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(
        content: Text('Rezervasyon iptal edildi'),
        backgroundColor: Colors.orange,
      ),
    );
  }
}

