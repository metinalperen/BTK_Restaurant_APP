import 'package:flutter/foundation.dart';

class Reservation {
  final String tableId;
  final String name;
  final String phone;
  final DateTime dateTime;
  final int people;
  final String? note;

  Reservation({
    required this.tableId,
    required this.name,
    required this.phone,
    required this.dateTime,
    required this.people,
    this.note,
  });
}

class ReservationsRepo extends ChangeNotifier {
  static final ReservationsRepo instance = ReservationsRepo._();
  ReservationsRepo._();

  final List<Reservation> _items = [];
  List<Reservation> get items => List.unmodifiable(_items);

  void add(Reservation r) {
    _items.add(r);
    notifyListeners();
  }

  void removeAt(int i) {
    _items.removeAt(i);
    notifyListeners();
  }

  void clear() {
    _items.clear();
    notifyListeners();
  }
}
