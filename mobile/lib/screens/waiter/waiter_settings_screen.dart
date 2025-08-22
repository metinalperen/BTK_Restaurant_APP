import 'package:flutter/material.dart';

class WaiterSettingsScreen extends StatefulWidget {
  const WaiterSettingsScreen({Key? key}) : super(key: key);

  @override
  State<WaiterSettingsScreen> createState() => _WaiterSettingsScreenState();
}

class _WaiterSettingsScreenState extends State<WaiterSettingsScreen> {
  bool _isDarkMode = false;
  String _selectedTheme = 'Açık';
  String _selectedLanguage = 'Türkçe';

  void _showThemeDialog() {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: Text('Tema Seçimi'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            ListTile(
              title: Text('Açık'),
              leading: Radio<String>(
                value: 'Açık',
                groupValue: _selectedTheme,
                onChanged: (value) {
                  setState(() => _selectedTheme = value!);
                  Navigator.pop(context);
                },
              ),
            ),
            ListTile(
              title: Text('Koyu'),
              leading: Radio<String>(
                value: 'Koyu',
                groupValue: _selectedTheme,
                onChanged: (value) {
                  setState(() => _selectedTheme = value!);
                  Navigator.pop(context);
                },
              ),
            ),
          ],
        ),
      ),
    );
  }

  void _showLanguageDialog() {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: Text('Dil Seçimi'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            ListTile(
              title: Text('Türkçe'),
              leading: Radio<String>(
                value: 'Türkçe',
                groupValue: _selectedLanguage,
                onChanged: (value) {
                  setState(() => _selectedLanguage = value!);
                  Navigator.pop(context);
                },
              ),
            ),
            ListTile(
              title: Text('English'),
              leading: Radio<String>(
                value: 'English',
                groupValue: _selectedLanguage,
                onChanged: (value) {
                  setState(() => _selectedLanguage = value!);
                  Navigator.pop(context);
                },
              ),
            ),
          ],
        ),
      ),
    );
  }

  void _showAboutDialog() {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: Text('Hakkında'),
        content: Text('Restoran yönetim uygulaması v1.0.0\n\nGarson paneli - Masa yönetimi ve sipariş takibi'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: Text('Kapat'),
          ),
        ],
      ),
    );
  }

  void _showHelpDialog() {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: Text('Yardım'),
        content: Text('Garson Paneli Kullanım Kılavuzu:\n\n• Masalara tıklayarak sipariş alabilirsiniz\n• Dolu masalara tıklayarak mevcut siparişi görebilirsiniz\n• Rezervasyonlar sayfasından rezervasyonları takip edebilirsiniz\n• Stok durumu sayfasından ürün stoklarını görebilirsiniz'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: Text('Kapat'),
          ),
        ],
      ),
    );
  }

  void _logout() {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: Text('Çıkış Yap'),
        content: Text('Hesabınızdan çıkmak istediğinizden emin misiniz?'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: Text('İptal'),
          ),
          ElevatedButton(
            onPressed: () {
              Navigator.pop(context);
              Navigator.of(context).pushReplacementNamed('/login');
            },
            style: ElevatedButton.styleFrom(backgroundColor: Colors.red),
            child: Text('Çıkış Yap'),
          ),
        ],
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.grey[50],
      appBar: AppBar(
        title: Text('Ayarlar'),
        backgroundColor: Colors.orange,
        foregroundColor: Colors.white,
      ),
      body: SingleChildScrollView(
        padding: EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Görünüm Ayarları
            _buildSectionHeader('Görünüm Ayarları', Icons.palette, Colors.orange),
            _buildSettingCard(
              'Tema',
              Icons.palette,
              _selectedTheme,
              onTap: _showThemeDialog,
            ),
            _buildSettingCard(
              'Dil',
              Icons.language,
              _selectedLanguage,
              onTap: _showLanguageDialog,
            ),
            _buildSwitchCard(
              'Karanlık Mod',
              Icons.dark_mode,
              'Karanlık tema kullan',
              _isDarkMode,
              (value) => setState(() => _isDarkMode = value),
            ),

            SizedBox(height: 24),

            // Uygulama Bilgileri
            _buildSectionHeader('Uygulama Bilgileri', Icons.info, Colors.blue),
            _buildInfoCard(
              'Versiyon',
              Icons.info,
              '1.0.0',
            ),
            _buildSettingCard(
              'Hakkında',
              Icons.description,
              'Restoran yönetim uygulaması',
              onTap: _showAboutDialog,
            ),
            _buildSettingCard(
              'Yardım',
              Icons.help,
              'Kullanım kılavuzu',
              onTap: _showHelpDialog,
            ),

            SizedBox(height: 24),

            // Hesap
            _buildSectionHeader('Hesap', Icons.account_circle, Colors.red),
            _buildSettingCard(
              'Çıkış Yap',
              Icons.logout,
              'Hesabınızdan güvenli çıkış',
              onTap: _logout,
              textColor: Colors.red,
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildSectionHeader(String title, IconData icon, Color color) {
    return Container(
      margin: EdgeInsets.only(bottom: 16),
      child: Row(
        children: [
          Icon(icon, color: color, size: 24),
          SizedBox(width: 12),
          Text(
            title,
            style: TextStyle(
              fontSize: 18,
              fontWeight: FontWeight.bold,
              color: color,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildSettingCard(String title, IconData icon, String subtitle, {
    VoidCallback? onTap,
    Color? textColor,
  }) {
    return Card(
      margin: EdgeInsets.only(bottom: 8),
      child: ListTile(
        leading: Icon(icon, color: Colors.grey[600]),
        title: Text(title),
        subtitle: Text(
          subtitle,
          style: TextStyle(color: textColor ?? Colors.grey[600]),
        ),
        trailing: Icon(Icons.arrow_forward_ios, size: 16),
        onTap: onTap,
      ),
    );
  }

  Widget _buildSwitchCard(String title, IconData icon, String subtitle, bool value, ValueChanged<bool> onChanged) {
    return Card(
      margin: EdgeInsets.only(bottom: 8),
      child: SwitchListTile(
        secondary: Icon(icon, color: Colors.grey[600]),
        title: Text(title),
        subtitle: Text(subtitle, style: TextStyle(color: Colors.grey[600])),
        value: value,
        onChanged: onChanged,
        activeColor: Colors.orange,
      ),
    );
  }

  Widget _buildInfoCard(String title, IconData icon, String value) {
    return Card(
      margin: EdgeInsets.only(bottom: 8),
      child: ListTile(
        leading: Icon(icon, color: Colors.grey[600]),
        title: Text(title),
        subtitle: Text(value, style: TextStyle(color: Colors.grey[600])),
        trailing: Container(
          width: 8,
          height: 8,
          decoration: BoxDecoration(
            color: Colors.black,
            shape: BoxShape.circle,
          ),
        ),
      ),
    );
  }
}
