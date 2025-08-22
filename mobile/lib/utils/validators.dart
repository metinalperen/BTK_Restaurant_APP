import 'package:flutter/services.dart';

class Validators {
  // Baş ve sondaki karakter harf olmalı; Türkçe harf + boşluk + tire + kesme
  static final RegExp _turkceIsimRegex = RegExp(
    r"^[A-Za-zÇĞİÖŞÜçğıöşü][A-Za-zÇĞİÖŞÜçğıöşü\-' ]{0,58}[A-Za-zÇĞİÖŞÜçğıöşü]$",
    unicode: true,
  );

  static String? adSoyadValidator(String? v) {
    final s = (v ?? '').trim();
    if (s.isEmpty) return 'Bu alan zorunludur';
    if (!_turkceIsimRegex.hasMatch(s)) {
      return 'Lütfen geçerli bir isim girin (Türkçe karakter destekli).';
    }
    return null;
  }

  static TextInputFormatter adSoyadFormatter() =>
      FilteringTextInputFormatter.allow(
        RegExp(r"[A-Za-zÇĞİÖŞÜçğıöşü\-' ]", unicode: true),
      );
}
