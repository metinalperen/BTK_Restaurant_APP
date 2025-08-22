import 'dart:async';
import 'package:flutter/material.dart';
import '../../data/app_state.dart';
import '../../models/reservation.dart';
import '../../services/dining_table_service.dart';
import 'reservation_form.dart';

enum Mode { rezervasyon, masa, salon }

// Görsellerdeki tam renkler
const _rezColor = Color(0xFF43A047);      // yeşil - Rezervasyon Yap
const _masaColor = Color(0xFF1E88E5);    // mavi - Masa Düzeni  
const _salColor = Color(0xFF5E35B1);     // mor - Kat Düzeni
const _tableGreen = Color(0xFF4CAF50);   // masa kartları yeşili
const _lightLavender = Color(0xFFF3E5F5); // arka plan açık lavanta
const _statusGreen = Color(0xFF4CAF50);  // Boş Masa yeşili
const _statusRed = Color(0xFFF44336);    // Dolu Masa kırmızısı
const _statusYellow = Color(0xFFFF9800); // Rezerve sarısı
const _statusBlue = Color(0xFF2196F3);   // Toplam Masa mavisi

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});
  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  Mode mode = Mode.rezervasyon;
  int activeSalon = 0;
  Timer? _ticker;

  @override
  void initState() {
    super.initState();
    _ticker = Timer.periodic(const Duration(minutes: 1), (_) {
      if (mounted) setState(() {});
    });
  }

  @override
  void dispose() {
    _ticker?.cancel();
    super.dispose();
  }

  bool _isOccupied(String tableId, DateTime now) {
    for (final r in AppState.reservations.value) {
      if (r.tableId == tableId &&
          now.isAfter(r.dateTime) &&
          now.isBefore(r.dateTime.add(Duration(hours: AppState.sessionHours)))) {
        return true;
      }
    }
    return false;
  }

  Reservation? _nearestUpcoming(String tableId, DateTime now) =>
      AppState.nearestUpcomingForTable(tableId, now);

  bool _isYellow(String tableId, DateTime now) =>
      AppState.isYellowForTable(tableId, now);

  Color _tileColor(String tableId, DateTime now) {
    if (_isOccupied(tableId, now)) return _statusRed; // dolu
    if (_isYellow(tableId, now)) return _statusYellow;     // 59 dk kala
    return _tableGreen;                                   // boş / rezerve ileri tarih
  }

  Future<void> _addSalonDialog() async {
    final ctrl = TextEditingController();
    final res = await showDialog<String>(
      context: context,
      builder: (_) => AlertDialog(
        title: const Text('Salon Ekle'),
        content: TextField(
          controller: ctrl,
          decoration: const InputDecoration(hintText: 'Salon adı (örn: Teras)'),
          textInputAction: TextInputAction.done,
          onSubmitted: (_) => Navigator.pop(context, ctrl.text.trim()),
        ),
        actions: [
          TextButton(onPressed: () => Navigator.pop(context), child: const Text('İptal')),
          FilledButton(onPressed: () => Navigator.pop(context, ctrl.text.trim()), child: const Text('Ekle')),
        ],
      ),
    );
    final name = (res ?? '').trim();
    if (name.isNotEmpty) {
      AppState.addSalon(name);
      setState(() => activeSalon = AppState.salons.value.length - 1);
    }
  }

  Future<void> _addTableDialog() async {
    final capacityCtrl = TextEditingController();
    final tableNumberCtrl = TextEditingController();
    
    final res = await showDialog<Map<String, dynamic>>(
      context: context,
      builder: (_) => AlertDialog(
        title: const Text('Masa Ekle'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            TextField(
              controller: tableNumberCtrl,
              decoration: const InputDecoration(
                labelText: 'Masa Numarası',
                hintText: 'Masa numarasını girin',
              ),
              keyboardType: TextInputType.number,
            ),
            const SizedBox(height: 16),
            TextField(
              controller: capacityCtrl,
              decoration: const InputDecoration(
                labelText: 'Kapasite',
                hintText: 'Masa kapasitesi (örn: 4)',
              ),
              keyboardType: TextInputType.number,
            ),
          ],
        ),
        actions: [
          TextButton(onPressed: () => Navigator.pop(context), child: const Text('İptal')),
          FilledButton(
            onPressed: () {
              final tableNumber = int.tryParse(tableNumberCtrl.text.trim());
              final capacity = int.tryParse(capacityCtrl.text.trim());
              
              if (tableNumber != null && capacity != null) {
                Navigator.pop(context, {
                  'tableNumber': tableNumber,
                  'capacity': capacity,
                });
              }
            }, 
            child: const Text('Ekle')
          ),
        ],
      ),
    );
    
    if (res != null) {
      try {
        // Backend'e masa ekle
        final tableData = {
          'tableNumber': res['tableNumber'],
          'capacity': res['capacity'],
          'statusId': 1, // 1 = Boş
          'salonId': activeSalon + 1, // Backend'de salon ID'leri 1'den başlıyor
        };
        
        final newTable = await DiningTableService.createTable(tableData);
        
        // Başarılı mesajı göster
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(
              content: Text('Masa ${res['tableNumber']} başarıyla eklendi!'),
              backgroundColor: Colors.green,
            ),
          );
        }
        
        // Local state'i güncelle
        AppState.addTable(activeSalon, id: 'M${res['tableNumber']}', capacity: res['capacity']);
        setState(() {});
        
      } catch (e) {
        // Hata mesajı göster
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(
              content: Text('Masa eklenirken hata oluştu: $e'),
              backgroundColor: Colors.red,
            ),
          );
        }
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    final now = DateTime.now();

    return Scaffold(
      backgroundColor: _lightLavender,
      appBar: AppBar(
        backgroundColor: Colors.white,
        elevation: 0,
        leading: IconButton(
          icon: const Icon(Icons.arrow_back, color: Colors.black),
          onPressed: () => Navigator.pop(context),
        ),
        title: const Text(
          'Ana Sayfa',
          style: TextStyle(
            color: Colors.black,
            fontSize: 24,
            fontWeight: FontWeight.bold,
          ),
        ),
        centerTitle: true,
      ),
      body: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
                         // Üst butonlar - görsellerdeki tasarım
             Row(
               children: [
                 _modeChip('Rezervasyon', Mode.rezervasyon, _rezColor),
                 const SizedBox(width: 8),
                 _modeChip('Masa Düzeni', Mode.masa, _masaColor),
                 const SizedBox(width: 8),
                 _modeChip('Salon', Mode.salon, _salColor),
               ],
             ),
            const SizedBox(height: 16),

            // Sayaçlar - görsellerdeki tasarım
            ValueListenableBuilder<List<Salon>>(
              valueListenable: AppState.salons,
              builder: (_, salons, __) {
                final total = AppState.totalTables(activeSalon);
                final occ = AppState.occupiedNow(activeSalon, now);
                final res = AppState.reservedUpcoming(activeSalon, now);
                final free = AppState.freeNow(activeSalon, now);
                
                return Wrap(
                  spacing: 8,
                  runSpacing: 8,
                  children: [
                    _statusBox('Boş: $free', _statusGreen),
                    _statusBox('Dolu: $occ', _statusRed),
                    _statusBox('Rezerve: $res', _statusYellow),
                    _statusBox('Toplam: $total', _statusBlue),
                  ],
                );
              },
            ),
            const SizedBox(height: 16),

            // Salon sekmeleri - görsellerdeki tasarım
            ValueListenableBuilder<List<Salon>>(
              valueListenable: AppState.salons,
              builder: (_, salons, __) => SingleChildScrollView(
                scrollDirection: Axis.horizontal,
                child: Row(
                  children: [
                    for (int i = 0; i < salons.length; i++) ...[
                      _floorChip(salons[i].name, activeSalon == i, () {
                        setState(() => activeSalon = i);
                      }),
                      const SizedBox(width: 8),
                    ],
                  ],
                ),
              ),
            ),
            const SizedBox(height: 16),

            // MASA GRID - görsellerdeki tasarım
            Expanded(
              child: ValueListenableBuilder<List<Salon>>(
                valueListenable: AppState.salons,
                builder: (_, salons, __) {
                  final tables = AppState.tablesOf(activeSalon);
                  final salonName = salons[activeSalon].name;

                  return Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                                             // Salon başlığı ve butonlar
                       Column(
                         crossAxisAlignment: CrossAxisAlignment.start,
                         children: [
                           Text(
                             '$salonName - Masa Seçimi',
                             style: const TextStyle(
                               fontSize: 20,
                               fontWeight: FontWeight.bold,
                               color: Colors.black,
                             ),
                           ),
                           const SizedBox(height: 8),
                           Wrap(
                             spacing: 8,
                             children: [
                               if (mode == Mode.salon)
                                 ElevatedButton.icon(
                                   onPressed: _addSalonDialog,
                                   icon: const Icon(Icons.storefront),
                                   label: const Text('Salon Ekle'),
                                   style: ElevatedButton.styleFrom(
                                     backgroundColor: _salColor,
                                     foregroundColor: Colors.white,
                                     shape: RoundedRectangleBorder(
                                       borderRadius: BorderRadius.circular(8),
                                     ),
                                   ),
                                 ),
                                                          if (mode == Mode.masa) ...[
                             ElevatedButton.icon(
                               onPressed: _addTableDialog,
                               icon: const Icon(Icons.add),
                               label: const Text('Masa Ekle'),
                               style: ElevatedButton.styleFrom(
                                 backgroundColor: _masaColor,
                                 foregroundColor: Colors.white,
                                 shape: RoundedRectangleBorder(
                                   borderRadius: BorderRadius.circular(8),
                                 ),
                               ),
                             ),
                             const SizedBox(width: 8),
                             Container(
                               padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
                               decoration: BoxDecoration(
                                 color: Colors.blue.shade50,
                                 borderRadius: BorderRadius.circular(16),
                                 border: Border.all(color: Colors.blue.shade200),
                               ),
                               child: const Text(
                                 '💡 Çift tık: Düzenle, Uzun bas: Sil',
                                 style: TextStyle(
                                   fontSize: 12,
                                   color: Colors.blue,
                                   fontWeight: FontWeight.w500,
                                 ),
                               ),
                             ),
                           ],
                             ],
                           ),
                         ],
                       ),
                      const SizedBox(height: 16),
                      
                                             // Masa grid'i
                       Expanded(
                         child: GridView.builder(
                           gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
                             crossAxisCount: 3,
                             crossAxisSpacing: 12,
                             mainAxisSpacing: 12,
                             childAspectRatio: 0.8, // Overflow'u önlemek için
                           ),
                           itemCount: tables.length,
                           itemBuilder: (_, i) {
                            final t = tables[i];
                            final color = _tileColor(t.id, now);
                            final next = _nearestUpcoming(t.id, now);

                            return GestureDetector(
                              onTap: () async {
                                if (mode == Mode.rezervasyon) {
                                  final r = await Navigator.push<Reservation>(
                                    context,
                                    MaterialPageRoute(
                                      builder: (_) => ReservationForm(
                                        salonName: salonName,
                                        tableId: t.id,
                                        capacity: t.capacity,
                                      ),
                                    ),
                                  );
                                  if (r != null) {
                                    AppState.addReservation(r);
                                    setState(() {}); // kartı güncelle
                                  }
                                }
                              },
                              onLongPress: () async {
                                if (mode == Mode.masa) {
                                  // Masa silme onayı
                                  final confirm = await showDialog<bool>(
                                    context: context,
                                    builder: (_) => AlertDialog(
                                      title: const Text('Masa Sil'),
                                      content: Text('${t.id} masasını silmek istediğinizden emin misiniz?'),
                                      actions: [
                                        TextButton(
                                          onPressed: () => Navigator.pop(context, false),
                                          child: const Text('İptal'),
                                        ),
                                        FilledButton(
                                          onPressed: () => Navigator.pop(context, true),
                                          style: FilledButton.styleFrom(backgroundColor: Colors.red),
                                          child: const Text('Sil'),
                                        ),
                                      ],
                                    ),
                                  );
                                  
                                  if (confirm == true) {
                                    try {
                                      // Backend'den masa sil
                                      final tableNumber = int.tryParse(t.id.replaceAll('M', ''));
                                      if (tableNumber != null) {
                                        await DiningTableService.deleteTable(tableNumber);
                                        
                                        // Local state'den masa sil
                                        AppState.removeTable(activeSalon, t.id);
                                        setState(() {});
                                        
                                        if (mounted) {
                                          ScaffoldMessenger.of(context).showSnackBar(
                                            SnackBar(
                                              content: Text('${t.id} masası silindi!'),
                                              backgroundColor: Colors.green,
                                            ),
                                          );
                                        }
                                      }
                                    } catch (e) {
                                      if (mounted) {
                                        ScaffoldMessenger.of(context).showSnackBar(
                                          SnackBar(
                                            content: Text('Masa silinirken hata oluştu: $e'),
                                            backgroundColor: Colors.red,
                                          ),
                                        );
                                      }
                                    }
                                  }
                                }
                              },
                              onDoubleTap: () async {
                                if (mode == Mode.masa) {
                                  // Masa düzenleme dialog'u
                                  final capacityCtrl = TextEditingController(text: t.capacity.toString());
                                  
                                  final res = await showDialog<Map<String, dynamic>>(
                                    context: context,
                                    builder: (_) => AlertDialog(
                                      title: Text('${t.id} Düzenle'),
                                      content: Column(
                                        mainAxisSize: MainAxisSize.min,
                                        children: [
                                          TextField(
                                            controller: capacityCtrl,
                                            decoration: const InputDecoration(
                                              labelText: 'Kapasite',
                                              hintText: 'Masa kapasitesi',
                                            ),
                                            keyboardType: TextInputType.number,
                                          ),
                                        ],
                                      ),
                                      actions: [
                                        TextButton(
                                          onPressed: () => Navigator.pop(context),
                                          child: const Text('İptal'),
                                        ),
                                        FilledButton(
                                          onPressed: () {
                                            final capacity = int.tryParse(capacityCtrl.text.trim());
                                            if (capacity != null) {
                                              Navigator.pop(context, {
                                                'capacity': capacity,
                                              });
                                            }
                                          },
                                          child: const Text('Güncelle'),
                                        ),
                                      ],
                                    ),
                                  );
                                  
                                  if (res != null) {
                                    try {
                                      // Backend'de masa güncelle
                                      final tableNumber = int.tryParse(t.id.replaceAll('M', ''));
                                      if (tableNumber != null) {
                                        final tableData = {
                                          'tableNumber': tableNumber,
                                          'capacity': res['capacity'],
                                          'statusId': 1, // 1 = Boş
                                          'salonId': activeSalon + 1,
                                        };
                                        
                                        await DiningTableService.updateTable(tableNumber, tableData);
                                        
                                        // Local state'i güncelle
                                        AppState.updateTable(activeSalon, t.id, capacity: res['capacity']);
                                        setState(() {});
                                        
                                        if (mounted) {
                                          ScaffoldMessenger.of(context).showSnackBar(
                                            SnackBar(
                                              content: Text('${t.id} masası güncellendi!'),
                                              backgroundColor: Colors.green,
                                            ),
                                          );
                                        }
                                      }
                                    } catch (e) {
                                      if (mounted) {
                                        ScaffoldMessenger.of(context).showSnackBar(
                                          SnackBar(
                                            content: Text('Masa güncellenirken hata oluştu: $e'),
                                            backgroundColor: Colors.red,
                                          ),
                                        );
                                      }
                                    }
                                  }
                                }
                              },
                              child: Container(
                              decoration: BoxDecoration(
                                color: color,
                                borderRadius: BorderRadius.circular(16),
                              ),
                                                              padding: const EdgeInsets.all(6),
                              child: Center(
                                child: Column(
                                  mainAxisSize: MainAxisSize.min,
                                  children: [
                                                                           Text(
                                         '${t.capacity} Kişilik',
                                         style: const TextStyle(
                                           color: Colors.white,
                                           fontWeight: FontWeight.w600,
                                           fontSize: 12,
                                         ),
                                       ),
                                       const SizedBox(height: 4),
                                       Text(
                                         t.id,
                                         style: const TextStyle(
                                           color: Colors.white,
                                           fontSize: 22,
                                           fontWeight: FontWeight.bold,
                                         ),
                                       ),
                                       const SizedBox(height: 4),
                                       Text(
                                         _isOccupied(t.id, now)
                                             ? 'Dolu'
                                             : (next != null ? 'Rezerve' : 'Boş'),
                                         style: const TextStyle(
                                           color: Colors.white,
                                           fontWeight: FontWeight.w700,
                                           fontSize: 14,
                                         ),
                                       ),
                                                                          if (next != null)
                                        Padding(
                                          padding: const EdgeInsets.only(top: 2),
                                          child: Text(
                                            '${next.firstName} ${next.lastName} - ${next.dateTime.hour.toString().padLeft(2, '0')}:${next.dateTime.minute.toString().padLeft(2, '0')}',
                                            style: const TextStyle(
                                              color: Colors.white,
                                              fontSize: 10,
                                            ),
                                            textAlign: TextAlign.center,
                                          ),
                                        ),
                                  ],
                                ),
                              ),
                            ),
                          );
                          },
                        ),
                      ),
                    ],
                  );
                },
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _modeChip(String text, Mode m, Color activeColor) {
    final active = mode == m;
    return GestureDetector(
      onTap: () => setState(() => mode = m),
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 10),
        decoration: BoxDecoration(
          color: active ? activeColor : Colors.grey.shade300,
          borderRadius: BorderRadius.circular(20),
        ),
        child: Text(
          text,
          style: TextStyle(
            color: active ? Colors.white : Colors.black,
            fontWeight: FontWeight.w700,
            fontSize: 16,
          ),
        ),
      ),
    );
  }

  Widget _statusBox(String text, Color color) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(8),
        border: Border.all(color: color.withOpacity(0.3)),
      ),
      child: Text(
        text,
        style: TextStyle(
          color: color,
          fontWeight: FontWeight.w600,
          fontSize: 14,
        ),
      ),
    );
  }

  Widget _floorChip(String name, bool isSelected, VoidCallback onTap) {
    return GestureDetector(
      onTap: onTap,
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 10),
        decoration: BoxDecoration(
          color: isSelected ? _salColor : Colors.white,
          borderRadius: BorderRadius.circular(8),
          border: Border.all(
            color: isSelected ? _salColor : Colors.grey.shade300,
          ),
        ),
        child: Row(
          mainAxisSize: MainAxisSize.min,
          children: [
            if (isSelected)
              const Icon(
                Icons.check,
                color: Colors.white,
                size: 20,
              ),
            if (isSelected) const SizedBox(width: 8),
            Text(
              name,
              style: TextStyle(
                color: isSelected ? Colors.white : Colors.black,
                fontWeight: FontWeight.w600,
                fontSize: 14,
              ),
            ),
          ],
        ),
      ),
    );
  }
}
