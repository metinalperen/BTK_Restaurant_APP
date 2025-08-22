import 'package:flutter/material.dart';
import '../screens/admin/admin_dashboard_screen.dart';

class ResponsiveScaffold extends StatefulWidget {
  const ResponsiveScaffold({super.key});

  @override
  State<ResponsiveScaffold> createState() => _ResponsiveScaffoldState();
}

class _ResponsiveScaffoldState extends State<ResponsiveScaffold> {
  int selectedIndex = 0;

  final List<Widget> _screens = const [
    AdminDashboardScreen(),
  
    Center(child: Text("Gelecek Sayfa")), // Placeholder
  ];

  final List<String> _titles = const [
    'Dashboard',
    'Rezervasyonlar',
    'Ürünler',
  ];

  void onItemTapped(int index) {
    Navigator.pop(context); // Menü kapansın
    setState(() {
      selectedIndex = index;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(_titles[selectedIndex]),
        backgroundColor: Colors.deepPurple,
      ),
      drawer: Drawer(
        child: ListView(
          padding: EdgeInsets.zero,
          children: [
            const DrawerHeader(
              decoration: BoxDecoration(color: Colors.deepPurple),
              child: Text('Admin Menü', style: TextStyle(color: Colors.white, fontSize: 24)),
            ),
            ListTile(
              leading: const Icon(Icons.dashboard),
              title: const Text('Dashboard'),
              onTap: () => onItemTapped(0),
            ),
            ListTile(
              leading: const Icon(Icons.event_seat),
              title: const Text('Rezervasyonlar'),
              onTap: () => onItemTapped(1),
            ),
            ListTile(
              leading: const Icon(Icons.shopping_bag),
              title: const Text('Ürünler'),
              onTap: () => onItemTapped(2),
            ),
          ],
        ),
      ),
      body: _screens[selectedIndex],
    );
  }
}
