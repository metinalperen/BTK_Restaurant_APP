import 'package:flutter/material.dart';
import '../screens/admin/home_screen.dart';
import '../screens/admin/product_screen.dart';
import '../screens/admin/users_screen.dart';
import '../screens/admin/report_screen.dart';
import '../screens/admin/reservations_screen.dart';
import '../screens/admin/restaurant_management_screen.dart';
import '../screens/admin/order_history_screen.dart';
import '../screens/admin/activity_logs_screen.dart';

class AdminNavbar extends StatefulWidget {
  const AdminNavbar({super.key});
  @override
  State<AdminNavbar> createState() => _AdminNavbarState();
}

class _AdminNavbarState extends State<AdminNavbar> {
  int _selectedIndex = 0;

  final List<Widget> _pages = const [
    HomeScreen(),
    UsersScreen(),
    ReportScreen(),
    ProductScreen(),
    ReservationsScreen(),
    RestaurantManagementScreen(),
    OrderHistoryScreen(),
    ActivityLogsScreen(),
  ];

  void _onItemTapped(int index) => setState(() => _selectedIndex = index);

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: _pages[_selectedIndex],
      bottomNavigationBar: Container(
        height: 80,
        decoration: BoxDecoration(
          color: Colors.white,
          boxShadow: [
            BoxShadow(
              color: Colors.grey.withOpacity(0.2),
              spreadRadius: 1,
              blurRadius: 5,
              offset: const Offset(0, -2),
            ),
          ],
        ),
        child: SingleChildScrollView(
          scrollDirection: Axis.horizontal,
          child: Row(
            mainAxisAlignment: MainAxisAlignment.spaceEvenly,
            children: [
              _buildNavItem(0, Icons.home, 'Ana Sayfa', Colors.pink),
              _buildNavItem(1, Icons.person, 'Personel', Colors.purple),
              _buildNavItem(2, Icons.bar_chart, 'Rapor', Colors.green),
              _buildNavItem(3, Icons.restaurant_menu, 'Ürün Yönetimi', Colors.deepPurple),
              _buildNavItem(4, Icons.event, 'Rezervasyonlar', Colors.lightBlue),
              _buildNavItem(5, Icons.store, 'Restoran Yönetimi', Colors.indigo),
              _buildNavItem(6, Icons.receipt_long, 'Sipariş Geçmişi', Colors.orange),
              _buildNavItem(7, Icons.explore, 'Aktivite Logları', Colors.amber),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildNavItem(int index, IconData icon, String label, Color color) {
    final isSelected = _selectedIndex == index;
    return GestureDetector(
      onTap: () => _onItemTapped(index),
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
        margin: const EdgeInsets.symmetric(horizontal: 4),
        decoration: BoxDecoration(
          color: isSelected ? color.withOpacity(0.1) : Colors.transparent,
          borderRadius: BorderRadius.circular(12),
          border: Border.all(
            color: isSelected ? color : Colors.grey.withOpacity(0.3),
            width: isSelected ? 2 : 1,
          ),
        ),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(
              icon,
              color: isSelected ? color : Colors.grey[600],
              size: 24,
            ),
            const SizedBox(height: 4),
            Text(
              label,
              style: TextStyle(
                color: isSelected ? color : Colors.grey[600],
                fontSize: 10,
                fontWeight: isSelected ? FontWeight.bold : FontWeight.normal,
              ),
            ),
          ],
        ),
      ),
    );
  }
}
