import 'package:flutter/material.dart';
import '../../models/reservation.dart';
import '../../utils/validators.dart';
import '../../data/app_state.dart';

class ReservationForm extends StatefulWidget {
  const ReservationForm({
    super.key,
    required this.salonName,
    required this.tableId,
    required this.capacity,
  });

  final String salonName;
  final String tableId;
  final int capacity;

  @override
  State<ReservationForm> createState() => _ReservationFormState();
}

class _ReservationFormState extends State<ReservationForm> {
  final _formKey = GlobalKey<FormState>();

  final _adCtrl = TextEditingController();
  final _soyadCtrl = TextEditingController();
  final _telCtrl = TextEditingController();
  final _emailCtrl = TextEditingController();
  final _noteCtrl = TextEditingController();
  DateTime _date = DateTime.now();
  TimeOfDay? _time;
  int? _party;

  @override
  void dispose() {
    _adCtrl.dispose();
    _soyadCtrl.dispose();
    _telCtrl.dispose();
    _emailCtrl.dispose();
    _noteCtrl.dispose();
    super.dispose();
  }

  Future<void> _pickDate() async {
    final res = await showDatePicker(
      context: context,
      firstDate: DateTime.now(),
      lastDate: DateTime.now().add(const Duration(days: 365)),
      initialDate: _date,
    );
    if (res != null) setState(() => _date = res);
  }

  Future<void> _pickTime() async {
    final res = await showTimePicker(
      context: context,
      initialTime: TimeOfDay(hour: AppState.openHour, minute: 0),
      helpText: 'Çalışma Saatleri: 09:00 – 22:00',
    );
    if (res != null) setState(() => _time = res);
  }

  void _error(String msg) =>
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(msg)));

  void _save() {
    if (!_formKey.currentState!.validate()) return;
    if (_time == null || _party == null) {
      _error('Saat ve kişi sayısı seçilmelidir.');
      return;
    }

    final now = DateTime.now();
    final dt = DateTime(_date.year, _date.month, _date.day, _time!.hour, _time!.minute);

    if (dt.isBefore(now)) {
      _error('Geçmiş bir zaman seçilemez.');
      return;
    }

    if (!AppState.withinOpenHours(dt)) {
      _error('Rezervasyon saatleri 09:00–22:00 arasındadır.');
      return;
    }

    final clashEnd = AppState.nextAllowedTime(widget.tableId, dt);
    if (clashEnd != null) {
      final hh = clashEnd.hour.toString().padLeft(2,'0');
      final mm = clashEnd.minute.toString().padLeft(2,'0');
      _error('Bu masa için en az ${AppState.minGapHours} saat ara kuralı var. En erken $hh:$mm.');
      return;
    }

    final r = Reservation(
      id: DateTime.now().millisecondsSinceEpoch.toString(),
      salonName: widget.salonName,
      tableId: widget.tableId,
      firstName: _adCtrl.text.trim(),
      lastName: _soyadCtrl.text.trim(),
      phone: _telCtrl.text.trim(),
      dateTime: dt,
      partySize: _party!,
      email: _emailCtrl.text.trim().isEmpty ? null : _emailCtrl.text.trim(),
      note: _noteCtrl.text.trim().isEmpty ? null : _noteCtrl.text.trim(),
    );

    Navigator.pop(context, r); // HomeScreen'e Reservation döndür
  }

  @override
  Widget build(BuildContext context) {
    final max = widget.capacity;

    return Scaffold(
      appBar: AppBar(title: Text('Masa ${widget.tableId} – Yeni Rezervasyon')),
      body: Form(
        key: _formKey,
        child: ListView(
          padding: const EdgeInsets.all(16),
          children: [
            Row(
              children: [
                Expanded(
                  child: TextFormField(
                    controller: _adCtrl,
                    inputFormatters: [Validators.adSoyadFormatter()],
                    validator: Validators.adSoyadValidator,
                    decoration: const InputDecoration(labelText: 'Ad'),
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: TextFormField(
                    controller: _soyadCtrl,
                    inputFormatters: [Validators.adSoyadFormatter()],
                    validator: Validators.adSoyadValidator,
                    decoration: const InputDecoration(labelText: 'Soyad'),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 12),
            TextFormField(
              controller: _telCtrl,
              keyboardType: TextInputType.phone,
              decoration: const InputDecoration(labelText: 'Telefon'),
              validator: (v) => (v==null || v.trim().isEmpty) ? 'Telefon zorunlu' : null,
            ),
            const SizedBox(height: 12),
            TextFormField(
              controller: _emailCtrl,
              keyboardType: TextInputType.emailAddress,
              decoration: const InputDecoration(labelText: 'E-mail (isteğe bağlı)'),
            ),
            const SizedBox(height: 12),
            ListTile(
              contentPadding: EdgeInsets.zero,
              title: const Text('Tarih'),
              subtitle: Text('${_date.day.toString().padLeft(2,'0')}.${_date.month.toString().padLeft(2,'0')}.${_date.year}'),
              trailing: ElevatedButton.icon(
                onPressed: _pickDate,
                icon: const Icon(Icons.calendar_month),
                label: const Text('Seç'),
              ),
            ),
            ListTile(
              contentPadding: EdgeInsets.zero,
              title: const Text('Saat'),
              subtitle: Text(_time==null ? '--:--' : '${_time!.hour.toString().padLeft(2,'0')}:${_time!.minute.toString().padLeft(2,'0')}'),
              trailing: ElevatedButton.icon(
                onPressed: _pickTime,
                icon: const Icon(Icons.access_time),
                label: const Text('Seç'),
              ),
            ),
            DropdownButtonFormField<int>(
              value: _party,
              decoration: const InputDecoration(labelText: 'Kişi Sayısı'),
              items: [for (int i=1; i<=max; i++) DropdownMenuItem(value: i, child: Text('$i'))],
              onChanged: (v)=> setState(()=> _party = v),
              validator: (v)=> v==null ? 'Kişi sayısı seçiniz' : null,
            ),
            const SizedBox(height: 8),
            Text('Bu masa $max kişilik. Maksimum $max kişi seçebilirsiniz.',
              style: Theme.of(context).textTheme.bodySmall),
            const SizedBox(height: 12),
            TextFormField(
              controller: _noteCtrl,
              maxLines: 3,
              decoration: const InputDecoration(labelText: 'Not (isteğe bağlı)'),
            ),
            const SizedBox(height: 16),
            ElevatedButton(
              onPressed: _save,
              child: const Padding(
                padding: EdgeInsets.symmetric(vertical: 14),
                child: Text('Rezervasyon Oluştur'),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
