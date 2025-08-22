import 'package:flutter/material.dart';

class OrderHistoryScreen extends StatefulWidget {
  const OrderHistoryScreen({super.key});

  @override
  State<OrderHistoryScreen> createState() => _OrderHistoryScreenState();
}

class _OrderHistoryScreenState extends State<OrderHistoryScreen> {
  String _selectedFilter = 'Tümü';
  String _selectedPersonnel = 'Tümü';
  DateTime _startDate = DateTime.now().subtract(const Duration(days: 7));
  DateTime _endDate = DateTime.now();
  
  final List<String> _filterOptions = ['Tümü', 'Tamamlanan', 'İptal Edilen', 'Bekleyen'];
  final List<String> _personnelOptions = ['Tümü', 'Admin', 'Garson', 'Kasiyer'];
  
  // Örnek sipariş verileri
  final List<Map<String, dynamic>> _orders = [
    {
      'id': 'ORD-001',
      'tableNumber': 'Masa 1',
      'customerName': 'Ahmet Yılmaz',
      'orderTime': DateTime.now().subtract(const Duration(hours: 2)),
      'totalAmount': 125.50,
      'status': 'Tamamlandı',
      'personnel': 'Admin',
      'items': ['Karışık Pizza', 'Cola', 'Tiramisu'],
    },
    {
      'id': 'ORD-002',
      'tableNumber': 'Masa 3',
      'customerName': 'Fatma Demir',
      'orderTime': DateTime.now().subtract(const Duration(hours: 4)),
      'totalAmount': 89.75,
      'status': 'Tamamlandı',
      'personnel': 'Garson',
      'items': ['Mantı', 'Ayran', 'Künefe'],
    },
    {
      'id': 'ORD-003',
      'tableNumber': 'Masa 2',
      'customerName': 'Mehmet Kaya',
      'orderTime': DateTime.now().subtract(const Duration(hours: 6)),
      'totalAmount': 156.00,
      'status': 'İptal Edildi',
      'personnel': 'Kasiyer',
      'items': ['Döner', 'Pilav', 'Baklava'],
    },
    {
      'id': 'ORD-004',
      'tableNumber': 'Masa 5',
      'customerName': 'Ayşe Özkan',
      'orderTime': DateTime.now().subtract(const Duration(hours: 8)),
      'totalAmount': 67.25,
      'status': 'Tamamlandı',
      'personnel': 'Garson',
      'items': ['Çorba', 'Salata', 'Su'],
    },
  ];

