class User {
  final int id;
  final String name;
  final String email;
  final String? phoneNumber;
  final String? photoBase64;
  final DateTime? createdAt;
  final List<int> roles;
  final bool isActive;

  User({
    required this.id,
    required this.name,
    required this.email,
    this.phoneNumber,
    this.photoBase64,
    this.createdAt,
    required this.roles,
    required this.isActive,
  });

  factory User.fromJson(Map<String, dynamic> json) {
    return User(
      id: json['id'] ?? 0,
      name: json['name'] ?? '',
      email: json['email'] ?? '',
      phoneNumber: json['phoneNumber'],
      photoBase64: json['photoBase64'],
      createdAt: json['createdAt'] != null 
          ? DateTime.parse(json['createdAt']) 
          : null,
      roles: List<int>.from(json['roles'] ?? []),
      isActive: json['isActive'] ?? true,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'name': name,
      'email': email,
      'phoneNumber': phoneNumber,
      'createdAt': createdAt?.toIso8601String(),
      'roleName': _getRoleNameFromRoles(),
    };
  }

  // Role enum'ları
  static const int ROLE_ADMIN = 0;
  static const int ROLE_WAITER = 1;
  static const int ROLE_CASHIER = 2;

  // İlk role'ü al (genellikle tek role olur)
  int get primaryRoleId => roles.isNotEmpty ? roles.first : ROLE_WAITER;

  // Role adını getir
  String get roleDisplayName {
    if (roles.isEmpty) return 'Bilinmeyen';
    
    final roleId = roles.first;
    switch (roleId) {
      case ROLE_ADMIN:
        return 'Yönetici';
      case ROLE_WAITER:
        return 'Garson';
      case ROLE_CASHIER:
        return 'Kasiyer';
      default:
        return 'Bilinmeyen';
    }
  }

  // Role rengini getir
  String get roleColor {
    if (roles.isEmpty) return '#718096';
    
    final roleId = roles.first;
    switch (roleId) {
      case ROLE_ADMIN:
        return '#6B46C1'; // Mor
      case ROLE_WAITER:
        return '#38A169'; // Yeşil
      case ROLE_CASHIER:
        return '#3182CE'; // Mavi
      default:
        return '#718096'; // Gri
    }
  }

  // Backend'e gönderilecek role adını getir
  String _getRoleNameFromRoles() {
    if (roles.isEmpty) return 'waiter';
    
    final roleId = roles.first;
    switch (roleId) {
      case ROLE_ADMIN:
        return 'admin';
      case ROLE_WAITER:
        return 'waiter';
      case ROLE_CASHIER:
        return 'cashier';
      default:
        return 'waiter';
    }
  }

  // Role ID'sinden role adını getir
  static String getRoleNameFromId(int roleId) {
    switch (roleId) {
      case ROLE_ADMIN:
        return 'admin';
      case ROLE_WAITER:
        return 'waiter';
      case ROLE_CASHIER:
        return 'cashier';
      default:
        return 'waiter';
    }
  }

  // Role adından role ID'sini getir
  static int getRoleIdFromName(String roleName) {
    switch (roleName.toLowerCase()) {
      case 'admin':
        return ROLE_ADMIN;
      case 'waiter':
        return ROLE_WAITER;
      case 'cashier':
        return ROLE_CASHIER;
      default:
        return ROLE_WAITER;
    }
  }
}
