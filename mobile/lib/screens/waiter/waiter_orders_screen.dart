import 'package:flutter/material.dart';

class WaiterOrdersScreen extends StatefulWidget {
  const WaiterOrdersScreen({super.key});

  @override
  State<WaiterOrdersScreen> createState() => _WaiterOrdersScreenState();
}

class _WaiterOrdersScreenState extends State<WaiterOrdersScreen> {
  bool _isDailyView = true;
  String _selectedFilter = 'Tüm Siparişler';
  
  // Mock data for orders
  final List<Map<String, dynamic>> _dailyOrders = [
    {
      'id': '259',
      'tableNumber': '37',
      'price': 8000.00,
      'items': 'Fok Yemi x8',
      'status': 'devam_ediyor',
      'timestamp': '20 Ağustos 2025 11:08',
    },
    {
      'id': '258',
      'tableNumber': '33',
      'price': 200.00,
      'items': 'Köfte x5',
      'status': 'devam_ediyor',
      'timestamp': '20 Ağustos 2025 11:07',
    },
    {
      'id': '257',
      'tableNumber': '35',
      'price': 400.00,
      'items': 'Köfte x10',
      'status': 'devam_ediyor',
      'timestamp': '20 Ağustos 2025 11:05',
    },
    {
      'id': '256',
      'tableNumber': '28',
      'price': 180.00,
      'items': 'iced amaricano x1',
      'status': 'tamamlandi',
      'timestamp': '20 Ağustos 2025 10:48',
    },
  ];