  List<Map<String, dynamic>> get _filteredOrders {
    return _orders.where((order) {
      bool dateFilter = order['orderTime'].isAfter(_startDate) && 
                       order['orderTime'].isBefore(_endDate.add(const Duration(days: 1)));
      
      bool statusFilter = _selectedFilter == 'Tümü' || order['status'] == _selectedFilter;
      bool personnelFilter = _selectedPersonnel == 'Tümü' || order['personnel'] == _selectedPersonnel;
      
      return dateFilter && statusFilter && personnelFilter;
    }).toList();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Sipariş Geçmişi'),
        backgroundColor: const Color(0xFFFF9800),
        foregroundColor: Colors.white,
      ),
      body: Column(
        children: [
          // Filtreler
          Container(
            padding: const EdgeInsets.all(16.0),
            decoration: BoxDecoration(
              color: Colors.white,
              boxShadow: [
                BoxShadow(
                  color: Colors.grey.withOpacity(0.1),
                  spreadRadius: 1,
                  blurRadius: 3,
                  offset: const Offset(0, 2),
                ),
              ],
            ),
            child: Column(
              children: [
                // Arama çubuğu
                TextField(
                  decoration: InputDecoration(
                    hintText: 'Sipariş, personel veya masa ara',
                    prefixIcon: const Icon(Icons.search),
                    border: OutlineInputBorder(
                      borderRadius: BorderRadius.circular(8),
                    ),
                    filled: true,
                    fillColor: Colors.grey[50],
                  ),
                ),
                const SizedBox(height: 16),
                
                // Filtre butonları
                Row(
                  children: [
                    Expanded(
                      child: DropdownButtonFormField<String>(
                        value: _selectedFilter,
                        decoration: const InputDecoration(
                          labelText: 'Tüm İşlemler',
                          border: OutlineInputBorder(),
                          contentPadding: EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                        ),
                        items: _filterOptions.map((String option) {
                          return DropdownMenuItem<String>(
                            value: option,
                            child: Text(option),
                          );
                        }).toList(),
                        onChanged: (String? newValue) {
                          setState(() {
                            _selectedFilter = newValue!;
                          });
                        },
                      ),
                    ),
                    const SizedBox(width: 12),
                    Expanded(
                      child: DropdownButtonFormField<String>(
                        value: _selectedPersonnel,
                        decoration: const InputDecoration(
                          labelText: 'Tüm Personeller',
                          border: OutlineInputBorder(),
                          contentPadding: EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                        ),
                        items: _personnelOptions.map((String option) {
                          return DropdownMenuItem<String>(
                            value: option,
                            child: Text(option),
                          );
                        }).toList(),
                        onChanged: (String? newValue) {
                          setState(() {
                            _selectedPersonnel = newValue!;
                          });
                        },
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 16),
                
                // Tarih seçimi
                Row(
                  children: [
                    Expanded(
                      child: TextFormField(
                        readOnly: true,
                        decoration: InputDecoration(
                          labelText: 'Başlangıç Tarihi',
                          border: const OutlineInputBorder(),
                          prefixIcon: const Icon(Icons.calendar_today),
                          suffixIcon: IconButton(
                            icon: const Icon(Icons.calendar_month),
                            onPressed: () => _selectDate(context, true),
                          ),
                        ),
                        controller: TextEditingController(
                          text: '${_startDate.day.toString().padLeft(2, '0')}.${_startDate.month.toString().padLeft(2, '0')}.${_startDate.year}',
                        ),
                      ),
                    ),
                    const SizedBox(width: 12),
                    Expanded(
                      child: TextFormField(
                        readOnly: true,
                        decoration: InputDecoration(
                          labelText: 'Bitiş Tarihi',
                          border: const OutlineInputBorder(),
                          prefixIcon: const Icon(Icons.calendar_today),
                          suffixIcon: IconButton(
                            icon: const Icon(Icons.calendar_month),
                            onPressed: () => _selectDate(context, false),
                          ),
                        ),
                        controller: TextEditingController(
                          text: '${_endDate.day.toString().padLeft(2, '0')}.${_endDate.month.toString().padLeft(2, '0')}.${_endDate.year}',
                        ),
                      ),
                    ),
                  ],
                ),
              ],
            ),
          ),
          
          // Sipariş listesi
          Expanded(
            child: ListView.builder(
              padding: const EdgeInsets.all(16.0),
              itemCount: _filteredOrders.length,
              itemBuilder: (context, index) {
                final order = _filteredOrders[index];
                return _buildOrderCard(order);
              },
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildOrderCard(Map<String, dynamic> order) {
    Color statusColor;
    switch (order['status']) {
      case 'Tamamlandı':
        statusColor = Colors.green;
        break;
      case 'İptal Edildi':
        statusColor = Colors.red;
        break;
      case 'Bekleyen':
        statusColor = Colors.orange;
        break;
      default:
        statusColor = Colors.grey;
    }

    return Card(
      margin: const EdgeInsets.only(bottom: 16),
      elevation: 2,
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Üst satır - Sipariş ID ve Durum
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text(
                  'Sipariş #${order['id']}',
                  style: const TextStyle(
                    fontWeight: FontWeight.bold,
                    fontSize: 16,
                  ),
                ),
                Container(
                  padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
                  decoration: BoxDecoration(
                    color: statusColor,
                    borderRadius: BorderRadius.circular(20),
                  ),
                  child: Text(
                    order['status'],
                    style: const TextStyle(
                      color: Colors.white,
                      fontWeight: FontWeight.bold,
                      fontSize: 12,
                    ),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 12),
            
            // Sipariş içeriği
            Text(
              'İçerik: ${order['items'].join(', ')}',
              style: const TextStyle(fontSize: 14),
              maxLines: 2,
              overflow: TextOverflow.ellipsis,
            ),
            const SizedBox(height: 8),
            
            // Alt bilgiler
            Row(
              children: [
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Row(
                        children: [
                          const Icon(Icons.person, size: 16, color: Colors.blue),
                          const SizedBox(width: 4),
                          Text(
                            order['personnel'],
                            style: const TextStyle(fontSize: 12),
                          ),
                        ],
                      ),
                      const SizedBox(height: 4),
                      Row(
                        children: [
                          const Icon(Icons.table_restaurant, size: 16, color: Colors.green),
                          const SizedBox(width: 4),
                          Text(
                            order['tableNumber'],
                            style: const TextStyle(fontSize: 12),
                          ),
                        ],
                      ),
                    ],
                  ),
                ),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.end,
                    children: [
                      Text(
                        '₺${order['totalAmount'].toStringAsFixed(2)}',
                        style: const TextStyle(
                          fontWeight: FontWeight.bold,
                          fontSize: 16,
                          color: Colors.green,
                        ),
                      ),
                      const SizedBox(height: 4),
                      Text(
                        '${order['orderTime'].day.toString().padLeft(2, '0')}.${order['orderTime'].month.toString().padLeft(2, '0')}.${order['orderTime'].year} ${order['orderTime'].hour.toString().padLeft(2, '0')}:${order['orderTime'].minute.toString().padLeft(2, '0')}',
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
          ],
        ),
      ),
    );
  }

  Future<void> _selectDate(BuildContext context, bool isStartDate) async {
    final DateTime? picked = await showDatePicker(
      context: context,
      initialDate: isStartDate ? _startDate : _endDate,
      firstDate: DateTime.now().subtract(const Duration(days: 365)),
      lastDate: DateTime.now(),
    );
    
    if (picked != null) {
      setState(() {
        if (isStartDate) {
          _startDate = picked;
        } else {
          _endDate = picked;
        }
      });
    }
  }
}
