import 'package:flutter/material.dart';

class CashierScreen extends StatefulWidget {
  const CashierScreen({super.key});

  @override
  State<CashierScreen> createState() => _CashierScreenState();
}

class _CashierScreenState extends State<CashierScreen> {
  int _selectedIndex = 0;
  
  // Sipariş verileri
  final List<Map<String, dynamic>> _orders = [
    {
      'id': 'ORD-001',
      'tableNumber': 1,
      'customerName': 'Ahmet Yılmaz',
      'items': [
        {'name': 'Cheeseburger', 'quantity': 2, 'price': 45.0},
        {'name': 'Cola', 'quantity': 2, 'price': 15.0},
        {'name': 'Patates Kızartması', 'quantity': 1, 'price': 25.0},
      ],
      'totalAmount': 145.0,
      'status': 'Hazırlanıyor',
      'orderTime': DateTime.now().subtract(const Duration(minutes: 15)),
    },
    {
      'id': 'ORD-002',
      'tableNumber': 3,
      'customerName': 'Fatma Demir',
      'items': [
        {'name': 'Pizza Margherita', 'quantity': 1, 'price': 65.0},
        {'name': 'Su', 'quantity': 1, 'price': 8.0},
      ],
      'totalAmount': 73.0,
      'status': 'Tamamlandı',
      'orderTime': DateTime.now().subtract(const Duration(minutes: 30)),
    },
    {
      'id': 'ORD-003',
      'tableNumber': 5,
      'customerName': 'Mehmet Kaya',
      'items': [
        {'name': 'Tavuk Şiş', 'quantity': 1, 'price': 55.0},
        {'name': 'Pilav', 'quantity': 1, 'price': 20.0},
        {'name': 'Ayran', 'quantity': 1, 'price': 12.0},
      ],
      'totalAmount': 87.0,
      'status': 'Bekliyor',
      'orderTime': DateTime.now().subtract(const Duration(minutes: 5)),
    },
  ];

