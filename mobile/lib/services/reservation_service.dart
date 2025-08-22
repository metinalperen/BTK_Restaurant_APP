import 'package:flutter/foundation.dart';
import '../models/reservation.dart';

class ReservationService {
  ReservationService._();
  static final ReservationService instance = ReservationService._();

  final ValueNotifier<List<Reservation>> reservations =
      ValueNotifier<List<Reservation>>([]);

  void add(Reservation r) {
    final list = List<Reservation>.from(reservations.value)..add(r);
    reservations.value = list;
  }

  List<Reservation> get all => reservations.value;
}
