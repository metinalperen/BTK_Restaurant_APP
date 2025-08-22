// lib/screens/admin/admin_dashboard_screen.dart

import 'package:flutter/material.dart';

class AdminDashboardScreen extends StatelessWidget { // ← DEĞİŞTİRİLDİ
  const AdminDashboardScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: const [
          Text("Dashboard", style: TextStyle(fontSize: 24)),
          SizedBox(height: 20),
          Text("Hoşgeldiniz, Admin!"),
        ],
      ),
    );
  }
}
