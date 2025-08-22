import 'package:flutter/material.dart';
import 'package:restaurant_app/screens/waiter/waiter_settings_screen.dart';

class WaiterHomeScreen extends StatefulWidget {
  const WaiterHomeScreen({Key? key}) : super(key: key);

  @override
  State<WaiterHomeScreen> createState() => _WaiterHomeScreenState();
}

class _WaiterHomeScreenState extends State<WaiterHomeScreen> {
  int _selectedFloor = 0;
  List<Map<String, dynamic>> _tables = [];
  List<Map<String, dynamic>> _products = [];
  List<Map<String, dynamic>> _currentOrders = [];

  @override
  void initState() {
    super.initState();
    _loadMockData();
  }

  void _loadMockData() {
    // Mock masa verileri
    _tables = [
      {'id': '1', 'name': 'Masa 1', 'status': 'empty', 'floor': 1, 'capacity': 4},
      {'id': '2', 'name': 'Masa 2', 'status': 'occupied', 'floor': 1, 'capacity': 6},
      {'id': '3', 'name': 'Masa 3', 'status': 'reserved', 'floor': 1, 'capacity': 4},
      {'id': '4', 'name': 'Masa 4', 'status': 'empty', 'floor': 1, 'capacity': 8},
      {'id': '5', 'name': 'Masa 5', 'status': 'occupied', 'floor': 2, 'capacity': 4},
      {'id': '6', 'name': 'Masa 6', 'status': 'reserved_soon', 'floor': 2, 'capacity': 6},
      {'id': '7', 'name': 'Masa 7', 'status': 'empty', 'floor': 2, 'capacity': 4},
      {'id': '8', 'name': 'Masa 8', 'status': 'empty', 'floor': 2, 'capacity': 8},
      {'id': '9', 'name': 'Masa 9', 'status': 'empty', 'floor': 3, 'capacity': 4},
      {'id': '10', 'name': 'Masa 10', 'status': 'empty', 'floor': 3, 'capacity': 6},
      {'id': '11', 'name': 'Masa 11', 'status': 'empty', 'floor': 3, 'capacity': 8},
      {'id': '12', 'name': 'Masa 12', 'status': 'empty', 'floor': 3, 'capacity': 4},
    ];

    // Mock ürün verileri
    _products = [
      {'id': 1, 'name': 'Karışık Pizza', 'price': 45.0, 'category': 'Ana Yemek'},
      {'id': 2, 'name': 'Hamburger', 'price': 35.0, 'category': 'Ana Yemek'},
      {'id': 3, 'name': 'Salata', 'price': 25.0, 'category': 'Salata'},
      {'id': 4, 'name': 'Cola', 'price': 8.0, 'category': 'İçecek'},
      {'id': 5, 'name': 'Su', 'price': 5.0, 'category': 'İçecek'},
      {'id': 6, 'name': 'Patates', 'price': 15.0, 'category': 'Yan Ürün'},
      {'id': 7, 'name': 'Köfte', 'price': 40.0, 'category': 'Ana Yemek'},
      {'id': 8, 'name': 'Çorba', 'price': 20.0, 'category': 'Çorba'},
    ];

    // Mock sipariş verileri
    _currentOrders = [
      {'id': '1', 'tableId': '2', 'tableName': 'Masa 2', 'items': [{'id': 2, 'name': 'Hamburger', 'price': 35.0, 'quantity': 2}, {'id': 4, 'name': 'Cola', 'price': 8.0, 'quantity': 2}], 'totalPrice': 86.0, 'status': 'preparing'},
      {'id': '2', 'tableId': '5', 'tableName': 'Masa 5', 'items': [{'id': 1, 'name': 'Karışık Pizza', 'price': 45.0, 'quantity': 1}, {'id': 5, 'name': 'Su', 'price': 5.0, 'quantity': 2}], 'totalPrice': 55.0, 'status': 'ready'},
    ];
  }

  Color _getTableColor(String status) {
    switch (status) {
      case 'empty':
        return Colors.green;
      case 'occupied':
        return Colors.red;
      case 'reserved':
        return Colors.blue;
      case 'reserved_soon':
        return Colors.orange; // 1 saat kala turuncu
      default:
        return Colors.grey;
    }
  }

