import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import 'dart:convert';

class ResetPasswordScreen extends StatefulWidget {
  const ResetPasswordScreen({super.key});

  @override
  State<ResetPasswordScreen> createState() => _ResetPasswordScreenState();
}

class _ResetPasswordScreenState extends State<ResetPasswordScreen> {
  final newPasswordController = TextEditingController();
  final confirmPasswordController = TextEditingController();

  String error = '';
  String message = '';
  bool loading = false;
  late String token;

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    // Get token from arguments
    final args = ModalRoute.of(context)!.settings.arguments as Map<String, dynamic>;
    token = args['token'];
  }

  Future<void> resetPassword() async {
    final password = newPasswordController.text;
    final confirmPassword = confirmPasswordController.text;

    if (password.isEmpty || confirmPassword.isEmpty) {
      setState(() => error = 'Lütfen tüm alanları doldurun.');
      return;
    }

    if (password != confirmPassword) {
      setState(() => error = 'Şifreler eşleşmiyor.');
      return;
    }

    setState(() {
      loading = true;
      error = '';
      message = '';
    });

    try {
      final response = await http.post(
        Uri.parse('http://<BACKEND_URL>/api/reset-password'), // ← endpoint'e göre güncelle
        headers: {'Content-Type': 'application/json'},
        body: jsonEncode({'token': token, 'newPassword': password}),
      );

      if (response.statusCode == 200) {
        setState(() => message = 'Şifre başarıyla güncellendi.');

        Future.delayed(Duration(seconds: 3), () {
          Navigator.pushReplacementNamed(context, '/login');
        });
      } else {
        setState(() => error = 'Şifre sıfırlama başarısız oldu.');
      }
    } catch (e) {
      setState(() => error = 'Hata: ${e.toString()}');
    } finally {
      setState(() => loading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Color(0xFFF5EFFF),
      body: Center(
        child: SingleChildScrollView(
          padding: const EdgeInsets.all(24),
          child: Container(
            width: 420,
            padding: const EdgeInsets.all(24),
            decoration: BoxDecoration(
              color: Color(0xFFCBC3E3),
              borderRadius: BorderRadius.circular(20),
              boxShadow: [BoxShadow(color: Colors.black12, blurRadius: 12)],
            ),
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                CircleAvatar(
                  radius: 35,
                  backgroundColor: Color(0xFFA294F9),
                  child: Icon(Icons.vpn_key, color: Colors.white),
                ),
                SizedBox(height: 16),
                Text('Yeni Şifre Belirle',
                    style: TextStyle(fontSize: 22, fontWeight: FontWeight.bold, color: Color(0xFFA294F9))),
                SizedBox(height: 8),
                Text('Yeni şifrenizi belirleyin',
                    textAlign: TextAlign.center,
                    style: TextStyle(fontSize: 14, color: Color(0xFF2D1B69))),
                SizedBox(height: 24),
                TextField(
                  controller: newPasswordController,
                  obscureText: true,
                  decoration: InputDecoration(
                    labelText: 'Yeni Şifre',
                    filled: true,
                    fillColor: Color(0xFFE5D9F2),
                    border: OutlineInputBorder(borderRadius: BorderRadius.circular(12)),
                  ),
                ),
                SizedBox(height: 16),
                TextField(
                  controller: confirmPasswordController,
                  obscureText: true,
                  decoration: InputDecoration(
                    labelText: 'Şifre Tekrar',
                    filled: true,
                    fillColor: Color(0xFFE5D9F2),
                    border: OutlineInputBorder(borderRadius: BorderRadius.circular(12)),
                  ),
                ),
                if (error.isNotEmpty)
                  Padding(
                    padding: const EdgeInsets.symmetric(vertical: 12),
                    child: Text(error, style: TextStyle(color: Colors.red)),
                  ),
                if (message.isNotEmpty)
                  Padding(
                    padding: const EdgeInsets.symmetric(vertical: 12),
                    child: Text(message, style: TextStyle(color: Colors.green[700])),
                  ),
                SizedBox(height: 20),
                ElevatedButton(
                  onPressed: loading ? null : resetPassword,
                  child: Text(loading ? 'Güncelleniyor...' : 'Şifreyi Güncelle'),
                  style: ElevatedButton.styleFrom(
                    minimumSize: Size(double.infinity, 48),
                    backgroundColor: Color(0xFFA294F9),
                    shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
