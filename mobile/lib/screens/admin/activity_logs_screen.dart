import 'package:flutter/material.dart';

class ActivityLogsScreen extends StatefulWidget {
  const ActivityLogsScreen({super.key});

  @override
  State<ActivityLogsScreen> createState() => _ActivityLogsScreenState();
}

class _ActivityLogsScreenState extends State<ActivityLogsScreen> {
  String _selectedFilter = 'Tümü';
  String _selectedUser = 'Tüm Kullanıcılar';
  DateTime _startDate = DateTime.now().subtract(const Duration(days: 7));
  DateTime _endDate = DateTime.now();
  
  final List<String> _filterOptions = ['Tümü', 'Giriş', 'Çıkış', 'Rezervasyon', 'Sipariş', 'Sistem'];
  final List<String> _userOptions = ['Tüm Kullanıcılar', 'Admin', 'Garson', 'Kasiyer', 'Müşteri'];
  
  // Örnek aktivite log verileri
  final List<Map<String, dynamic>> _activityLogs = [
    {
      'id': 'LOG-001',
      'timestamp': DateTime.now().subtract(const Duration(minutes: 5)),
      'user': 'Admin',
      'action': 'Giriş',
      'description': 'Sisteme giriş yapıldı',
      'ipAddress': '192.168.1.100',
      'status': 'Başarılı',
      'category': 'Giriş',
    },
    {
      'id': 'LOG-002',
      'timestamp': DateTime.now().subtract(const Duration(minutes: 15)),
      'user': 'Garson',
      'action': 'Rezervasyon',
      'description': 'Masa 3 için yeni rezervasyon oluşturuldu',
      'ipAddress': '192.168.1.101',
      'status': 'Başarılı',
      'category': 'Rezervasyon',
    },
    {
      'id': 'LOG-003',
      'timestamp': DateTime.now().subtract(const Duration(minutes: 30)),
      'user': 'Kasiyer',
      'action': 'Sipariş',
      'description': 'Masa 1\'den yeni sipariş alındı - Toplam: ₺125.50',
      'ipAddress': '192.168.1.102',
      'status': 'Başarılı',
      'category': 'Sipariş',
    },
    {
      'id': 'LOG-004',
      'timestamp': DateTime.now().subtract(const Duration(hours: 1)),
      'user': 'Admin',
      'action': 'Sistem',
      'description': 'Yeni masa eklendi: Masa 8 (4 kişilik)',
      'ipAddress': '192.168.1.100',
      'status': 'Başarılı',
      'category': 'Sistem',
    },
    {
      'id': 'LOG-005',
      'timestamp': DateTime.now().subtract(const Duration(hours: 2)),
      'user': 'Garson',
      'action': 'Giriş',
      'description': 'Sisteme giriş yapıldı',
      'ipAddress': '192.168.1.101',
      'status': 'Başarılı',
      'category': 'Giriş',
    },
    {
      'id': 'LOG-006',
      'timestamp': DateTime.now().subtract(const Duration(hours: 3)),
      'user': 'Müşteri',
      'action': 'Rezervasyon',
      'description': 'Online rezervasyon iptal edildi - Masa 2',
      'ipAddress': '185.123.45.67',
      'status': 'Başarılı',
      'category': 'Rezervasyon',
    },
    {
      'id': 'LOG-007',
      'timestamp': DateTime.now().subtract(const Duration(hours: 4)),
      'user': 'Admin',
      'action': 'Sistem',
      'description': 'Sistem yedeklemesi tamamlandı',
      'ipAddress': '192.168.1.100',
      'status': 'Başarılı',
      'category': 'Sistem',
    },
    {
      'id': 'LOG-008',
      'timestamp': DateTime.now().subtract(const Duration(hours: 5)),
      'user': 'Kasiyer',
      'action': 'Çıkış',
      'description': 'Sistemden çıkış yapıldı',
      'ipAddress': '192.168.1.102',
      'status': 'Başarılı',
      'category': 'Çıkış',
    },
  ];

