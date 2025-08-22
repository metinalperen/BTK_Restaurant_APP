import 'package:flutter/material.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'screens/login_screen.dart';
import 'screens/forgot_password_screen.dart';
import 'screens/reset_password_screen.dart';
import 'widgets/admin_navbar.dart';
import 'screens/waiter/waiter_main_screen.dart';
import 'screens/cashier_screen.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      title: 'Restoran Uygulama',
      theme: ThemeData(
        fontFamily: 'Arial', 
        primarySwatch: Colors.deepPurple,
        // Türkçe karakter desteği için
        textTheme: const TextTheme(
          bodyLarge: TextStyle(fontFamily: 'Arial'),
          bodyMedium: TextStyle(fontFamily: 'Arial'),
          titleLarge: TextStyle(fontFamily: 'Arial'),
          titleMedium: TextStyle(fontFamily: 'Arial'),
          titleSmall: TextStyle(fontFamily: 'Arial'),
        ),
      ),
      // Türkçe dil desteği
      localizationsDelegates: const [
        GlobalMaterialLocalizations.delegate,
        GlobalWidgetsLocalizations.delegate,
        GlobalCupertinoLocalizations.delegate,
      ],
      supportedLocales: const [
        Locale('tr', 'TR'), // Türkçe
        Locale('en', 'US'), // İngilizce
      ],
      locale: const Locale('tr', 'TR'), // Varsayılan dil Türkçe
      initialRoute: '/',
      routes: {
        '/': (context) => const LoginScreen(),
        '/forgot-password': (context) => const ForgotPasswordScreen(),
        '/reset-password': (context) => const ResetPasswordScreen(),
        // Admin tabbar'ı (aşağıda)
        '/admin': (context) => const AdminNavbar(),
        '/garson': (context) => const WaiterMainScreen(),
        '/kasiyer': (context) => const CashierScreen(),
      },
    );
  }
}
