import 'package:flutter/material.dart';
import 'package:electric_index/db/dao/locations_dao.dart';

class LocationFormScreen extends StatefulWidget {
  final int? locationId;
  final String? initialName;
  final int? initialFloor;

  const LocationFormScreen({
    super.key,
    this.locationId,
    this.initialName,
    this.initialFloor,
  });

  @override
  State<LocationFormScreen> createState() => _LocationFormScreenState();
}

class _LocationFormScreenState extends State<LocationFormScreen> {
  final _nameCtrl = TextEditingController();
  final _floorCtrl = TextEditingController();
  final _dao = LocationsDao();

  bool _saving = false;

  bool get isEdit => widget.locationId != null;

  @override
  void initState() {
    super.initState();
    _nameCtrl.text = widget.initialName ?? '';
    _floorCtrl.text = (widget.initialFloor ?? 0).toString();
  }

  @override
  void dispose() {
    _nameCtrl.dispose();
    _floorCtrl.dispose();
    super.dispose();
  }

  Future<void> _save() async {
    final name = _nameCtrl.text.trim();
    final floor = int.tryParse(_floorCtrl.text.trim());

    if (name.isEmpty || floor == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Nhập name và floor hợp lệ nha')),
      );
      return;
    }

    setState(() => _saving = true);
    try {
      if (isEdit) {
        await _dao.updateLocation(
          locationId: widget.locationId!,
          locationName: name,
          floor: floor,
        );
      } else {
        await _dao.createLocation(locationName: name, floor: floor);
      }

      if (!mounted) return;
      Navigator.pop(context, true);
    } catch (e) {
      if (!mounted) return;
      ScaffoldMessenger.of(
        context,
      ).showSnackBar(SnackBar(content: Text('❌ Save failed: $e')));
      setState(() => _saving = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFFE3F2FD),

      appBar: AppBar(
        title: Text(isEdit ? 'Edit Location' : 'Create Location'),
        backgroundColor: const Color(0xFF1565C0),
        foregroundColor: Colors.white,
        elevation: 0,
      ),

      body: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          children: [
            Card(
              elevation: 3,
              shadowColor: Colors.blue.withOpacity(0.15),
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(18),
              ),
              child: Padding(
                padding: const EdgeInsets.all(14),
                child: Column(
                  children: [
                    TextField(
                      controller: _nameCtrl,
                      textInputAction: TextInputAction.next,
                      decoration: InputDecoration(
                        labelText: 'Location name',
                        prefixIcon: const Icon(
                          Icons.place,
                          color: Color(0xFF1565C0),
                        ),
                        filled: true,
                        fillColor: Colors.white,
                        border: OutlineInputBorder(
                          borderRadius: BorderRadius.circular(14),
                          borderSide: BorderSide.none,
                        ),
                      ),
                    ),
                    const SizedBox(height: 12),
                    TextField(
                      controller: _floorCtrl,
                      keyboardType: TextInputType.number,
                      textInputAction: TextInputAction.done,
                      decoration: InputDecoration(
                        labelText: 'Floor (e.g. -1,0,1,2...)',
                        prefixIcon: const Icon(
                          Icons.layers,
                          color: Color(0xFF1565C0),
                        ),
                        filled: true,
                        fillColor: Colors.white,
                        border: OutlineInputBorder(
                          borderRadius: BorderRadius.circular(14),
                          borderSide: BorderSide.none,
                        ),
                      ),
                    ),
                  ],
                ),
              ),
            ),

            const Spacer(),

            SizedBox(
              width: double.infinity,
              height: 56,
              child: FilledButton(
                style: FilledButton.styleFrom(
                  backgroundColor: const Color(0xFF1565C0),
                  foregroundColor: Colors.white,
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(16),
                  ),
                  textStyle: const TextStyle(
                    fontSize: 17,
                    fontWeight: FontWeight.bold,
                  ),
                ),
                onPressed: _saving ? null : _save,
                child: _saving
                    ? const SizedBox(
                        width: 22,
                        height: 22,
                        child: CircularProgressIndicator(
                          strokeWidth: 2,
                          color: Colors.white,
                        ),
                      )
                    : const Row(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          Icon(Icons.check_circle_outline),
                          SizedBox(width: 8),
                          Text('Save'),
                        ],
                      ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