  String _getTableStatus(String status) {
    switch (status) {
      case 'empty':
        return 'Boş';
      case 'occupied':
        return 'Dolu';
      case 'reserved':
        return 'Rezerve';
      case 'reserved_soon':
        return 'Yaklaşan Rezervasyon';
      default:
        return 'Bilinmiyor';
    }
  }

  void _onTableTap(Map<String, dynamic> table) {
    if (table['status'] == 'occupied') {
      _showTableDetails(table);
    } else {
      _showNewOrderDialog(table);
    }
  }

  void _showTableDetails(Map<String, dynamic> table) {
    final currentOrder = _currentOrders.firstWhere(
      (order) => order['tableId'] == table['id'],
      orElse: () => <String, dynamic>{},
    );

    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: Row(
          children: [
            Icon(
              Icons.table_restaurant,
              color: _getTableColor(table['status']),
              size: 24,
            ),
            const SizedBox(width: 8),
            Text('Masa ${table['name']}'),
          ],
        ),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Table status
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
              decoration: BoxDecoration(
                color: _getTableColor(table['status']),
                borderRadius: BorderRadius.circular(20),
              ),
              child: Text(
                _getTableStatus(table['status']),
                style: const TextStyle(
                  color: Colors.white,
                  fontWeight: FontWeight.bold,
                ),
              ),
            ),
            const SizedBox(height: 16),

            // Table info
            Text('Kapasite: ${table['capacity']} kişi'),
            const SizedBox(height: 8),