  final List<Map<String, dynamic>> _monthlyOrders = [];

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Siparişlerim'),
        backgroundColor: Colors.purple[600],
        foregroundColor: Colors.white,
      ),
      body: Column(
        children: [
          // Tab buttons
          Container(
            padding: const EdgeInsets.all(16),
            child: Row(
              children: [
                Expanded(
                  child: GestureDetector(
                    onTap: () => setState(() => _isDailyView = true),
                    child: Container(
                      padding: const EdgeInsets.symmetric(vertical: 12),
                      decoration: BoxDecoration(
                        color: _isDailyView ? Colors.purple[300] : Colors.grey[300],
                        borderRadius: BorderRadius.circular(8),
                      ),
                      child: Text(
                        'Günlük Siparişler',
                        textAlign: TextAlign.center,
                        style: TextStyle(
                          color: _isDailyView ? Colors.white : Colors.grey[600],
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                    ),
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: GestureDetector(
                    onTap: () => setState(() => _isDailyView = false),
                    child: Container(
                      padding: const EdgeInsets.symmetric(vertical: 12),
                      decoration: BoxDecoration(
                        color: !_isDailyView ? Colors.purple[300] : Colors.grey[300],
                        borderRadius: BorderRadius.circular(8),
                      ),
                      child: Text(
                        'Aylık Siparişler',
                        textAlign: TextAlign.center,
                        style: TextStyle(
                          color: !_isDailyView ? Colors.white : Colors.grey[600],
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                    ),
                  ),
                ),
              ],
            ),
          ),

          // Filter dropdown
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 16),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.end,
              children: [
                Container(
                  padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                  decoration: BoxDecoration(
                    border: Border.all(color: Colors.purple[300]!),
                    borderRadius: BorderRadius.circular(8),
                  ),
                  child: Row(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      Text(
                        _selectedFilter,
                        style: TextStyle(color: Colors.purple[600]),
                      ),
                      const SizedBox(width: 8),
                      Icon(Icons.arrow_drop_down, color: Colors.purple[600]),
                    ],
                  ),
                ),
              ],
            ),
          ),

          // Main title
          Padding(
            padding: const EdgeInsets.all(16),
            child: Text(
              _isDailyView 
                ? 'Güncel Siparişlerim (20.08.2025)'
                : 'Aylık Siparişler',
              style: TextStyle(
                fontSize: 20,
                fontWeight: FontWeight.bold,
                color: Colors.purple[800],
              ),
            ),
          ),

          // Summary banner
          Container(
            margin: const EdgeInsets.symmetric(horizontal: 16),
            padding: const EdgeInsets.all(16),
            decoration: BoxDecoration(
              color: Colors.purple[100],
              borderRadius: BorderRadius.circular(12),
            ),
            child: Row(
              children: [
                Icon(Icons.analytics, color: Colors.purple[600], size: 24),
                const SizedBox(width: 12),
                Expanded(
                  child: Text(
                    _isDailyView 
                      ? 'Toplam Günlük Sipariş Sayısı: ${_dailyOrders.length}'
                      : 'Ağustos Ayı Toplam Sipariş Sayısı: ${_monthlyOrders.length}',
                    style: TextStyle(
                      fontSize: 16,
                      fontWeight: FontWeight.w600,
                      color: Colors.purple[800],
                    ),
                  ),
                ),
                Container(
                  padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
                  decoration: BoxDecoration(
                    color: Colors.green,
                    borderRadius: BorderRadius.circular(20),
                  ),
                  child: Text(
                    _isDailyView 
                      ? '${_dailyOrders.length}'
                      : '${_monthlyOrders.length}',
                    style: const TextStyle(
                      color: Colors.white,
                      fontWeight: FontWeight.bold,
                      fontSize: 18,
                    ),
                  ),
                ),
              ],
            ),
          ),

          // Monthly calendar view
          if (!_isDailyView) ...[
            const SizedBox(height: 16),
            Container(
              margin: const EdgeInsets.symmetric(horizontal: 16),
              padding: const EdgeInsets.all(16),
              decoration: BoxDecoration(
                color: Colors.white,
                borderRadius: BorderRadius.circular(12),
                border: Border.all(color: Colors.purple[200]!),
              ),
              child: Column(
                children: [
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      IconButton(
                        onPressed: () {},
                        icon: Icon(Icons.chevron_left, color: Colors.purple[600]),
                      ),
                      Text(
                        'Ağustos 2025',
                        style: TextStyle(
                          fontSize: 18,
                          fontWeight: FontWeight.bold,
                          color: Colors.purple[800],
                        ),
                      ),
                      IconButton(
                        onPressed: () {},
                        icon: Icon(Icons.chevron_right, color: Colors.purple[600]),
                      ),
                    ],
                  ),
                  const SizedBox(height: 16),
                  // Calendar grid would go here
                  Container(
                    height: 200,
                    decoration: BoxDecoration(
                      color: Colors.grey[100],
                      borderRadius: BorderRadius.circular(8),
                    ),
                    child: const Center(
                      child: Text(
                        'Takvim Görünümü',
                        style: TextStyle(color: Colors.grey),
                      ),
                    ),
                  ),
                ],
              ),
            ),
          ],

          // Orders list
          if (_isDailyView) ...[
            const SizedBox(height: 16),
            Expanded(
              child: ListView.builder(
                padding: const EdgeInsets.symmetric(horizontal: 16),
                itemCount: _dailyOrders.length,
                itemBuilder: (context, index) {
                  final order = _dailyOrders[index];
                  return _buildOrderCard(order);
                },
              ),
            ),
          ],
        ],
      ),
    );
  }

  Widget _buildOrderCard(Map<String, dynamic> order) {
    final isCompleted = order['status'] == 'tamamlandi';
    
    return Container(
      margin: const EdgeInsets.only(bottom: 12),
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.purple[50],
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: Colors.purple[200]!),
      ),
      child: Row(
        children: [
          // Table info
          Expanded(
            flex: 2,
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  'Masa ${order['tableNumber']}',
                  style: TextStyle(
                    fontSize: 16,
                    fontWeight: FontWeight.bold,
                    color: Colors.purple[800],
                  ),
                ),
                const SizedBox(height: 4),
                Text(
                  '₺${order['price'].toStringAsFixed(2)}',
                  style: const TextStyle(
                    fontSize: 18,
                    fontWeight: FontWeight.bold,
                    color: Colors.green,
                  ),
                ),
              ],
            ),
          ),

          // Order details
          Expanded(
            flex: 3,
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  children: [
                    Text(
                      'Sipariş ID: ',
                      style: TextStyle(color: Colors.grey[600]),
                    ),
                    Text(
                      '#${order['id']}',
                      style: const TextStyle(fontWeight: FontWeight.bold),
                    ),
                  ],
                ),
                const SizedBox(height: 4),
                Text(
                  order['items'],
                  style: const TextStyle(
                    fontSize: 14,
                    fontWeight: FontWeight.w500,
                  ),
                ),
                const SizedBox(height: 4),
                Text(
                  order['timestamp'],
                  style: TextStyle(
                    fontSize: 12,
                    color: Colors.grey[600],
                  ),
                ),
              ],
            ),
          ),

          // Status
          Column(
            children: [
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
                decoration: BoxDecoration(
                  color: isCompleted ? Colors.green : Colors.orange,
                  borderRadius: BorderRadius.circular(20),
                ),
                child: Row(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    Icon(
                      isCompleted ? Icons.check : Icons.hourglass_empty,
                      color: Colors.white,
                      size: 16,
                    ),
                    const SizedBox(width: 4),
                    Text(
                      isCompleted ? 'Tamamlandı' : 'Devam Ediyor',
                      style: const TextStyle(
                        color: Colors.white,
                        fontSize: 12,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                  ],
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }
}

