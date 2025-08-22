import 'package:flutter/material.dart';
import 'dart:io';
import 'package:image_picker/image_picker.dart';
import 'package:flutter/services.dart';

class PersonnelScreen extends StatefulWidget {
  const PersonnelScreen({super.key});

  @override
  State<PersonnelScreen> createState() => _PersonnelScreenState();
}

class _PersonnelScreenState extends State<PersonnelScreen> {
  final _formKey = GlobalKey<FormState>();
  final _nameController = TextEditingController();
  final _emailController = TextEditingController();
  final _phoneController = TextEditingController();
  
  String _selectedPosition = 'Garson';
  DateTime _hireDate = DateTime.now();
  int _selectedTabIndex = 0;
  bool _isEditing = false;
  String? _editingPersonId;
  File? _selectedImage;
  final ImagePicker _picker = ImagePicker();
  
  // Filtre değişkeni ekle
  String _selectedFilter = 'Tümü';
  
  final List<String> _positionOptions = [
    'Garson',
    'Kasiyer',
    'Şef',
    'Temizlik Görevlisi',
    'Güvenlik',
    'Admin',
  ];
  
  final List<String> _statusOptions = ['Aktif', 'Pasif'];
  
  // Aktif personel listesi
  List<Map<String, dynamic>> _activePersonnel = [
    {
      'id': 'P001',
      'name': 'Admin User',
      'email': 'admin@test.com',
      'phone': '5551234567',
      'position': 'Admin',
      'status': 'Aktif',
      'hireDate': DateTime(2025, 2, 16),
      'avatar': 'AU',
      'photoPath': null,
    },
    {
      'id': 'P002',
      'name': 'Garson User',
      'email': 'garson@test.com',
      'phone': '5559876543',
      'position': 'Garson',
      'status': 'Aktif',
      'hireDate': DateTime(2025, 3, 1),
      'avatar': 'GU',
      'photoPath': null,
    },
  ];
  
  // Geçmiş personel listesi (boş başlangıç)
  List<Map<String, dynamic>> _pastPersonnel = [];

  List<Map<String, dynamic>> get _currentPersonnelList {
    return _selectedTabIndex == 0 ? _activePersonnel : _pastPersonnel;
  }

