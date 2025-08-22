import 'package:flutter/material.dart';
import 'package:restaurant_app/screens/waiter/waiter_home_screen.dart';
import 'package:restaurant_app/screens/waiter/waiter_reservations_screen.dart';
import 'package:restaurant_app/screens/waiter/waiter_stock_screen.dart';
import 'package:restaurant_app/screens/waiter/waiter_orders_screen.dart';

class WaiterMainScreen extends StatefulWidget {
  const WaiterMainScreen({super.key});

  @override
  State<WaiterMainScreen> createState() => _WaiterMainScreenState();
}

class _WaiterMainScreenState extends State<WaiterMainScreen> {
  int _selectedIndex = 0;

  final List<Widget> _screens = [
    const WaiterHomeScreen(),
    const WaiterOrdersScreen(),
    const WaiterReservationsScreen(),
    const WaiterStockScreen(),
  ];

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: _screens[_selectedIndex],
      bottomNavigationBar: BottomNavigationBar(
        type: BottomNavigationBarType.fixed,
        currentIndex: _selectedIndex,
        onTap: (index) {
          setState(() {
            _selectedIndex = index;
          });
        },
        selectedItemColor: Colors.purple[600],
        unselectedItemColor: Colors.grey[600],
        items: const [
          BottomNavigationBarItem(
            icon: Icon(Icons.home),
            label: 'Anasayfa',
          ),
          BottomNavigationBarItem(
            icon: Icon(Icons.receipt_long),
            label: 'Siparişlerim',
          ),
          BottomNavigationBarItem(
            icon: Icon(Icons.calendar_today),
            label: 'Rezervasyonlar',
          ),
          BottomNavigationBarItem(
            icon: Icon(Icons.inventory),
            label: 'Stok Durumu',
          ),
        ],
      ),
    );
  }
}
