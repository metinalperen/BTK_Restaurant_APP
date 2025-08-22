class Reservation {
  final String id;        // benzersiz id (ms epoch vs.)
  final String salonName; // Zemin / Kat 1 / Kat 2 / ...
  final String tableId;   // Örn: Z2
  final String firstName; // Ad
  final String lastName;  // Soyad
  final String phone;
  final DateTime dateTime;
  final int partySize;
  final String? email;
  final String? note;

  Reservation({
    required this.id,
    required this.salonName,
    required this.tableId,
    required this.firstName,
    required this.lastName,
    required this.phone,
    required this.dateTime,
    required this.partySize,
    this.email,
    this.note,
  });
}
