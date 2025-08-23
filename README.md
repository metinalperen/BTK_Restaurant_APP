# 🍽️ BTK Restaurant Management System

<div align="center">

![Restaurant Management](https://img.shields.io/badge/Restaurant-Management%20System-blue?style=for-the-badge&logo=restaurant)
![Multi-Platform](https://img.shields.io/badge/Multi--Platform-Web%20%7C%20Mobile%20%7C%20Backend-green?style=for-the-badge)
![Spring Boot](https://img.shields.io/badge/Backend-Spring%20Boot%203.3.2-orange?style=for-the-badge&logo=spring)
![React](https://img.shields.io/badge/Frontend-React%2019.1.0-blue?style=for-the-badge&logo=react)
![Flutter](https://img.shields.io/badge/Mobile-Flutter%203.7.2-02569B?style=for-the-badge&logo=flutter)

**Modern, Kapsamlı ve Kullanıcı Dostu Restoran Yönetim Sistemi**

</div>

---

## 📋 İçindekiler

- [🎯 Proje Hakkında](#-proje-hakkında)
- [✨ Özellikler](#-özellikler)
- [🏗️ Mimari](#️-mimari)
- [🔧 Teknolojiler](#-teknolojiler)
- [⚙️ Kurulum](#️-kurulum)
- [👥 Geliştirici Ekibi](#-geliştirici-ekibi)

---

## 🎯 Proje Hakkında

**BTK Restaurant Management System**, modern restoran işletmelerinin ihtiyaçlarını karşılamak üzere tasarlanmış kapsamlı bir yönetim sistemidir. Sistem, web tabanlı admin paneli, mobil uygulama ve güçlü backend API'si ile tam entegre bir çözüm sunar.

Bu proje, **BTK (Bilgi Teknolojileri ve İletişim Kurumu)** staj programı kapsamında geliştirilmiştir.

### 🎯 Hedefler

- **Operasyonel Verimlilik**: Günlük restoran operasyonlarını optimize etmek
- **Gerçek Zamanlı İzleme**: Canlı sipariş ve rezervasyon takibi
- **Analitik İçgörüler**: Detaylı raporlama ve performans analizi
- **Kullanıcı Deneyimi**: Sezgisel ve kullanıcı dostu arayüzler
- **Ölçeklenebilirlik**: Küçük kafelerden büyük restoranlara kadar uyumluluk

---

## ✨ Özellikler

### 🍽️ **Sipariş Yönetimi**
- **Gerçek Zamanlı Sipariş Takibi**: Anlık sipariş durumu güncellemeleri
- **Çoklu Kategori Desteği**: Yemek, içecek, tatlı kategorileri
- **Özel Sipariş Notları**: Müşteri tercihleri ve özel istekler
- **Hızlı Sipariş Alma**: Garson ve kasiyer için optimize edilmiş arayüzler
- **Sipariş Geçmişi**: Detaylı sipariş arşivi ve analizi

### 📅 **Rezervasyon Sistemi**
- **Akıllı Rezervasyon Yönetimi**: Masa ve zaman bazlı rezervasyon
- **Otomatik Çakışma Kontrolü**: Rezervasyon çakışmalarını önleme
- **Müşteri Bilgi Yönetimi**: Müşteri profilleri ve tercihleri
- **Rezervasyon Hatırlatmaları**: Otomatik bildirim sistemi
- **Rezervasyon Düzenleme**: Kolay güncelleme ve iptal işlemleri

### 🏪 **Masa Yönetimi**
- **Salon ve Masa Organizasyonu**: Çoklu salon desteği
- **Gerçek Zamanlı Masa Durumu**: Boş, dolu, rezerve durumları
- **Masa Kapasitesi Yönetimi**: Farklı boyutlarda masalar
- **Dinamik Masa Oluşturma**: İhtiyaca göre masa ekleme/çıkarma
- **Masa Transferi**: Müşterileri masalar arası taşıma

### 👥 **Personel Yönetimi**
- **Rol Tabanlı Erişim**: Admin, garson, kasiyer rolleri
- **Personel Performans Takibi**: Sipariş ve gelir analizi
- **Vardiya Yönetimi**: Çalışma saatleri ve görev dağılımı
- **Personel Profilleri**: Detaylı personel bilgileri
- **Yetkilendirme Sistemi**: Güvenli erişim kontrolü

### 📊 **Analitik ve Raporlama**
- **Gerçek Zamanlı Analitik**: Anlık satış ve performans verileri
- **Günlük/Haftalık/Aylık Raporlar**: Kapsamlı raporlama sistemi
- **Ürün Performans Analizi**: En çok satan ürünler
- **Kategori Bazlı Satış**: Kategori performans analizi
- **Personel Performans Raporları**: Çalışan verimlilik analizi
- **Gelir-Gider Takibi**: Finansal performans izleme

### 🛒 **Stok Yönetimi**
- **Otomatik Stok Takibi**: Ürün bazlı stok kontrolü
- **Stok Uyarıları**: Düşük stok bildirimleri
- **Stok Güncelleme**: Kolay stok miktarı güncelleme
- **Stok Geçmişi**: Stok değişim kayıtları

### ⚙️ **Sistem Ayarları**
- **Restoran Bilgileri**: İsim, açılış-kapanış saatleri
- **Rezervasyon Kuralları**: Rezervasyon kesinti süreleri
- **Tema Özelleştirme**: Açık/koyu tema desteği
- **Bildirim Ayarları**: Sistem bildirimleri yapılandırması

---

## 🏗️ Mimari

### 📐 **Sistem Mimarisi**

```
┌─────────────────────────────────────────────────────────────────┐
│                    BTK Restaurant Management System             │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐        │
│  │    Web      │    │   Mobile    │    │   Backend   │        │
│  │  Frontend   │    │   App       │    │    API      │        │
│  │   React     │    │  Flutter    │    │ Spring Boot │        │
│  └─────────────┘    └─────────────┘    └─────────────┘        │
│         │                   │                   │              │
│         └───────────────────┼───────────────────┘              │
│                             │                                  │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │                 PostgreSQL Database                     │   │
│  │  • Users & Authentication                               │   │
│  │  • Orders & Order Items                                 │   │
│  │  • Reservations & Tables                                │   │
│  │  • Products & Categories                                │   │
│  │  • Analytics & Reports                                  │   │
│  └─────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
```

---

## 🔧 Teknolojiler

### 🖥️ **Backend (Spring Boot 3.3.2)**
- **Framework**: Spring Boot 3.3.2
- **Java Version**: 21
- **Database**: PostgreSQL 42.7.7
- **Security**: Spring Security + JWT
- **ORM**: Spring Data JPA + Hibernate
- **Validation**: Bean Validation
- **Email**: Spring Mail
- **Scheduling**: Spring Scheduling
- **Build Tool**: Maven

### 🌐 **Web Frontend (React 19.1.0)**
- **Framework**: React 19.1.0
- **Build Tool**: Vite 7.0.4
- **UI Library**: Material-UI 7.3.1, Ant Design 5.27.0
- **Styling**: Bootstrap 5.3.7, Emotion
- **State Management**: Redux Toolkit 2.8.2
- **Routing**: React Router DOM 7.8.0
- **Charts**: Chart.js 4.5.0 + React Chart.js 2
- **HTTP Client**: Axios 1.11.0
- **Icons**: Lucide React 0.536.0

### 📱 **Mobile App (Flutter 3.7.2)**
- **Framework**: Flutter 3.7.2
- **Language**: Dart
- **UI**: Material Design 3
- **Localization**: Flutter Localizations
- **Image Picker**: Image Picker 1.0.7
- **State Management**: Provider Pattern
- **HTTP**: Dio (via services)

### 🗄️ **Database**
- **Primary Database**: PostgreSQL
- **Migration Tool**: Flyway
- **Connection Pool**: HikariCP
- **Backup**: Automated SQL dumps

### 🔐 **Güvenlik**
- **Authentication**: JWT (JSON Web Tokens)
- **Authorization**: Role-based access control
- **Password Hashing**: BCrypt
- **CORS**: Configured for cross-origin requests
- **Input Validation**: Server-side validation

---

## ⚙️ Kurulum

### 📋 **Gereksinimler**

- **Java**: 21 veya üzeri
- **Node.js**: 18 veya üzeri
- **Flutter**: 3.7.2 veya üzeri
- **PostgreSQL**: 12 veya üzeri
- **Maven**: 3.6 veya üzeri

### 🚀 **Hızlı Başlangıç**

#### 1. **Repository'yi Klonlayın**
```bash
git clone https://github.com/metinalperen/BTK_Restaurant_APP.git
cd BTK_Restaurant_APP
```

#### 2. **Backend Kurulumu**
```bash
cd backend
mvn spring-boot:run
```

#### 3. **Web Frontend Kurulumu**
```bash
cd web/restoran-yonetim-sistemi
npm install
npm run dev
```

#### 4. **Mobile App Kurulumu**
```bash
cd mobile
flutter pub get
flutter run
```

---

## 👥 Geliştirici Ekibi

### 🎯 **Proje Yöneticisi**
- **Metin Alperen UÇAN**
- **Ozan Ahmet DEDE**

### 💻 **Geliştiriciler**

- **Pelin DAĞ** 
- **Muhammed Nasih AYDIN**
- **Gül YASEMİN**
- **Kübra SOYSAL**
- **Rüveyda BAYRAM**
- **Gül YETİK**
- **İbrahim ÇELİK**
- **Mehmet ÖZÇELİK**
- **Zeynep Ruken BALCI**
- **Sude Melek ACAR** 
- **Miyasenur TAŞKIN**
- **Muhammet Salih HASILCIO**
- **Umut SARAÇ**
- **Selin ÇALIŞKAN**
- **Betül KEMANECİ**
- **Zeki Furkan YILDIZ**

---

<div align="center">

**⭐ Bu projeyi beğendiyseniz yıldız vermeyi unutmayın!**

**🍽️ BTK Restaurant Management System** - Modern restoran yönetimi için en iyi çözüm

</div>