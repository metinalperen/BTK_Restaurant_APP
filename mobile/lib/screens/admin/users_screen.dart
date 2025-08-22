import 'package:flutter/material.dart';
import '../../models/user.dart';
import '../../services/user_service.dart';
import 'dart:convert'; // Added for base64Decode
import 'dart:io'; // Added for File
import 'package:image_picker/image_picker.dart'; // Added for image picker
import 'add_user_screen.dart'; // Added for AddUserScreen

class UsersScreen extends StatefulWidget {
  const UsersScreen({super.key});

  @override
  State<UsersScreen> createState() => _UsersScreenState();
}

class _UsersScreenState extends State<UsersScreen> {
  List<User> users = [];
  bool isLoading = true;
  String? errorMessage;
  String searchQuery = '';
  int? selectedRoleFilter;
  bool showActiveUsers = true; // Aktif/Geçmiş personel seçimi

  @override
  void initState() {
    super.initState();
    _loadUsers();
  }

  Future<void> _loadUsers() async {
    try {
      setState(() {
        isLoading = true;
        errorMessage = null;
      });

      final loadedUsers = showActiveUsers 
          ? await UserService.getActiveUsers()
          : await UserService.getInactiveUsers();
          
      setState(() {
        users = loadedUsers;
        isLoading = false;
      });
    } catch (e) {
      setState(() {
        errorMessage = 'Kullanıcılar yüklenirken hata: $e';
        isLoading = false;
      });
    }
  }

  List<User> get filteredUsers {
    return users.where((user) {
      final matchesSearch = user.name.toLowerCase().contains(searchQuery.toLowerCase()) ||
                          user.email.toLowerCase().contains(searchQuery.toLowerCase());
      
      final matchesRole = selectedRoleFilter == null || user.roles.contains(selectedRoleFilter);
      
      return matchesSearch && matchesRole;
    }).toList();
  }

  Future<void> _refreshUsers() async {
    await _loadUsers();
  }

