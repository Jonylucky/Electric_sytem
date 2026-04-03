import 'package:flutter/material.dart';
import 'package:mobile_scanner/mobile_scanner.dart';
import 'package:electric_index/screens/reading/meter_reading_hub_screen.dart';

class ScanQrScreen extends StatefulWidget {
  const ScanQrScreen({super.key});

  @override
  State<ScanQrScreen> createState() => _ScanQrScreenState();
}

class _ScanQrScreenState extends State<ScanQrScreen> {
  final MobileScannerController _controller = MobileScannerController();

  bool _handled = false; // chống scan 1 QR nhiều lần

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  String? extractMeterId(String raw) {
    final s = raw.trim();
    if (s.isEmpty) return null;

    // Case 1: CID=7;MID=MTR_001
    final midMatch = RegExp(r'MID=([^;]+)').firstMatch(s);
    if (midMatch != null) {
      return midMatch.group(1);
    }

    // Case 2: EI:MTR_001
    if (s.startsWith('EI:')) {
      return s.substring(3).trim();
    }

    // Case 3: raw meter_id
    return s;
  }

  Future<void> _onDetect(BarcodeCapture capture) async {
    if (_handled) return;

    final raw = capture.barcodes.first.rawValue;
    if (raw == null || raw.trim().isEmpty) return;

    final meterId = extractMeterId(raw);
    if (meterId == null || meterId.isEmpty) {
      ScaffoldMessenger.of(
        context,
      ).showSnackBar(SnackBar(content: Text('QR không hợp lệ: $raw')));
      return;
    }

    _handled = true;
    await _controller.stop();

    if (!mounted) return;
    await Navigator.push(
      context,
      MaterialPageRoute(
        builder: (_) => MeterReadingHubScreen(meterId: meterId),
      ),
    );

    // khi quay lại màn scan → bật lại camera để scan tiếp
    _handled = false;
    await _controller.start();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Scan QR')),
      body: MobileScanner(controller: _controller, onDetect: _onDetect),
    );
  }
}