            // Current order info
            if (currentOrder.isNotEmpty) ...[
              const Divider(),
              const Text(
                'Mevcut Sipariş:',
                style: TextStyle(fontWeight: FontWeight.bold),
              ),
              const SizedBox(height: 8),
              ...currentOrder['items'].map<Widget>((item) => Padding(
                padding: const EdgeInsets.symmetric(vertical: 2),
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Text('${item['name']} x${item['quantity']}'),
                    Text('₺${item['price'].toStringAsFixed(2)}'),
                  ],
                ),
              )).toList(),
              const Divider(),
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  const Text('Toplam:', style: TextStyle(fontWeight: FontWeight.bold)),
                  Text('₺${currentOrder['totalPrice'].toStringAsFixed(2)}', style: const TextStyle(fontWeight: FontWeight.bold)),
                ],
              ),
              const SizedBox(height: 16),
              Row(
                children: [
                  Expanded(
                    child: ElevatedButton.icon(
                      onPressed: () => _showOrderActionsDialog(currentOrder),
                      icon: const Icon(Icons.more_horiz),
                      label: const Text('Sipariş İşlemleri'),
                      style: ElevatedButton.styleFrom(
                        backgroundColor: Colors.blue,
                        foregroundColor: Colors.white,
                      ),
                    ),
                  ),
                  const SizedBox(width: 8),
                  Expanded(
                    child: ElevatedButton.icon(
                      onPressed: () => _showAddProductDialog(table),
                      icon: const Icon(Icons.add),
                      label: const Text('Ürün Ekle'),
                      style: ElevatedButton.styleFrom(
                        backgroundColor: Colors.green,
                        foregroundColor: Colors.white,
                      ),
                    ),
                  ),
                ],
              ),
            ] else ...[
              const SizedBox(height: 16),
              ElevatedButton.icon(
                onPressed: () => _showNewOrderDialog(table),
                icon: const Icon(Icons.add_shopping_cart),
                label: const Text('Yeni Sipariş'),
                style: ElevatedButton.styleFrom(
                  backgroundColor: Colors.green,
                  foregroundColor: Colors.white,
                ),
              ),
            ],
          ],
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(context).pop(),
            child: const Text('Kapat'),
          ),
        ],
      ),
    );
  }

  void _showNewOrderDialog(Map<String, dynamic> table) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: Text('Masa ${table['name']} - Yeni Sipariş'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Text('Masa Kapasitesi: ${table['capacity']} Kişilik'),
            const SizedBox(height: 16),
            const Text(
              'Ürün Seç:',
              style: TextStyle(fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 16),
            DropdownButtonFormField<String>(
              decoration: const InputDecoration(
                labelText: 'Ürün Seç',
                border: OutlineInputBorder(),
              ),
              value: null,
              items: _products.map<DropdownMenuItem<String>>((product) {
                return DropdownMenuItem<String>(
                  value: product['id'].toString(),
                  child: Text('${product['name']} - ₺${product['price'].toStringAsFixed(2)}'),
                );
              }).toList(),
              onChanged: (value) {
                if (value != null) {
                  final product = _products.firstWhere((p) => p['id'].toString() == value);
                  _createNewOrder(table, product);
                  Navigator.of(context).pop();
                }
              },
            ),
          ],
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(context).pop(),
            child: const Text('Kapat'),
          ),
        ],
      ),
    );
  }

  void _showAddProductDialog(Map<String, dynamic> table) {
    final currentOrder = _currentOrders.firstWhere(
      (order) => order['tableId'] == table['id'],
      orElse: () => <String, dynamic>{},
    );

    if (currentOrder.isEmpty) return;

    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: Text('Masa ${table['name']} - Ürün Ekle'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            // Current order items
            const Text(
              'Mevcut Ürünler:',
              style: TextStyle(fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 8),
            ...currentOrder['items'].map<Widget>((item) => Padding(
              padding: const EdgeInsets.symmetric(vertical: 4),
              child: Row(
                children: [
                  Expanded(
                    child: Text('${item['name']} x${item['quantity']}'),
                  ),
                  IconButton(
                    onPressed: () => _removeProductFromOrder(currentOrder, item),
                    icon: const Icon(Icons.remove_circle, color: Colors.red),
                    iconSize: 20,
                  ),
                  IconButton(
                    onPressed: () => _addProductToOrder(currentOrder, item),
                    icon: const Icon(Icons.add_circle, color: Colors.green),
                    iconSize: 20,
                  ),
                ],
              ),
            )).toList(),
            const Divider(),
            
            // Add new product
            const Text(
              'Yeni Ürün Ekle:',
              style: TextStyle(fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 16),
            DropdownButtonFormField<String>(
              decoration: const InputDecoration(
                labelText: 'Ürün Seç',
                border: OutlineInputBorder(),
              ),
              value: null,
              items: _products.map<DropdownMenuItem<String>>((product) {
                return DropdownMenuItem<String>(
                  value: product['id'].toString(),
                  child: Text('${product['name']} - ₺${product['price'].toStringAsFixed(2)}'),
                );
              }).toList(),
              onChanged: (value) {
                if (value != null) {
                  final product = _products.firstWhere((p) => p['id'].toString() == value);
                  _addProductToOrder(currentOrder, product);
                  Navigator.of(context).pop();
                }
              },
            ),
          ],
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(context).pop(),
            child: const Text('Kapat'),
          ),
        ],
      ),
    );
  }

  void _addProductToOrder(Map<String, dynamic> order, Map<String, dynamic> product) {
    setState(() {
      final existingItemIndex = order['items'].indexWhere((item) => item['id'] == product['id']);
      
      if (existingItemIndex != -1) {
        order['items'][existingItemIndex]['quantity']++;
      } else {
        order['items'].add({
          'id': product['id'],
          'name': product['name'],
          'price': product['price'],
          'quantity': 1,
        });
      }
      
      // Update total price
      order['totalPrice'] = order['items'].fold<double>(
        0.0, 
        (sum, item) => sum + (item['price'] * item['quantity'])
      );
    });
  }

  void _removeProductFromOrder(Map<String, dynamic> order, Map<String, dynamic> product) {
    setState(() {
      final existingItemIndex = order['items'].indexWhere((item) => item['id'] == product['id']);
      
      if (existingItemIndex != -1) {
        if (order['items'][existingItemIndex]['quantity'] > 1) {
          order['items'][existingItemIndex]['quantity']--;
        } else {
          order['items'].removeAt(existingItemIndex);
        }
        
        // Update total price
        order['totalPrice'] = order['items'].fold<double>(
          0.0, 
          (sum, item) => sum + (item['price'] * item['quantity'])
        );
        
        // If no items left, remove the order
        if (order['items'].isEmpty) {
          _currentOrders.removeWhere((o) => o['id'] == order['id']);
          // Update table status to empty
          final tableIndex = _tables.indexWhere((t) => t['id'] == order['tableId']);
          if (tableIndex != -1) {
            _tables[tableIndex]['status'] = 'empty';
          }
        }
      }
    });
  }

  void _createNewOrder(Map<String, dynamic> table, Map<String, dynamic> product) {
    setState(() {
      final orderId = DateTime.now().millisecondsSinceEpoch.toString();
      final newOrder = {
        'id': orderId,
        'tableId': table['id'],
        'tableName': table['name'],
        'items': [{
          'id': product['id'],
          'name': product['name'],
          'price': product['price'],
          'quantity': 1,
        }],
        'totalPrice': product['price'],
        'status': 'preparing',
        'createdAt': DateTime.now(),
      };
      
      _currentOrders.add(newOrder);
      
      // Update table status to occupied
      final tableIndex = _tables.indexWhere((t) => t['id'] == table['id']);
      if (tableIndex != -1) {
        _tables[tableIndex]['status'] = 'occupied';
      }
    });
    
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text('Masa ${table['name']} için yeni sipariş oluşturuldu'),
        backgroundColor: Colors.green,
      ),
    );
  }

  void _showOrderActionsDialog(Map<String, dynamic> order) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: Text('Sipariş İşlemleri - ${order['tableName']}'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            // Order status
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
              decoration: BoxDecoration(
                color: _getOrderStatusColor(order['status']),
                borderRadius: BorderRadius.circular(20),
              ),
              child: Text(
                _getOrderStatusText(order['status']),
                style: const TextStyle(
                  color: Colors.white,
                  fontWeight: FontWeight.bold,
                ),
              ),
            ),
            const SizedBox(height: 16),
            
            // Order items
            const Text(
              'Sipariş Detayları:',
              style: TextStyle(fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 8),
            ...order['items'].map<Widget>((item) => Padding(
              padding: const EdgeInsets.symmetric(vertical: 2),
              child: Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Text('${item['name']} x${item['quantity']}'),
                  Text('₺${item['price'].toStringAsFixed(2)}'),
                ],
              ),
            )).toList(),
            const Divider(),
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                const Text('Toplam:', style: TextStyle(fontWeight: FontWeight.bold)),
                Text('₺${order['totalPrice'].toStringAsFixed(2)}', style: const TextStyle(fontWeight: FontWeight.bold)),
              ],
            ),
            const SizedBox(height: 16),
            
            // Status update buttons
            Row(
              children: [
                Expanded(
                  child: ElevatedButton(
                    onPressed: () => _updateOrderStatus(order, 'preparing'),
                    style: ElevatedButton.styleFrom(
                      backgroundColor: order['status'] == 'preparing' ? Colors.orange : Colors.grey,
                      foregroundColor: Colors.white,
                    ),
                    child: const Text('Hazırlanıyor'),
                  ),
                ),
                const SizedBox(width: 8),
                Expanded(
                  child: ElevatedButton(
                    onPressed: () => _updateOrderStatus(order, 'ready'),
                    style: ElevatedButton.styleFrom(
                      backgroundColor: order['status'] == 'ready' ? Colors.blue : Colors.grey,
                      foregroundColor: Colors.white,
                    ),
                    child: const Text('Hazır'),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 8),
            SizedBox(
              width: double.infinity,
              child: ElevatedButton(
                onPressed: () => _completeOrder(order),
                style: ElevatedButton.styleFrom(
                  backgroundColor: Colors.green,
                  foregroundColor: Colors.white,
                ),
                child: const Text('Siparişi Tamamla'),
              ),
            ),
          ],
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(context).pop(),
            child: const Text('Kapat'),
          ),
        ],
      ),
    );
  }

  Color _getOrderStatusColor(String status) {
    switch (status) {
      case 'preparing':
        return Colors.orange;
      case 'ready':
        return Colors.blue;
      case 'completed':
        return Colors.green;
      default:
        return Colors.grey;
    }
  }

  String _getOrderStatusText(String status) {
    switch (status) {
      case 'preparing':
        return 'Hazırlanıyor';
      case 'ready':
        return 'Hazır';
      case 'completed':
        return 'Tamamlandı';
      default:
        return 'Bilinmiyor';
    }
  }

  void _updateOrderStatus(Map<String, dynamic> order, String newStatus) {
    setState(() {
      order['status'] = newStatus;
    });
    
    Navigator.of(context).pop();
    
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text('Sipariş durumu güncellendi: ${_getOrderStatusText(newStatus)}'),
        backgroundColor: Colors.blue,
      ),
    );
  }

  void _completeOrder(Map<String, dynamic> order) {
    setState(() {
      order['status'] = 'completed';
      _currentOrders.removeWhere((o) => o['id'] == order['id']);
      
      // Update table status to empty
      final tableIndex = _tables.indexWhere((t) => t['id'] == order['tableId']);
      if (tableIndex != -1) {
        _tables[tableIndex]['status'] = 'empty';
      }
    });
    
    Navigator.of(context).pop();
    
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text('Sipariş tamamlandı: ${order['tableName']}'),
        backgroundColor: Colors.green,
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final floorTables = _tables.where((table) => table['floor'] == _selectedFloor + 1).toList();
    
    return Scaffold(
      appBar: AppBar(
        title: const Text('Garson Ana Sayfa'),
        backgroundColor: Colors.orange,
        foregroundColor: Colors.white,
        actions: [
          IconButton(
            icon: const Icon(Icons.settings),
            onPressed: () {
              Navigator.push(
                context,
                MaterialPageRoute(
                  builder: (context) => const WaiterSettingsScreen(),
                ),
              );
            },
          ),
        ],
      ),
      body: Column(
        children: [
          // Floor selector
          Container(
            padding: const EdgeInsets.all(16),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceEvenly,
              children: [
                for (int i = 0; i < 3; i++)
                  ElevatedButton(
                    onPressed: () => setState(() => _selectedFloor = i),
                    style: ElevatedButton.styleFrom(
                      backgroundColor: _selectedFloor == i ? Colors.orange : Colors.grey,
                      foregroundColor: Colors.white,
                    ),
                    child: Text('${i + 1}. Kat'),
                  ),
              ],
            ),
          ),

          // Floor statistics
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 16),
            child: Row(
              children: [
                Expanded(
                  child: _buildStatCard(
                    'Boş Masalar',
                    '${floorTables.where((t) => t['status'] == 'empty').length}',
                    Colors.green,
                  ),
                ),
                const SizedBox(width: 8),
                Expanded(
                  child: _buildStatCard(
                    'Dolu Masalar',
                    '${floorTables.where((t) => t['status'] == 'occupied').length}',
                    Colors.red,
                  ),
                ),
                const SizedBox(width: 8),
                Expanded(
                  child: _buildStatCard(
                    'Rezerve',
                    '${floorTables.where((t) => t['status'] == 'reserved' || t['status'] == 'reserved_soon').length}',
                    Colors.blue,
                  ),
                ),
              ],
            ),
          ),

          const SizedBox(height: 16),

          // Tables grid
          Expanded(
            child: GridView.builder(
              padding: const EdgeInsets.all(16),
              gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
                crossAxisCount: 3,
                childAspectRatio: 1.2,
                crossAxisSpacing: 16,
                mainAxisSpacing: 16,
              ),
              itemCount: floorTables.length,
              itemBuilder: (context, index) {
                final table = floorTables[index];
                return _buildTableCard(table);
              },
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildStatCard(String title, String value, Color color) {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: color.withOpacity(0.1),
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: color.withOpacity(0.3)),
      ),
      child: Column(
        children: [
          Text(
            value,
            style: TextStyle(
              fontSize: 24,
              fontWeight: FontWeight.bold,
              color: color,
            ),
          ),
          const SizedBox(height: 4),
          Text(
            title,
            style: TextStyle(
              fontSize: 12,
              color: color.withOpacity(0.8),
            ),
            textAlign: TextAlign.center,
          ),
        ],
      ),
    );
  }

  Widget _buildTableCard(Map<String, dynamic> table) {
    return GestureDetector(
      onTap: () => _onTableTap(table),
      child: Container(
        decoration: BoxDecoration(
          color: _getTableColor(table['status']).withOpacity(0.1),
          borderRadius: BorderRadius.circular(12),
          border: Border.all(
            color: _getTableColor(table['status']),
            width: 2,
          ),
        ),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(
              Icons.table_restaurant,
              size: 32,
              color: _getTableColor(table['status']),
            ),
            const SizedBox(height: 8),
            Text(
              table['name'],
              style: const TextStyle(
                fontWeight: FontWeight.bold,
                fontSize: 16,
              ),
            ),
            const SizedBox(height: 4),
            Text(
              _getTableStatus(table['status']),
              style: TextStyle(
                fontSize: 12,
                color: _getTableColor(table['status']),
              ),
            ),
            const SizedBox(height: 4),
            Text(
              '${table['capacity']} kişi',
              style: const TextStyle(
                fontSize: 10,
                color: Colors.grey,
              ),
            ),
          ],
        ),
      ),
    );
  }
}