  void _toggleUserType() {
    setState(() {
      showActiveUsers = !showActiveUsers;
      selectedRoleFilter = null; // Role filtresini sıfırla
    });
    _loadUsers();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFFF8F5FF),
      appBar: AppBar(
        title: const Text('👥 Personel Yönetimi'),
        backgroundColor: const Color(0xFF6B46C1),
        foregroundColor: Colors.white,
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh),
            onPressed: _refreshUsers,
          ),
        ],
      ),
      body: Column(
        children: [
          // Aktif/Geçmiş personel seçimi
          Container(
            padding: const EdgeInsets.all(16),
            child: Row(
              children: [
                Expanded(
                  child: ElevatedButton(
                    onPressed: showActiveUsers ? null : _toggleUserType,
                    style: ElevatedButton.styleFrom(
                      backgroundColor: showActiveUsers ? Colors.green : Colors.grey[300],
                      foregroundColor: showActiveUsers ? Colors.white : Colors.grey[600],
                      padding: const EdgeInsets.symmetric(vertical: 12),
                    ),
                    child: const Text('Aktif Personel'),
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: ElevatedButton(
                    onPressed: !showActiveUsers ? null : _toggleUserType,
                    style: ElevatedButton.styleFrom(
                      backgroundColor: !showActiveUsers ? Colors.green : Colors.grey[300],
                      foregroundColor: !showActiveUsers ? Colors.white : Colors.grey[600],
                      padding: const EdgeInsets.symmetric(vertical: 12),
                    ),
                    child: const Text('Geçmiş Personel'),
                  ),
                ),
              ],
            ),
          ),

          // Role filtreleme butonları
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 16),
            child: Row(
              children: [
                _buildRoleFilterButton(null, 'Tümü'),
                const SizedBox(width: 8),
                _buildRoleFilterButton(User.ROLE_WAITER, 'Garson'),
                const SizedBox(width: 8),
                _buildRoleFilterButton(User.ROLE_CASHIER, 'Kasiyer'),
              ],
            ),
          ),

          const SizedBox(height: 16),

          // Arama kutusu
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 16),
            child: TextField(
              decoration: InputDecoration(
                hintText: '🔍 Personel ara...',
                prefixIcon: const Icon(Icons.search),
                border: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(12),
                ),
                filled: true,
                fillColor: Colors.white,
              ),
              onChanged: (value) {
                setState(() {
                  searchQuery = value;
                });
              },
            ),
          ),

          const SizedBox(height: 16),

          // Kullanıcı listesi
          Expanded(
            child: isLoading
                ? const Center(child: CircularProgressIndicator())
                : errorMessage != null
                    ? Center(
                        child: Column(
                          mainAxisAlignment: MainAxisAlignment.center,
                          children: [
                            Icon(
                              Icons.error_outline,
                              size: 64,
                              color: Colors.red[400],
                            ),
                            const SizedBox(height: 16),
                            Text(
                              errorMessage!,
                              style: TextStyle(
                                fontSize: 16,
                                color: Colors.red[600],
                              ),
                              textAlign: TextAlign.center,
                            ),
                            const SizedBox(height: 16),
                            ElevatedButton(
                              onPressed: _loadUsers,
                              child: const Text('Tekrar Dene'),
                            ),
                          ],
                        ),
                      )
                    : filteredUsers.isEmpty
                        ? Center(
                            child: Column(
                              mainAxisAlignment: MainAxisAlignment.center,
                              children: [
                                Icon(
                                  Icons.people_outline,
                                  size: 64,
                                  color: Colors.grey,
                                ),
                                const SizedBox(height: 16),
                                Text(
                                  'Personel bulunamadı',
                                  style: TextStyle(
                                    fontSize: 18,
                                    color: Colors.grey,
                                  ),
                                ),
                              ],
                            ),
                          )
                        : ListView.builder(
                            padding: const EdgeInsets.all(16),
                            itemCount: filteredUsers.length,
                            itemBuilder: (context, index) {
                              final user = filteredUsers[index];
                              return _buildUserCard(user);
                            },
                          ),
          ),
        ],
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: () async {
          // Yeni personel ekleme ekranına git
          final result = await Navigator.push(
            context,
            MaterialPageRoute(
              builder: (context) => const AddUserScreen(),
            ),
          );
          
          // Eğer yeni personel eklendiyse listeyi yenile
          if (result != null && result is User) {
            await _loadUsers();
            if (mounted) {
              ScaffoldMessenger.of(context).showSnackBar(
                SnackBar(
                  content: Text('${result.name} başarıyla eklendi!'),
                  backgroundColor: Colors.green,
                ),
              );
            }
          }
        },
        backgroundColor: const Color(0xFF9F7AEA),
        child: const Icon(Icons.add, color: Colors.white),
      ),
    );
  }

  Widget _buildRoleFilterButton(int? roleId, String label) {
    final isSelected = selectedRoleFilter == roleId;
    return Expanded(
      child: ElevatedButton(
        onPressed: () {
          setState(() {
            selectedRoleFilter = selectedRoleFilter == roleId ? null : roleId;
          });
        },
        style: ElevatedButton.styleFrom(
          backgroundColor: isSelected ? const Color(0xFF6B46C1) : Colors.white,
          foregroundColor: isSelected ? Colors.white : Colors.grey[600],
          padding: const EdgeInsets.symmetric(vertical: 8),
        ),
        child: Text(label),
      ),
    );
  }

  Widget _buildUserCard(User user) {
    return Card(
      margin: const EdgeInsets.only(bottom: 12),
      child: ListTile(
        leading: CircleAvatar(
          backgroundColor: Color(int.parse(user.roleColor.replaceAll('#', '0xFF'))),
          child: user.photoBase64 != null && user.photoBase64!.isNotEmpty
              ? ClipOval(
                  child: Image.memory(
                    base64Decode(user.photoBase64!),
                    width: 40,
                    height: 40,
                    fit: BoxFit.cover,
                  ),
                )
              : Text(
                  user.name.isNotEmpty ? user.name[0].toUpperCase() : '?',
                  style: const TextStyle(
                    color: Colors.white,
                    fontWeight: FontWeight.bold,
                  ),
                ),
        ),
        title: Text(
          user.name,
          style: const TextStyle(fontWeight: FontWeight.w600),
        ),
        subtitle: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(user.email),
            if (user.phoneNumber != null) Text('📞 ${user.phoneNumber}'),
            Row(
              children: [
                Container(
                  padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                  decoration: BoxDecoration(
                    color: Color(int.parse(user.roleColor.replaceAll('#', '0xFF'))).withOpacity(0.2),
                    borderRadius: BorderRadius.circular(12),
                  ),
                  child: Text(
                    user.roleDisplayName,
                    style: TextStyle(
                      color: Color(int.parse(user.roleColor.replaceAll('#', '0xFF'))),
                      fontSize: 12,
                      fontWeight: FontWeight.w600,
                    ),
                  ),
                ),
                const SizedBox(width: 8),
                Container(
                  padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                  decoration: BoxDecoration(
                    color: user.isActive ? Colors.green.withOpacity(0.2) : Colors.red.withOpacity(0.2),
                    borderRadius: BorderRadius.circular(12),
                  ),
                  child: Text(
                    user.isActive ? 'Aktif' : 'Pasif',
                    style: TextStyle(
                      color: user.isActive ? Colors.green : Colors.red,
                      fontSize: 12,
                      fontWeight: FontWeight.w600,
                    ),
                  ),
                ),
              ],
            ),
          ],
        ),
        trailing: PopupMenuButton<String>(
          onSelected: (value) {
            switch (value) {
              case 'edit':
                // TODO: Düzenleme ekranına git
                break;
              case 'photo':
                _addUserPhoto(user);
                break;
              case 'toggle_active':
                _toggleUserActiveStatus(user);
                break;
              case 'delete':
                _showDeleteConfirmation(user);
                break;
            }
          },
          itemBuilder: (context) => [
            const PopupMenuItem(
              value: 'edit',
              child: Row(
                children: [
                  Icon(Icons.edit, color: Colors.blue),
                  SizedBox(width: 8),
                  Text('Düzenle'),
                ],
              ),
            ),
            PopupMenuItem(
              value: 'photo',
              child: Row(
                children: [
                  Icon(Icons.photo_camera, color: Colors.green),
                  SizedBox(width: 8),
                  Text('Fotoğraf Ekle'),
                ],
              ),
            ),
            PopupMenuItem(
              value: 'toggle_active',
              child: Row(
                children: [
                  Icon(
                    user.isActive ? Icons.block : Icons.check_circle,
                    color: user.isActive ? Colors.orange : Colors.green,
                  ),
                  const SizedBox(width: 8),
                  Text(user.isActive ? 'Pasif Yap' : 'Aktif Yap'),
                ],
              ),
            ),
            const PopupMenuItem(
              value: 'delete',
              child: Row(
                children: [
                  Icon(Icons.delete, color: Colors.red),
                  SizedBox(width: 8),
                  Text('Sil'),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  Future<void> _toggleUserActiveStatus(User user) async {
    try {
      // Backend'den güncellenmiş user bilgisini al
      final updatedUser = await UserService.updateUserActiveStatus(user.id, !user.isActive);
      
      // Local listeyi güncelle (backend'den gelen veri ile)
      setState(() {
        final index = filteredUsers.indexWhere((u) => u.id == user.id);
        if (index != -1) {
          filteredUsers[index] = updatedUser;
        }
      });
      
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('${user.name} ${user.isActive ? 'pasif' : 'aktif'} yapıldı'),
            backgroundColor: Colors.green,
          ),
        );
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Hata: $e'),
            backgroundColor: Colors.red,
          ),
        );
      }
    }
  }

  // Kullanıcı fotoğrafı ekle
  Future<void> _addUserPhoto(User user) async {
    try {
      // Image picker ile fotoğraf seç
      final ImagePicker picker = ImagePicker();
      final XFile? image = await picker.pickImage(
        source: ImageSource.gallery,
        maxWidth: 1024,
        maxHeight: 1024,
        imageQuality: 80,
      );
      
      if (image == null) return; // Kullanıcı iptal etti
      
      // Loading göster
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('Fotoğraf yükleniyor...'),
            backgroundColor: Colors.blue,
          ),
        );
      }
      
      // File oluştur ve backend'e gönder
      final File photoFile = File(image.path);
      final updatedUser = await UserService.updateUserPhoto(user.id, photoFile);
      
      // Local listeyi güncelle
      setState(() {
        final index = filteredUsers.indexWhere((u) => u.id == user.id);
        if (index != -1) {
          filteredUsers[index] = updatedUser;
        }
      });
      
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('${user.name} için fotoğraf başarıyla eklendi'),
            backgroundColor: Colors.green,
          ),
        );
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Fotoğraf ekleme hatası: $e'),
            backgroundColor: Colors.red,
          ),
        );
      }
    }
  }

  void _showDeleteConfirmation(User user) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Personel Sil'),
        content: Text('${user.name} adlı personeli silmek istediğinizden emin misiniz?'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('İptal'),
          ),
          TextButton(
            onPressed: () async {
              Navigator.pop(context);
              await _deleteUser(user.id);
            },
            style: TextButton.styleFrom(foregroundColor: Colors.red),
            child: const Text('Sil'),
          ),
        ],
      ),
    );
  }

  Future<void> _deleteUser(int userId) async {
    try {
      final success = await UserService.deleteUser(userId);
      if (success) {
        await _loadUsers();
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(content: Text('Personel başarıyla silindi')),
          );
        }
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Hata: $e')),
        );
      }
    }
  }
}
