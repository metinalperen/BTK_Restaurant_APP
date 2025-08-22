import 'package:flutter/material.dart';

class WaiterReservationsScreen extends StatefulWidget {
  const WaiterReservationsScreen({Key? key}) : super(key: key);

  @override
  State<WaiterReservationsScreen> createState() => _WaiterReservationsScreenState();
}

class _WaiterReservationsScreenState extends State<WaiterReservationsScreen> {
  List<Map<String, dynamic>> _reservations = [];
  String _selectedFilter = 'Tümü';
  DateTime _selectedDate = DateTime.now();

  @override
  void initState() {
    super.initState();
    _loadMockData();
  }

  void _loadMockData() {
    // Mock rezervasyon verileri
    _reservations = [
      {
        'id': 1,
        'customerName': 'Ahmet Yılmaz',
        'phone': '0532 123 45 67',
        'tableId': '3',
        'tableName': 'Masa 3',
        'date': '2025-01-20',
        'time': '19:00',
        'guestCount': 4,
        'status': 'confirmed',
        'notes': 'Pencere kenarı tercih edildi'
      },
      {
        'id': 2,
        'customerName': 'Fatma Demir',
        'phone': '0533 987 65 43',
        'tableId': '6',
        'tableName': 'Masa 6',
        'date': '2025-01-20',
        'time': '20:30',
        'guestCount': 6,
        'status': 'confirmed',
        'notes': 'Doğum günü kutlaması'
      },
      {
        'id': 3,
        'customerName': 'Mehmet Kaya',
        'phone': '0534 555 44 33',
        'tableId': '8',
        'tableName': 'Masa 8',
        'date': '2025-01-21',
        'time': '18:00',
        'guestCount': 2,
        'status': 'pending',
        'notes': 'Romantik akşam yemeği'
      },
      {
        'id': 4,
        'customerName': 'Ayşe Özkan',
        'phone': '0535 111 22 33',
        'tableId': '1',
        'tableName': 'Masa 1',
        'date': '2025-01-21',
        'time': '19:30',
        'guestCount': 8,
        'status': 'confirmed',
        'notes': 'İş yemeği - özel menü'
      },
      {
        'id': 5,
        'customerName': 'Ali Veli',
        'phone': '0536 444 55 66',
        'tableId': '4',
        'tableName': 'Masa 4',
        'date': '2025-01-22',
        'time': '20:00',
        'guestCount': 3,
        'status': 'confirmed',
        'notes': ''
      },
    ];
  }

  List<Map<String, dynamic>> get _filteredReservations {
    return _reservations.where((reservation) {
      final matchesFilter = _selectedFilter == 'Tümü' || 
                           (_selectedFilter == 'Bugün' && reservation['date'] == _formatDate(DateTime.now())) ||
                           (_selectedFilter == 'Yarın' && reservation['date'] == _formatDate(DateTime.now().add(Duration(days: 1)))) ||
                           (_selectedFilter == 'Bu Hafta' && _isThisWeek(DateTime.parse(reservation['date'])));
      return matchesFilter;
    }).toList();
  }

  String _formatDate(DateTime date) {
    return '${date.year.toString().padLeft(4, '0')}-${date.month.toString().padLeft(2, '0')}-${date.day.toString().padLeft(2, '0')}';
  }

  bool _isThisWeek(DateTime date) {
    final now = DateTime.now();
    final startOfWeek = now.subtract(Duration(days: now.weekday - 1));
    final endOfWeek = startOfWeek.add(Duration(days: 6));
    return date.isAfter(startOfWeek.subtract(Duration(days: 1))) && 
           date.isBefore(endOfWeek.add(Duration(days: 1)));
  }

  Color _getStatusColor(String status) {
    switch (status) {
      case 'confirmed':
        return Colors.green;
      case 'pending':
        return Colors.orange;
      case 'cancelled':
        return Colors.red;
      default:
        return Colors.grey;
    }
  }

  String _getStatusText(String status) {
    switch (status) {
      case 'confirmed':
        return 'Onaylandı';
      case 'pending':
        return 'Beklemede';
      case 'cancelled':
        return 'İptal Edildi';
      default:
        return 'Bilinmiyor';
    }
  }