  // Ödeme yöntemleri
  final List<String> _paymentMethods = ['Nakit', 'Kredi Kartı', 'Banka Kartı', 'Mobil Ödeme'];

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Kasiyer Paneli'),
        backgroundColor: Colors.orange[700],
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
          // Sol sidebar - Sipariş listesi
          Expanded(
            flex: 2,
            child: _buildOrdersList(),
          ),
          // Sağ panel - Sipariş detayı ve ödeme
          Expanded(
            flex: 3,
            child: _buildOrderDetails(),
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
            icon: Icon(Icons.receipt_long),
            label: 'Siparişler',
          ),
          BottomNavigationBarItem(
            icon: Icon(Icons.payment),
            label: 'Ödemeler',
          ),
          BottomNavigationBarItem(
            icon: Icon(Icons.people),
            label: 'Müşteriler',
          ),
          BottomNavigationBarItem(
            icon: Icon(Icons.analytics),
            label: 'Raporlar',
          ),
        ],
      ),
    );
  }

  Widget _buildOrdersList() {
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
                const Icon(Icons.receipt_long, color: Colors.orange),
                const SizedBox(width: 8),
                const Text(
                  'Aktif Siparişler',
                  style: TextStyle(
                    fontSize: 18,
                    fontWeight: FontWeight.bold,
                  ),
                ),
                const Spacer(),
                Text(
                  '${_orders.length} sipariş',
                  style: TextStyle(
                    color: Colors.grey[600],
                    fontSize: 14,
                  ),
                ),
              ],
            ),
          ),
          // Sipariş listesi
          Expanded(
            child: ListView.builder(
              itemCount: _orders.length,
              itemBuilder: (context, index) {
                final order = _orders[index];
                return _buildOrderCard(order, index);
              },
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildOrderCard(Map<String, dynamic> order, int index) {
    final statusColor = _getStatusColor(order['status']);
    
    return Card(
      margin: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
      child: InkWell(
        onTap: () {
          setState(() {
            _selectedIndex = index;
          });
        },
        child: Container(
          padding: const EdgeInsets.all(12),
          decoration: BoxDecoration(
            border: Border(
              left: BorderSide(
                color: statusColor,
                width: 4,
              ),
            ),
          ),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                children: [
                  Text(
                    'Sipariş ${order['id']}',
                    style: const TextStyle(
                      fontWeight: FontWeight.bold,
                      fontSize: 16,
                    ),
                  ),
                  const Spacer(),
                  Container(
                    padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                    decoration: BoxDecoration(
                      color: statusColor.withOpacity(0.1),
                      borderRadius: BorderRadius.circular(12),
                    ),
                    child: Text(
                      order['status'],
                      style: TextStyle(
                        color: statusColor,
                        fontSize: 12,
                        fontWeight: FontWeight.w500,
                      ),
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 8),
              Text(
                'Masa ${order['tableNumber']} - ${order['customerName']}',
                style: TextStyle(
                  color: Colors.grey[600],
                  fontSize: 14,
                ),
              ),
              const SizedBox(height: 4),
              Text(
                '₺${order['totalAmount'].toStringAsFixed(2)}',
                style: const TextStyle(
                  fontSize: 18,
                  fontWeight: FontWeight.bold,
                  color: Colors.green,
                ),
              ),
              const SizedBox(height: 4),
              Text(
                '${order['items'].length} ürün',
                style: TextStyle(
                  color: Colors.grey[500],
                  fontSize: 12,
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildOrderDetails() {
    if (_orders.isEmpty) {
      return const Center(
        child: Text('Henüz sipariş bulunmuyor'),
      );
    }

    final selectedOrder = _orders[_selectedIndex];
    
    return Container(
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Sipariş başlığı
          Row(
            children: [
              Icon(
                Icons.receipt_long,
                size: 32,
                color: Colors.orange[700],
              ),
              const SizedBox(width: 12),
              Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    'Sipariş ${selectedOrder['id']}',
                    style: const TextStyle(
                      fontSize: 24,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                  Text(
                    'Masa ${selectedOrder['tableNumber']} - ${selectedOrder['customerName']}',
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
                  color: _getStatusColor(selectedOrder['status']).withOpacity(0.1),
                  borderRadius: BorderRadius.circular(20),
                ),
                child: Text(
                  selectedOrder['status'],
                  style: TextStyle(
                    color: _getStatusColor(selectedOrder['status']),
                    fontWeight: FontWeight.bold,
                  ),
                ),
              ),
            ],
          ),
          const SizedBox(height: 24),
          
          // Sipariş detayları
          Expanded(
            child: SingleChildScrollView(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  // Ürün listesi
                  const Text(
                    'Sipariş Detayları',
                    style: TextStyle(
                      fontSize: 18,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                  const SizedBox(height: 12),
                  Container(
                    decoration: BoxDecoration(
                      border: Border.all(color: Colors.grey[300]!),
                      borderRadius: BorderRadius.circular(8),
                    ),
                    child: Column(
                      children: [
                        for (int i = 0; i < selectedOrder['items'].length; i++)
                          _buildOrderItem(selectedOrder['items'][i], i == selectedOrder['items'].length - 1),
                      ],
                    ),
                  ),
                  const SizedBox(height: 24),
                  
                  // Toplam
                  Container(
                    padding: const EdgeInsets.all(16),
                    decoration: BoxDecoration(
                      color: Colors.grey[100],
                      borderRadius: BorderRadius.circular(8),
                    ),
                    child: Row(
                      children: [
                        const Text(
                          'TOPLAM:',
                          style: TextStyle(
                            fontSize: 20,
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                        const Spacer(),
                        Text(
                          '₺${selectedOrder['totalAmount'].toStringAsFixed(2)}',
                          style: const TextStyle(
                            fontSize: 24,
                            fontWeight: FontWeight.bold,
                            color: Colors.green,
                          ),
                        ),
                      ],
                    ),
                  ),
                  const SizedBox(height: 24),
                  
                  // Ödeme işlemleri
                  if (selectedOrder['status'] == 'Tamamlandı')
                    _buildPaymentSection(selectedOrder)
                  else
                    _buildOrderActions(selectedOrder),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildOrderItem(Map<String, dynamic> item, bool isLast) {
    return Container(
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        border: Border(
          bottom: isLast ? BorderSide.none : BorderSide(color: Colors.grey[300]!),
        ),
      ),
      child: Row(
        children: [
          Expanded(
            flex: 3,
            child: Text(
              item['name'],
              style: const TextStyle(fontSize: 16),
            ),
          ),
          Expanded(
            flex: 1,
            child: Text(
              '${item['quantity']}x',
              textAlign: TextAlign.center,
              style: TextStyle(
                color: Colors.grey[600],
                fontWeight: FontWeight.w500,
              ),
            ),
          ),
          Expanded(
            flex: 2,
            child: Text(
              '₺${item['price'].toStringAsFixed(2)}',
              textAlign: TextAlign.right,
              style: const TextStyle(
                fontWeight: FontWeight.w500,
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildOrderActions(Map<String, dynamic> order) {
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
                  // Sipariş durumunu güncelle
                  _updateOrderStatus(order['id'], 'Hazırlanıyor');
                },
                icon: const Icon(Icons.restaurant),
                label: const Text('Hazırlanıyor'),
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
                  // Sipariş durumunu güncelle
                  _updateOrderStatus(order['id'], 'Tamamlandı');
                },
                icon: const Icon(Icons.check_circle),
                label: const Text('Tamamlandı'),
                style: ElevatedButton.styleFrom(
                  backgroundColor: Colors.green,
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

  Widget _buildPaymentSection(Map<String, dynamic> order) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const Text(
          'Ödeme İşlemi',
          style: TextStyle(
            fontSize: 18,
            fontWeight: FontWeight.bold,
          ),
        ),
        const SizedBox(height: 12),
        
        // Ödeme yöntemi seçimi
        DropdownButtonFormField<String>(
          decoration: const InputDecoration(
            labelText: 'Ödeme Yöntemi',
            border: OutlineInputBorder(),
          ),
          value: _paymentMethods[0],
          items: _paymentMethods.map((method) {
            return DropdownMenuItem(
              value: method,
              child: Text(method),
            );
          }).toList(),
          onChanged: (value) {
            // Ödeme yöntemi seçimi
          },
        ),
        const SizedBox(height: 16),
        
        // Ödeme butonu
        SizedBox(
          width: double.infinity,
          child: ElevatedButton.icon(
            onPressed: () {
              _processPayment(order);
            },
            icon: const Icon(Icons.payment),
            label: const Text(
              'Ödemeyi Tamamla',
              style: TextStyle(fontSize: 18),
            ),
            style: ElevatedButton.styleFrom(
              backgroundColor: Colors.green,
              foregroundColor: Colors.white,
              padding: const EdgeInsets.symmetric(vertical: 16),
            ),
          ),
        ),
      ],
    );
  }

  Color _getStatusColor(String status) {
    switch (status) {
      case 'Bekliyor':
        return Colors.orange;
      case 'Hazırlanıyor':
        return Colors.blue;
      case 'Tamamlandı':
        return Colors.green;
      case 'İptal Edildi':
        return Colors.red;
      default:
        return Colors.grey;
    }
  }

  void _updateOrderStatus(String orderId, String newStatus) {
    setState(() {
      final orderIndex = _orders.indexWhere((order) => order['id'] == orderId);
      if (orderIndex != -1) {
        _orders[orderIndex]['status'] = newStatus;
      }
    });
    
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text('Sipariş durumu güncellendi: $newStatus'),
        backgroundColor: Colors.green,
      ),
    );
  }

  void _processPayment(Map<String, dynamic> order) {
    // Ödeme işlemi simülasyonu
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Ödeme Başarılı'),
        content: Text('Sipariş ${order['id']} için ödeme tamamlandı.'),
        actions: [
          TextButton(
            onPressed: () {
              Navigator.of(context).pop();
              // Siparişi kapat
              _closeOrder(order['id']);
            },
            child: const Text('Tamam'),
          ),
        ],
      ),
    );
  }

  void _closeOrder(String orderId) {
    setState(() {
      _orders.removeWhere((order) => order['id'] == orderId);
      if (_selectedIndex >= _orders.length) {
        _selectedIndex = _orders.length - 1;
      }
    });
    
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(
        content: Text('Sipariş kapatıldı'),
        backgroundColor: Colors.green,
      ),
    );
  }
}

