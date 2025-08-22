import 'package:flutter/material.dart';
import '../models/reservation.dart';

class TableInfo {
  final String id;
  int capacity;
  TableInfo({required this.id, required this.capacity});
}

class Salon {
  String name;
  final List<TableInfo> tables;
  Salon({required this.name, required this.tables});
}

class AppState {
  // İş kuralları
  static const int sessionHours = 2;              // Masa "dolu" sayılma süresi
  static const int minGapHours = 4;               // Aynı masa için min. aralık
  static const int yellowThresholdMin = 59;       // Sarı uyarı eşiği
  static const int openHour = 9;                  // 09:00
  static const int closeHour = 22;                // 22:00 (başlangıcı hariç)

  // Salonlar (başlangıç)
  static final ValueNotifier<List<Salon>> salons = ValueNotifier<List<Salon>>([
    Salon(name: 'Zemin', tables: List.generate(8, (i) => TableInfo(id: 'Z${i + 1}', capacity: [5,4,3,2][i%4]))),
    Salon(name: 'Kat 1', tables: List.generate(8, (i) => TableInfo(id: 'A${i + 1}', capacity: [3,5,4,2][i%4]))),
    Salon(name: 'Kat 2', tables: List.generate(8, (i) => TableInfo(id: 'B${i + 1}', capacity: [2,4,5,3][i%4]))),
  ]);

  // Rezervasyonlar
  static final ValueNotifier<List<Reservation>> reservations =
      ValueNotifier<List<Reservation>>([]);

  // ---------- Yardımcılar ----------
  static List<TableInfo> tablesOf(int salonIndex) =>
      salons.value[salonIndex].tables;

  static void addSalon(String name) {
    final newSalon = Salon(name: name, tables: []);
    salons.value = [...salons.value, newSalon];
  }

  static void addTable(int salonIndex, {required String id, required int capacity}) {
    final copy = [...salons.value];
    copy[salonIndex].tables.add(TableInfo(id: id, capacity: capacity));
    salons.value = copy;
  }
  
  static void removeTable(int salonIndex, String tableId) {
    final copy = [...salons.value];
    copy[salonIndex].tables.removeWhere((table) => table.id == tableId);
    salons.value = copy;
  }
  
  static void updateTable(int salonIndex, String tableId, {int? capacity}) {
    final copy = [...salons.value];
    final table = copy[salonIndex].tables.firstWhere((table) => table.id == tableId);
    if (capacity != null) {
      table.capacity = capacity;
    }
    salons.value = copy;
  }

  static void addReservation(Reservation r) {
    reservations.value = [...reservations.value, r];
  }

  static void removeReservation(Reservation r) {
    reservations.value = [...reservations.value]..remove(r);
  }

  static List<Reservation> reservationsForTable(String tableId) =>
      reservations.value.where((r) => r.tableId == tableId).toList();

  // Sayaçlar
  static int totalTables(int salonIndex) => salons.value[salonIndex].tables.length;

  static int occupiedNow(int salonIndex, DateTime now) {
    final ids = salons.value[salonIndex].tables.map((e) => e.id).toSet();
    return reservations.value.where((r) =>
      ids.contains(r.tableId) &&
      now.isAfter(r.dateTime) &&
      now.isBefore(r.dateTime.add(Duration(hours: sessionHours)))
    ).length;
  }

  static int reservedUpcoming(int salonIndex, DateTime now) {
    final ids = salons.value[salonIndex].tables.map((e) => e.id).toSet();
    return reservations.value.where((r) =>
      ids.contains(r.tableId) && r.dateTime.isAfter(now)
    ).length;
  }

  static int freeNow(int salonIndex, DateTime now) {
    final total = totalTables(salonIndex);
    final occ = occupiedNow(salonIndex, now);
    final res = reservedUpcoming(salonIndex, now);
    return (total - occ - res).clamp(0, total);
  }

  // ---------- Masa bazlı durumlar ----------
  static bool isOccupiedForTable(String tableId, DateTime now) =>
      reservations.value.any((r) =>
        r.tableId == tableId &&
        now.isAfter(r.dateTime) &&
        now.isBefore(r.dateTime.add(Duration(hours: sessionHours)))
      );

  static Reservation? nearestUpcomingForTable(String tableId, DateTime now) {
    final ups = reservations.value
        .where((r) => r.tableId == tableId && r.dateTime.isAfter(now))
        .toList()
      ..sort((a, b) => a.dateTime.compareTo(b.dateTime));
    return ups.isEmpty ? null : ups.first;
  }

  static bool isYellowForTable(String tableId, DateTime now) {
    final next = nearestUpcomingForTable(tableId, now);
    if (next == null) return false;
    final diff = next.dateTime.difference(now).inMinutes;
    return diff >= 0 && diff <= yellowThresholdMin;
  }

  // ---------- Kurallar ----------
  static bool withinOpenHours(DateTime dt) {
    final start = DateTime(dt.year, dt.month, dt.day, openHour, 0);
    final end   = DateTime(dt.year, dt.month, dt.day, closeHour, 0);
    return !dt.isBefore(start) && dt.isBefore(end); // [09:00,22:00)
  }

  // dt, aynı masadaki herhangi bir rezervasyona minGapHours içinde mi?
  static DateTime? nextAllowedTime(String tableId, DateTime dt) {
    DateTime? blockEnd;
    for (final r in reservationsForTable(tableId)) {
      final begin = r.dateTime.subtract(Duration(hours: minGapHours));
      final end   = r.dateTime.add(Duration(hours: minGapHours));
      if (!dt.isBefore(begin) && !dt.isAfter(end)) {
        final e = end;
        if (blockEnd == null || e.isAfter(blockEnd)) blockEnd = e;
      }
    }
    return blockEnd; // null => uygun
  }
}