  void _showReservationDetails(Map<String, dynamic> reservation) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: Text('Rezervasyon Detayları'),
        content: Container(
          width: double.maxFinite,
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              _buildDetailRow('Müşteri Adı', reservation['customerName']),
              _buildDetailRow('Telefon', reservation['phone']),
              _buildDetailRow('Masa', reservation['tableName']),
              _buildDetailRow('Tarih', reservation['date']),
              _buildDetailRow('Saat', reservation['time']),
              _buildDetailRow('Kişi Sayısı', '${reservation['guestCount']}'),
              _buildDetailRow('Durum', _getStatusText(reservation['status'])),
              if (reservation['notes'].isNotEmpty) ...[
                SizedBox(height: 16),
                _buildDetailRow('Notlar', reservation['notes']),
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

  Widget _buildDetailRow(String label, String value) {
    return Padding(
      padding: EdgeInsets.symmetric(vertical: 4),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          SizedBox(
            width: 100,
            child: Text(
              '$label:',
              style: TextStyle(fontWeight: FontWeight.bold),
            ),
          ),
          Expanded(
            child: Text(value),
          ),
        ],
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final todayCount = _reservations.where((r) => r['date'] == _formatDate(DateTime.now())).length;
    final tomorrowCount = _reservations.where((r) => r['date'] == _formatDate(DateTime.now().add(Duration(days: 1)))).length;
    final thisWeekCount = _reservations.where((r) => _isThisWeek(DateTime.parse(r['date']))).length;

    return Scaffold(
      backgroundColor: Colors.grey[50],
      appBar: AppBar(
        title: Text('Rezervasyonlar'),
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
          // Filtreler
          Container(
            padding: EdgeInsets.all(16),
            color: Colors.white,
            child: Column(
              children: [
                // Tarih filtresi
                Row(
                  children: [
                    Text('Filtre: ', style: TextStyle(fontWeight: FontWeight.bold)),
                    SizedBox(width: 8),
                    Expanded(
                      child: DropdownButtonFormField<String>(
                        value: _selectedFilter,
                        decoration: InputDecoration(
                          border: OutlineInputBorder(borderRadius: BorderRadius.circular(8)),
                          contentPadding: EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                        ),
                        items: [
                          DropdownMenuItem(value: 'Tümü', child: Text('Tümü')),
                          DropdownMenuItem(value: 'Bugün', child: Text('Bugün')),
                          DropdownMenuItem(value: 'Yarın', child: Text('Yarın')),
                          DropdownMenuItem(value: 'Bu Hafta', child: Text('Bu Hafta')),
                        ],
                        onChanged: (value) => setState(() => _selectedFilter = value!),
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
                        'Bugün',
                        '$todayCount',
                        Icons.today,
                        Colors.blue,
                      ),
                    ),
                    SizedBox(width: 12),
                    Expanded(
                      child: _buildSummaryCard(
                        'Yarın',
                        '$tomorrowCount',
                        Icons.event,
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
                        'Bu Hafta',
                        '$thisWeekCount',
                        Icons.calendar_view_week,
                        Colors.orange,
                      ),
                    ),
                    SizedBox(width: 12),
                    Expanded(
                      child: _buildSummaryCard(
                        'Toplam',
                        '${_filteredReservations.length}',
                        Icons.list,
                        Colors.purple,
                      ),
                    ),
                  ],
                ),
              ],
            ),
          ),

          SizedBox(height: 16),

          // Rezervasyon listesi
          Expanded(
            child: _filteredReservations.isEmpty
                ? Center(
                    child: Column(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        Icon(Icons.event_busy, size: 64, color: Colors.grey),
                        SizedBox(height: 16),
                        Text(
                          'Rezervasyon bulunamadı',
                          style: TextStyle(fontSize: 18, color: Colors.grey),
                        ),
                      ],
                    ),
                  )
                : ListView.builder(
                    padding: EdgeInsets.symmetric(horizontal: 16),
                    itemCount: _filteredReservations.length,
                    itemBuilder: (context, index) {
                      final reservation = _filteredReservations[index];
                      return Card(
                        margin: EdgeInsets.only(bottom: 12),
                        child: ListTile(
                          leading: Container(
                            width: 50,
                            height: 50,
                            decoration: BoxDecoration(
                              color: _getStatusColor(reservation['status']),
                              borderRadius: BorderRadius.circular(8),
                            ),
                            child: Icon(
                              Icons.event,
                              color: Colors.white,
                              size: 24,
                            ),
                          ),
                          title: Text(
                            reservation['customerName'],
                            style: TextStyle(fontWeight: FontWeight.bold),
                          ),
                          subtitle: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Text('${reservation['tableName']} - ${reservation['date']} ${reservation['time']}'),
                              Text('${reservation['guestCount']} Kişi'),
                              if (reservation['notes'].isNotEmpty) ...[
                                Text(
                                  reservation['notes'],
                                  style: TextStyle(fontStyle: FontStyle.italic, color: Colors.grey[600]),
                                ),
                              ],
                            ],
                          ),
                          trailing: Column(
                            mainAxisAlignment: MainAxisAlignment.center,
                            children: [
                              Container(
                                padding: EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                                decoration: BoxDecoration(
                                  color: _getStatusColor(reservation['status']),
                                  borderRadius: BorderRadius.circular(12),
                                ),
                                child: Text(
                                  _getStatusText(reservation['status']),
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
                          onTap: () => _showReservationDetails(reservation),
                        ),
                      );
                    },
                  ),
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
