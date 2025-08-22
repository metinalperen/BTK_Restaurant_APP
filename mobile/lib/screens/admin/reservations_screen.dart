import 'package:flutter/material.dart';
import '../../data/app_state.dart';
import '../../models/reservation.dart';

class ReservationsScreen extends StatelessWidget {
  const ReservationsScreen({super.key});

  String _fmt(DateTime dt) {
    final d = '${dt.year.toString().padLeft(4,'0')}-${dt.month.toString().padLeft(2,'0')}-${dt.day.toString().padLeft(2,'0')}';
    final t = '${dt.hour.toString().padLeft(2,'0')}:${dt.minute.toString().padLeft(2,'0')}';
    return '$d  $t';
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Rezervasyonlarım')),
      body: ValueListenableBuilder<List<Reservation>>(
        valueListenable: AppState.reservations,
        builder: (_, list, __) {
          if (list.isEmpty) return const Center(child: Text('Henüz rezervasyon yok.'));
          final items = [...list]..sort((a,b)=> a.dateTime.compareTo(b.dateTime));
          return ListView.separated(
            padding: const EdgeInsets.all(12),
            itemCount: items.length,
            separatorBuilder: (_, __) => const Divider(height: 1),
            itemBuilder: (_, i) {
              final r = items[i];
              return ListTile(
                leading: const Icon(Icons.event_seat),
                title: Text('${r.firstName} ${r.lastName}  –  ${r.tableId}'),
                subtitle: Text('${r.salonName}  •  ${_fmt(r.dateTime)}  •  ${r.partySize} kişi'),
              );
            },
          );
        },
      ),
    );
  }
}