  // Filtrelenmiş personel listesi
  List<Map<String, dynamic>> get _filteredPersonnelList {
    if (_selectedFilter == 'Tümü') {
      return _currentPersonnelList;
    } else {
      return _currentPersonnelList.where((person) => 
        person['position'] == _selectedFilter
      ).toList();
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Personel Yönetimi'),
        backgroundColor: const Color(0xFF9C27B0),
        foregroundColor: Colors.white,
      ),
      body: SingleChildScrollView(
        child: Column(
          children: [
            // Tab seçimi
            Container(
              padding: const EdgeInsets.all(16.0),
              child: Row(
                children: [
                  Expanded(
                    child: GestureDetector(
                      onTap: () {
                        setState(() {
                          _selectedTabIndex = 0;
                        });
                      },
                      child: Container(
                        padding: const EdgeInsets.symmetric(vertical: 12),
                        decoration: BoxDecoration(
                          color: _selectedTabIndex == 0 ? const Color(0xFF4CAF50) : Colors.white,
                          borderRadius: BorderRadius.circular(8),
                          border: Border.all(
                            color: _selectedTabIndex == 0 ? const Color(0xFF4CAF50) : Colors.grey[300]!,
                          ),
                        ),
                        child: Text(
                          'Aktif Personel',
                          textAlign: TextAlign.center,
                          style: TextStyle(
                            color: _selectedTabIndex == 0 ? Colors.white : Colors.grey[600],
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                      ),
                    ),
                  ),
                  const SizedBox(width: 16),
                  Expanded(
                    child: GestureDetector(
                      onTap: () {
                        setState(() {
                          _selectedTabIndex = 1;
                        });
                      },
                      child: Container(
                        padding: const EdgeInsets.symmetric(vertical: 12),
                        decoration: BoxDecoration(
                          color: _selectedTabIndex == 1 ? const Color(0xFF4CAF50) : Colors.white,
                          borderRadius: BorderRadius.circular(8),
                          border: Border.all(
                            color: _selectedTabIndex == 1 ? const Color(0xFF4CAF50) : Colors.grey[300]!,
                          ),
                        ),
                        child: Text(
                          'Geçmiş Personel',
                          textAlign: TextAlign.center,
                          style: TextStyle(
                            color: _selectedTabIndex == 1 ? Colors.white : Colors.grey[600],
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                      ),
                    ),
                  ),
                ],
              ),
            ),
            
            // Filtre butonları
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 16.0),
              child: Row(
                children: [
                  _buildFilterButton('Tümü', _selectedFilter == 'Tümü'),
                  const SizedBox(width: 12),
                  _buildFilterButton('Garson', _selectedFilter == 'Garson'),
                  const SizedBox(width: 12),
                  _buildFilterButton('Kasiyer', _selectedFilter == 'Kasiyer'),
                ],
              ),
            ),
            
            const SizedBox(height: 16),
            
            // Personel ekleme formu
            Container(
              margin: const EdgeInsets.symmetric(horizontal: 16.0),
              padding: const EdgeInsets.all(20.0),
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
              child: Form(
                key: _formKey,
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      children: [
                        Icon(
                          Icons.person_add,
                          color: const Color(0xFF9C27B0),
                          size: 28,
                        ),
                        const SizedBox(width: 12),
                        const Expanded(
                          child: Text(
                            'Yeni Personel Ekle',
                            style: TextStyle(
                              fontSize: 20,
                              fontWeight: FontWeight.bold,
                            ),
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 20),
                    
                    // Fotoğraf seçimi
                    Row(
                      children: [
                        GestureDetector(
                          onTap: _pickImage,
                          child: Container(
                            width: 80,
                            height: 80,
                            decoration: BoxDecoration(
                              color: Colors.grey[200],
                              borderRadius: BorderRadius.circular(40),
                              border: Border.all(color: Colors.grey[300]!),
                            ),
                            child: _selectedImage != null
                                ? ClipRRect(
                                    borderRadius: BorderRadius.circular(40),
                                    child: Image.file(
                                      _selectedImage!,
                                      fit: BoxFit.cover,
                                    ),
                                  )
                                : Icon(
                                    Icons.camera_alt,
                                    color: Colors.grey[600],
                                    size: 32,
                                  ),
                          ),
                        ),
                        const SizedBox(width: 16),
                        Expanded(
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              const Text(
                                'Fotoğraf Ekle',
                                style: TextStyle(
                                  fontSize: 16,
                                  fontWeight: FontWeight.w500,
                                ),
                              ),
                              const SizedBox(height: 4),
                              Text(
                                'Personel fotoğrafı eklemek için tıklayın',
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
                    const SizedBox(height: 20),
                    
                    // Ad Soyad ve E-posta
                    Row(
                      children: [
                        Expanded(
                          child: TextFormField(
                            controller: _nameController,
                            decoration: const InputDecoration(
                              labelText: 'Ad Soyad',
                              border: OutlineInputBorder(),
                              prefixIcon: Icon(Icons.person),
                            ),
                          ),
                        ),
                        const SizedBox(width: 16),
                        Expanded(
                          child: TextFormField(
                            controller: _emailController,
                            decoration: const InputDecoration(
                              labelText: 'E-posta',
                              border: OutlineInputBorder(),
                              prefixIcon: Icon(Icons.email),
                            ),
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 16),
                    
                    // Telefon ve Pozisyon
                    Row(
                      children: [
                        Expanded(
                          child: TextFormField(
                            controller: _phoneController,
                            decoration: const InputDecoration(
                              labelText: 'Telefon (5XX XXX XX XX)',
                              border: OutlineInputBorder(),
                              prefixIcon: Icon(Icons.phone),
                            ),
                            keyboardType: TextInputType.phone,
                            inputFormatters: [
                              FilteringTextInputFormatter.digitsOnly,
                            ],
                          ),
                        ),
                        const SizedBox(width: 16),
                        Expanded(
                          child: DropdownButtonFormField<String>(
                            value: _selectedPosition,
                            decoration: const InputDecoration(
                              labelText: 'Pozisyon',
                              border: OutlineInputBorder(),
                              prefixIcon: Icon(Icons.work),
                            ),
                            items: _positionOptions.map((String position) {
                              return DropdownMenuItem<String>(
                                value: position,
                                child: Text(position),
                              );
                            }).toList(),
                            onChanged: (String? newValue) {
                              setState(() {
                                _selectedPosition = newValue!;
                              });
                            },
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 16),
                    
                    // İşe başlama tarihi
                    Row(
                      children: [
                        Icon(Icons.calendar_today, color: Colors.grey[600]),
                        const SizedBox(width: 8),
                        Text(
                          'İşe Başlama: ${_hireDate.day}/${_hireDate.month}/${_hireDate.year}',
                          style: TextStyle(
                            fontSize: 16,
                            color: Colors.grey[600],
                          ),
                        ),
                        const SizedBox(width: 16),
                        TextButton(
                          onPressed: _selectHireDate,
                          child: const Text('Tarih Seç'),
                        ),
                      ],
                    ),
                    const SizedBox(height: 20),
                    
                    // Butonlar
                    Row(
                      children: [
                        Expanded(
                          child: ElevatedButton(
                            onPressed: _isEditing ? _updatePersonnel : _addPersonnel,
                            style: ElevatedButton.styleFrom(
                              backgroundColor: const Color(0xFF9C27B0),
                              foregroundColor: Colors.white,
                              padding: const EdgeInsets.symmetric(vertical: 16),
                            ),
                            child: Text(
                              _isEditing ? 'GÜNCELLE' : 'PERSONEL EKLE',
                              style: const TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
                            ),
                          ),
                        ),
                        if (_isEditing) ...[
                          const SizedBox(width: 16),
                          Expanded(
                            child: ElevatedButton(
                              onPressed: _cancelEdit,
                              style: ElevatedButton.styleFrom(
                                backgroundColor: Colors.grey,
                                foregroundColor: Colors.white,
                                padding: const EdgeInsets.symmetric(vertical: 16),
                              ),
                              child: const Text(
                                'İPTAL',
                                style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
                              ),
                            ),
                          ),
                        ],
                      ],
                    ),
                  ],
                ),
              ),
            ),
            
            const SizedBox(height: 20),
            
            // Personel listesi
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 16.0),
              child: Column(
                children: _filteredPersonnelList.map((personnel) => 
                  _buildPersonnelCard(personnel)
                ).toList(),
              ),
            ),
            
            const SizedBox(height: 20),
          ],
        ),
      ),
    );
  }

  Widget _buildFilterButton(String text, bool isSelected) {
    return ElevatedButton(
      onPressed: () {
        setState(() {
          _selectedFilter = text;
        });
      },
      style: ElevatedButton.styleFrom(
        backgroundColor: isSelected ? const Color(0xFF9C27B0) : Colors.white,
        foregroundColor: isSelected ? Colors.white : Colors.grey[600],
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
      ),
      child: Text(text),
    );
  }

  Widget _buildPersonnelCard(Map<String, dynamic> person) {
    return Container(
      margin: const EdgeInsets.only(bottom: 16),
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
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Avatar/Fotoğraf
          Container(
            width: 60,
            height: 60,
            decoration: BoxDecoration(
              color: const Color(0xFF5E35B1),
              borderRadius: BorderRadius.circular(30),
            ),
            child: person['photoPath'] != null
                ? ClipRRect(
                    borderRadius: BorderRadius.circular(30),
                    child: Image.file(
                      File(person['photoPath']),
                      fit: BoxFit.cover,
                    ),
                  )
                : Center(
                    child: Text(
                      person['avatar'],
                      style: const TextStyle(
                        color: Colors.white,
                        fontSize: 20,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                  ),
          ),
          const SizedBox(width: 20),
          
          // Personel bilgileri
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  children: [
                    Expanded(
                      child: Text(
                        person['name'],
                        style: const TextStyle(
                          fontWeight: FontWeight.bold,
                          fontSize: 18,
                        ),
                        overflow: TextOverflow.ellipsis,
                      ),
                    ),
                    const SizedBox(width: 12),
                    Container(
                      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
                      decoration: BoxDecoration(
                        color: person['status'] == 'Aktif' ? const Color(0xFF4CAF50) : Colors.grey,
                        borderRadius: BorderRadius.circular(20),
                      ),
                      child: Text(
                        person['status'],
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
                Row(
                  children: [
                    Icon(Icons.work, color: Colors.blue, size: 16),
                    const SizedBox(width: 8),
                    Expanded(
                      child: Text(
                        person['position'],
                        overflow: TextOverflow.ellipsis,
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 8),
                Row(
                  children: [
                    Icon(Icons.email, color: Colors.orange, size: 16),
                    const SizedBox(width: 8),
                    Expanded(
                      child: Text(
                        person['email'],
                        overflow: TextOverflow.ellipsis,
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 8),
                Row(
                  children: [
                    Icon(Icons.phone, color: Colors.purple, size: 16),
                    const SizedBox(width: 8),
                    Expanded(
                      child: Text(
                        _formatPhoneNumber(person['phone']),
                        overflow: TextOverflow.ellipsis,
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 8),
                Text(
                  'İşe Başlama: ${person['hireDate'].day}/${person['hireDate'].month}/${person['hireDate'].year}',
                  style: TextStyle(
                    fontSize: 12,
                    color: Colors.grey[600],
                  ),
                ),
              ],
            ),
          ),
          
          // Aksiyon butonları
          Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              IconButton(
                onPressed: () => _editPersonnel(person),
                icon: const Icon(Icons.edit, color: Colors.blue),
                tooltip: 'Düzenle',
              ),
              const SizedBox(height: 8),
              ElevatedButton(
                onPressed: () => _togglePersonnelStatus(person),
                style: ElevatedButton.styleFrom(
                  backgroundColor: person['status'] == 'Aktif' ? Colors.orange : const Color(0xFF4CAF50),
                  foregroundColor: Colors.white,
                  padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                ),
                child: Text(
                  person['status'] == 'Aktif' ? 'Pasif Yap' : 'Aktif Yap',
                  style: const TextStyle(fontSize: 12),
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }

  String _formatPhoneNumber(String phone) {
    if (phone.length == 10) {
      return '${phone.substring(0, 3)} ${phone.substring(3, 6)} ${phone.substring(6, 8)} ${phone.substring(8, 10)}';
    }
    return phone;
  }

  Future<void> _pickImage() async {
    final XFile? image = await _picker.pickImage(source: ImageSource.gallery);
    if (image != null) {
      setState(() {
        _selectedImage = File(image.path);
      });
    }
  }

  Future<void> _selectHireDate() async {
    final DateTime? picked = await showDatePicker(
      context: context,
      initialDate: _hireDate,
      firstDate: DateTime.now().subtract(const Duration(days: 3650)),
      lastDate: DateTime.now(),
    );
    
    if (picked != null) {
      setState(() {
        _hireDate = picked;
      });
    }
  }

  void _addPersonnel() {
    if (_nameController.text.isNotEmpty && 
        _emailController.text.isNotEmpty && 
        _phoneController.text.isNotEmpty) {
      
      // Telefon numarası formatını kontrol et
      if (_phoneController.text.length != 10 || !_phoneController.text.startsWith('5')) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('Telefon numarası 5 ile başlamalı ve 10 haneli olmalıdır!'),
            backgroundColor: Colors.red,
          ),
        );
        return;
      }

      final newPerson = {
        'id': 'P${(_activePersonnel.length + 1).toString().padLeft(3, '0')}',
        'name': _nameController.text,
        'email': _emailController.text,
        'phone': _phoneController.text,
        'position': _selectedPosition,
        'status': 'Aktif',
        'hireDate': _hireDate,
        'avatar': _nameController.text.split(' ').map((e) => e[0]).join('').toUpperCase(),
        'photoPath': _selectedImage?.path,
      };
      
      setState(() {
        _activePersonnel.add(newPerson);
      });
      
      _clearForm();
      
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Personel başarıyla eklendi!'),
          backgroundColor: Color(0xFF4CAF50),
        ),
      );
    }
  }

  void _editPersonnel(Map<String, dynamic> person) {
    setState(() {
      _isEditing = true;
      _editingPersonId = person['id'];
      _nameController.text = person['name'];
      _emailController.text = person['email'];
      _phoneController.text = person['phone'];
      _selectedPosition = person['position'];
      _hireDate = person['hireDate'];
      if (person['photoPath'] != null) {
        _selectedImage = File(person['photoPath']);
      }
    });
  }

  void _updatePersonnel() {
    if (_editingPersonId != null && _formKey.currentState!.validate()) {
      final personIndex = _activePersonnel.indexWhere((p) => p['id'] == _editingPersonId);
      if (personIndex != -1) {
        setState(() {
          _activePersonnel[personIndex]['name'] = _nameController.text;
          _activePersonnel[personIndex]['email'] = _emailController.text;
          _activePersonnel[personIndex]['phone'] = _phoneController.text;
          _activePersonnel[personIndex]['position'] = _selectedPosition;
          _activePersonnel[personIndex]['hireDate'] = _hireDate;
          _activePersonnel[personIndex]['avatar'] = _nameController.text.split(' ').map((e) => e[0]).join('').toUpperCase();
          _activePersonnel[personIndex]['photoPath'] = _selectedImage?.path;
        });
        
        _clearForm();
        
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('Personel başarıyla güncellendi!'),
            backgroundColor: Color(0xFF4CAF50),
          ),
        );
      }
    }
  }

  void _cancelEdit() {
    setState(() {
      _isEditing = false;
      _editingPersonId = null;
    });
    _clearForm();
  }

  void _clearForm() {
    _nameController.clear();
    _emailController.clear();
    _phoneController.clear();
    _selectedPosition = 'Garson';
    _hireDate = DateTime.now();
    _selectedImage = null;
  }

  void _togglePersonnelStatus(Map<String, dynamic> person) {
    setState(() {
      if (person['status'] == 'Aktif') {
        // Aktif personeli pasif yap ve geçmiş personel listesine taşı
        person['status'] = 'Pasif';
        _activePersonnel.remove(person);
        _pastPersonnel.add(person);
      } else {
        // Pasif personeli aktif yap ve aktif personel listesine taşı
        person['status'] = 'Aktif';
        _pastPersonnel.remove(person);
        _activePersonnel.add(person);
      }
    });
    
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text('${person['name']} ${person['status'] == 'Aktif' ? 'aktif' : 'pasif'} yapıldı'),
        backgroundColor: person['status'] == 'Aktif' ? const Color(0xFF4CAF50) : Colors.orange,
      ),
    );
  }

  @override
  void dispose() {
    _nameController.dispose();
    _emailController.dispose();
    _phoneController.dispose();
    super.dispose();
  }
}