  List<Map<String, dynamic>> get _filteredLogs {
    return _activityLogs.where((log) {
      bool categoryMatch = _selectedFilter == 'Tümü' || log['category'] == _selectedFilter;
      bool userMatch = _selectedUser == 'Tüm Kullanıcılar' || log['user'] == _selectedUser;
      bool dateMatch = log['timestamp'].isAfter(_startDate) && 
                      log['timestamp'].isBefore(_endDate.add(const Duration(days: 1)));
      
      return categoryMatch && userMatch && dateMatch;
    }).toList();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Aktivite Logları'),
        backgroundColor: const Color(0xFF9C27B0),
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
                    hintText: 'Ara (mesaj, aksiyon, varlık, e-posta)',
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
                          labelText: 'Tüm Aksiyonlar',
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
                        value: _selectedUser,
                        decoration: const InputDecoration(
                          labelText: 'Tüm Personeller',
                          border: OutlineInputBorder(),
                          contentPadding: EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                        ),
                        items: _userOptions.map((String option) {
                          return DropdownMenuItem<String>(
                            value: option,
                            child: Text(option),
                          );
                        }).toList(),
                        onChanged: (String? newValue) {
                          setState(() {
                            _selectedUser = newValue!;
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
                const SizedBox(height: 16),
                
                // Filtreleri sıfırla butonu
                SizedBox(
                  width: double.infinity,
                  child: ElevatedButton(
                    onPressed: _resetFilters,
                    style: ElevatedButton.styleFrom(
                      backgroundColor: const Color(0xFF9C27B0),
                      foregroundColor: Colors.white,
                      padding: const EdgeInsets.symmetric(vertical: 12),
                    ),
                    child: const Text('Tümünü Getir (Filtreleri Sıfırla)'),
                  ),
                ),
              ],
            ),
          ),
          
          // Aktivite logları listesi
          Expanded(
            child: ListView.builder(
              padding: const EdgeInsets.all(16.0),
              itemCount: _filteredLogs.length,
              itemBuilder: (context, index) {
                final log = _filteredLogs[index];
                return _buildLogCard(log);
              },
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildLogCard(Map<String, dynamic> log) {
    Color actionColor;
    switch (log['action']) {
      case 'Giriş':
        actionColor = Colors.green;
        break;
      case 'Çıkış':
        actionColor = Colors.orange;
        break;
      case 'Rezervasyon':
        actionColor = Colors.blue;
        break;
      case 'Sipariş':
        actionColor = Colors.purple;
        break;
      case 'Sistem':
        actionColor = Colors.red;
        break;
      default:
        actionColor = Colors.grey;
    }

    return Card(
      margin: const EdgeInsets.only(bottom: 16),
      elevation: 2,
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Üst satır - Log ID ve Aksiyon
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text(
                  'Log #${log['id']}',
                  style: const TextStyle(
                    fontWeight: FontWeight.bold,
                    fontSize: 14,
                  ),
                ),
                Container(
                  padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
                  decoration: BoxDecoration(
                    color: actionColor,
                    borderRadius: BorderRadius.circular(20),
                  ),
                  child: Text(
                    log['action'],
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
            
            // Açıklama
            Text(
              log['description'],
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
                            log['user'],
                            style: const TextStyle(fontSize: 12),
                          ),
                        ],
                      ),
                      const SizedBox(height: 4),
                      Row(
                        children: [
                          const Icon(Icons.computer, size: 16, color: Colors.green),
                          const SizedBox(width: 4),
                          Text(
                            log['ipAddress'],
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
                        log['status'],
                        style: TextStyle(
                          fontSize: 12,
                          color: log['status'] == 'Başarılı' ? Colors.green : Colors.red,
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                      const SizedBox(height: 4),
                      Text(
                        '${log['timestamp'].day.toString().padLeft(2, '0')}.${log['timestamp'].month.toString().padLeft(2, '0')}.${log['timestamp'].year} ${log['timestamp'].hour.toString().padLeft(2, '0')}:${log['timestamp'].minute.toString().padLeft(2, '0')}',
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

  void _resetFilters() {
    setState(() {
      _selectedFilter = 'Tümü';
      _selectedUser = 'Tüm Kullanıcılar';
      _startDate = DateTime.now().subtract(const Duration(days: 7));
      _endDate = DateTime.now();
    });
  }
}
