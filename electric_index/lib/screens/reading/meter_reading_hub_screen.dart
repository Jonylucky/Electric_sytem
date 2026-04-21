import 'dart:io';
import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';
import 'package:path_provider/path_provider.dart';
import 'package:path/path.dart' as p;
import 'package:electric_index/db/dao/reading_dao.dart';
import 'package:electric_index/services/photo_service.dart';
import 'package:electric_index/db/dao/database_helper.dart';
import 'package:electric_index/db/dao/meter_dao.dart';
import 'package:electric_index/services/qr_excel_exporter.dart';
import 'package:share_plus/share_plus.dart';
import 'package:electric_index/db/dao/meter_reset_dao.dart';
import 'package:electric_index/screens/meter/reset_screen.dart';
import 'package:electric_index/screens/meter/meter_closing_screen.dart';

class MeterReadingHubScreen extends StatefulWidget {
  final String meterId;
  const MeterReadingHubScreen({super.key, required this.meterId});

  @override
  State<MeterReadingHubScreen> createState() => _MeterReadingHubScreenState();
}

class _MeterReadingHubScreenState extends State<MeterReadingHubScreen> {
  final _dao = ReadingDao();
  final MeterResetReadingService _meterResetReadingService =
      MeterResetReadingService();
  final _picker = ImagePicker();
  final _meterDao = MeterDao();
  bool _loading = true;
  Map<String, dynamic>? _latest; // bản ghi gần nhất
  int _prevAuto = 0;
  final TextEditingController _lastCtrl = TextEditingController();
  final FocusNode _lastFocus = FocusNode();

  // int get _prevAuto =>
  //     _latest == null ? 0 : (_latest!['index_last_month'] as int);

  @override
  void initState() {
    super.initState();
    _load();
  }

  String _billingMonthFromDate(DateTime d, {int cutoffDay = 20}) {
    // nếu ngày < cutoff → vẫn thuộc tháng trước
    final ref = (d.day < cutoffDay)
        ? DateTime(d.year, d.month - 1, 1)
        : DateTime(d.year, d.month, 1);

    final mm = ref.month.toString().padLeft(2, '0');
    return '${ref.year}-$mm';
  }

  // String _billingMonthFromDate(DateTime d) {
  //   // flex rule: 23..31 = tháng hiện tại, 1..22 = tháng trước
  //   final ref = (d.day >= 23) ? d : DateTime(d.year, d.month - 1, 1);
  //   final mm = ref.month.toString().padLeft(2, '0');
  //   return '${ref.year}-$mm';
  // }

  @override
  void dispose() {
    _lastCtrl.dispose();
    _lastFocus.dispose();
    super.dispose();
  }

  Future<void> _load() async {
    final month = _billingMonthFromDate(DateTime.now());

    // 1️⃣ Lấy baseline tháng trước
    final prevBaseline = await _dao.getBaselinePrevFromPrevMonth(
      meterId: widget.meterId,
      currentMonth: month,
    );
    print('prevBaseline : ${prevBaseline}');
    final latest = await _dao.getLatestReadingWithCompany(widget.meterId);
    if (!mounted) return;
    setState(() {
      _prevAuto = prevBaseline;
      _latest = latest;
      _loading = false;
    });
  }

  // ✅ HÀM EXPORT + SHARE (đánh dấu NEW bằng highlightMeterId)
  Future<void> _exportQrExcelForCurrentCompanyAndShare({
    required int companyId,
    required String companyName,
    String? highlightMeterId,
  }) async {
    try {
      final exporter = QrExcelExporter(meterDao: _meterDao);

      final filePath = await exporter.exportMetersQrExcel(
        companyId: companyId,
        companyName: companyName,
        highlightMeterId: highlightMeterId, // ✅ NEW row
      );

      if (!mounted) return;

      await Share.shareXFiles(
        [XFile(filePath)],
        text: 'QR meters – $companyName',
        subject: 'QR Excel – $companyName',
      );
    } catch (e) {
      if (!mounted) return;
      ScaffoldMessenger.of(
        context,
      ).showSnackBar(SnackBar(content: Text('Export/Share lỗi: $e')));
    }
  }

  Future<void> _openResetSheet() async {
    final ok = await Navigator.of(context).push<bool>(
      MaterialPageRoute(
        builder: (_) => ResetScreen(
          meterId: widget.meterId,
          initialLastBeforeReset:
              '${_latest?['index_last_month'] ?? _prevAuto}',
          service: _meterResetReadingService,
        ),
      ),
    );

    if (!mounted) return;

    if (ok == true) {
      await _load();
      if (!mounted) return;

      ScaffoldMessenger.of(
        context,
      ).showSnackBar(const SnackBar(content: Text('Reset đồng hồ thành công')));
    }
  }

  Future<void> _openClosingPage() async {
    final ok = await Navigator.of(context).push<bool>(
      MaterialPageRoute(
        builder: (_) => MeterClosingScreen(meterId: widget.meterId),
      ),
    );

    if (!mounted) return;

    if (ok == true) {
      await _load(); // reload lại prevAuto + latest
      if (!mounted) return;

      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Chốt số bàn giao thành công')),
      );
    }
  }

  Future<String> _saveWatermarkedAsOfficialFile(File wmFile) async {
    final dir = await getApplicationDocumentsDirectory();
    final imagesDir = Directory(p.join(dir.path, 'readings'));
    if (!await imagesDir.exists()) await imagesDir.create(recursive: true);

    final fileName =
        'image_reading_${widget.meterId}_${DateTime.now().millisecondsSinceEpoch}.png';

    final newPath = p.join(imagesDir.path, fileName);

    await wmFile.copy(newPath);

    return newPath;
  }

  Future<void> _takePhotoAndInput() async {
    final xfile = await _picker.pickImage(
      source: ImageSource.camera,
      imageQuality: 80,
    );
    if (xfile == null) return;

    if (!mounted) return;

    setState(() => _loading = true);
    final month = _billingMonthFromDate(DateTime.now());
    final db = await DatabaseHelper.instance.database;

    try {
      // 1️⃣ Tạo watermark trực tiếp từ ảnh camera
      final wmTmp = await PhotoService().addWatermark(
        originalImage: File(xfile.path),
      );

      // 2️⃣ Lưu watermark vào readings/ với tên chuẩn
      final finalPath = await _saveWatermarkedAsOfficialFile(wmTmp);

      if (!mounted) return;
      setState(() => _loading = false);

      // 3️⃣ Mở modal bằng ảnh đã watermark
      await _showInputModal(imagePath: finalPath);
    } catch (e) {
      if (!mounted) return;
      setState(() => _loading = false);

      ScaffoldMessenger.of(
        context,
      ).showSnackBar(SnackBar(content: Text('Watermark lỗi: $e')));
    }
  }

  // save
  Future<void> _saveFromSheet({
    required BuildContext sheetCtx, // ctx trong bottomsheet
    required String imagePath,
  }) async {
    final prev = _prevAuto; // ✅ MODE A: dùng prevAuto để validate UI
    final last = int.tryParse(_lastCtrl.text.trim());

    if (last == null) {
      if (sheetCtx.mounted) {
        ScaffoldMessenger.of(
          sheetCtx,
        ).showSnackBar(const SnackBar(content: Text('Nhập số hợp lệ nha')));
      }
      if (_lastFocus.context != null) _lastFocus.requestFocus();
      return;
    }

    if (last < prev) {
      if (sheetCtx.mounted) {
        ScaffoldMessenger.of(
          sheetCtx,
        ).showSnackBar(const SnackBar(content: Text('last phải >= prev')));
      }
      if (_lastFocus.context != null) _lastFocus.requestFocus();
      return;
    }

    // ✅ đóng keyboard trước khi pop
    FocusScope.of(sheetCtx).unfocus();

    final month = _billingMonthFromDate(DateTime.now());

    try {
      await _dao.insertReading(
        meterId: widget.meterId,
        month: month,
        indexLast: last, // ✅ Mode A: chỉ truyền last
        imageReading: imagePath,
      );
    } catch (e) {
      if (sheetCtx.mounted) {
        ScaffoldMessenger.of(
          sheetCtx,
        ).showSnackBar(SnackBar(content: Text('$e')));
      }
      return;
    }

    if (!sheetCtx.mounted) return;

    // optional: delay nhẹ để Android ổn định InputConnection
    await Future.delayed(const Duration(milliseconds: 50));

    Navigator.of(sheetCtx).pop(true); // ✅ đóng sheet, trả kết quả về màn scan
  }

  Future<void> _showInputModal({required String imagePath}) async {
    _lastCtrl.clear();
    final prev = _prevAuto;

    bool didAutoFocus = false;
    final bool? saved = await showModalBottomSheet<bool>(
      context: context,
      isScrollControlled: true,
      showDragHandle: true,
      builder: (ctx) {
        final bottom = MediaQuery.of(ctx).viewInsets.bottom;
        final screenH = MediaQuery.of(ctx).size.height;
        final imgH = (screenH * 0.33).clamp(180.0, 280.0);

        // ✅ CHỈ focus 1 lần (không spam postFrameCallback)
        if (!didAutoFocus) {
          didAutoFocus = true;
          WidgetsBinding.instance.addPostFrameCallback((_) {
            if (!ctx.mounted) return;
            if (_lastFocus.context == null) return; // chưa attach / đã detach
            _lastFocus.requestFocus();
          });
        }

        return SafeArea(
          child: Padding(
            padding: EdgeInsets.only(
              left: 16,
              right: 16,
              top: 16,
              bottom: bottom + 16,
            ),
            child: SingleChildScrollView(
              child: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  // ===== Header card =====
                  Card(
                    elevation: 3,
                    shadowColor: Colors.blue.withOpacity(0.15),
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(18),
                    ),
                    child: ListTile(
                      contentPadding: const EdgeInsets.symmetric(
                        horizontal: 16,
                        vertical: 10,
                      ),
                      leading: Container(
                        padding: const EdgeInsets.all(10),
                        decoration: BoxDecoration(
                          color: const Color(0xFFBBDEFB),
                          borderRadius: BorderRadius.circular(14),
                        ),
                        child: const Icon(
                          Icons.qr_code,
                          color: Color(0xFF1565C0),
                        ),
                      ),
                      title: Text(
                        'Meter: ${widget.meterId}',
                        maxLines: 1,
                        overflow: TextOverflow.ellipsis,
                        style: const TextStyle(
                          fontWeight: FontWeight.w700,
                          color: Color(0xFF0D47A1),
                        ),
                      ),
                      subtitle: Text(
                        'Prev auto: $prev',
                        maxLines: 1,
                        overflow: TextOverflow.ellipsis,
                        style: const TextStyle(color: Colors.black54),
                      ),
                    ),
                  ),

                  const SizedBox(height: 12),

                  // ===== Image card =====
                  Card(
                    elevation: 3,
                    shadowColor: Colors.blue.withOpacity(0.15),
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(18),
                    ),
                    clipBehavior: Clip.antiAlias,
                    child: InkWell(
                      onTap: () {
                        if (_lastFocus.context != null)
                          _lastFocus.requestFocus();
                      },
                      child: Padding(
                        padding: const EdgeInsets.all(12),
                        child: ClipRRect(
                          borderRadius: BorderRadius.circular(14),
                          child: Image.file(
                            File(imagePath),
                            height: imgH,
                            width: double.infinity,
                            fit: BoxFit.contain,
                          ),
                        ),
                      ),
                    ),
                  ),

                  const SizedBox(height: 12),

                  // ===== Input field =====
                  Card(
                    elevation: 3,
                    shadowColor: Colors.blue.withOpacity(0.15),
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(18),
                    ),
                    child: Padding(
                      padding: const EdgeInsets.all(14),
                      child: TextField(
                        controller: _lastCtrl,
                        focusNode: _lastFocus,
                        autofocus: true,
                        keyboardType: TextInputType.number,
                        textInputAction: TextInputAction.done,
                        decoration: InputDecoration(
                          labelText: 'Chỉ số hiện tại (last_month)',
                          prefixIcon: const Icon(
                            Icons.electric_meter,
                            color: Color(0xFF1565C0),
                          ),
                          filled: true,
                          fillColor: Colors.white,
                          border: OutlineInputBorder(
                            borderRadius: BorderRadius.circular(14),
                            borderSide: BorderSide.none,
                          ),
                        ),
                        onSubmitted: (_) =>
                            _saveFromSheet(sheetCtx: ctx, imagePath: imagePath),
                      ),
                    ),
                  ),

                  const SizedBox(height: 14),

                  // ===== Save button =====
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
                      onPressed: () =>
                          _saveFromSheet(sheetCtx: ctx, imagePath: imagePath),
                      child: const Row(
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
          ),
        );
      },
    );

    // ✅ chạy sau khi sheet đã đóng hẳn
    if (!mounted) return;
    if (saved == true) {
      Navigator.of(context).pop(true); // quay lại màn scan sau khi lưu xong
    }
  }

  @override
  Widget build(BuildContext context) {
    final latestMonth = _latest?['month'] as String?;
    final latestLast = _latest?['index_last_month'] as int?;
    final String? imageReading = _latest?['image_reading'] as String?;
    final String? companyName = _latest?['company_name'] as String?;

    return Scaffold(
      backgroundColor: const Color(0xFFE3F2FD),

      // ✅ APPBAR EDIT: gọi _openReplaceSheet()
      appBar: AppBar(
        title: const Text('Meter Info'),
        backgroundColor: const Color(0xFF1565C0),
        foregroundColor: Colors.white,
        elevation: 0,
        actions: [
        PopupMenuButton<String>(
  icon: const Icon(Icons.more_vert),
  onSelected: (value) {
    if (value == 'reset') {
      _openResetSheet();
    } else if (value == 'closing') {
      _openClosingPage();
    }
  },
  itemBuilder: (context) => const [
    PopupMenuItem(
      value: 'closing',
      child: Row(
        children: [
          Icon(Icons.assignment_turned_in),
          SizedBox(width: 8),
          Text('Chốt số bàn giao'),
        ],
      ),
    ),
    PopupMenuItem(
      value: 'reset',
      child: Row(
        children: [
          Icon(Icons.restart_alt),
          SizedBox(width: 8),
          Text('Reset đồng hồ'),
        ],
      ),
    ),
  ],
),
        ],
      ),

      body: _loading
          ? const Center(child: CircularProgressIndicator())
          : Padding(
              padding: const EdgeInsets.all(16),
              child: Column(
                children: [
                  // ===== CARD: Latest reading =====
                  Card(
                    elevation: 3,
                    shadowColor: Colors.blue.withOpacity(0.15),
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(18),
                    ),
                    child: Padding(
                      padding: const EdgeInsets.all(14),
                      child: Row(
                        crossAxisAlignment: CrossAxisAlignment.start, // ✅
                        children: [
                          ClipRRect(
                            borderRadius: BorderRadius.circular(14),
                            child: SizedBox(
                              width: 72,
                              height: 72,
                              child: _buildReadingImage(context, imageReading),
                            ),
                          ),
                          const SizedBox(width: 12),
                          Expanded(
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Text(
                                  companyName ?? '—',
                                  softWrap: true,
                                  maxLines: null, // ✅ full name
                                  overflow: TextOverflow.visible,
                                  style: Theme.of(context).textTheme.labelMedium
                                      ?.copyWith(
                                        color: const Color(0xFF1565C0),
                                        fontWeight: FontWeight.w700,
                                      ),
                                ),
                                const SizedBox(height: 4),
                                Text(
                                  'Bản ghi gần nhất',
                                  maxLines: 1,
                                  overflow: TextOverflow.ellipsis,
                                  style: Theme.of(context).textTheme.titleMedium
                                      ?.copyWith(
                                        color: const Color(0xFF0D47A1),
                                        fontWeight: FontWeight.w700,
                                      ),
                                ),
                                const SizedBox(height: 6),
                                Text(
                                  _latest == null
                                      ? 'Chưa có dữ liệu'
                                      : 'Tháng $latestMonth • last=$latestLast',
                                  maxLines: 2,
                                  overflow: TextOverflow.ellipsis,
                                  style: Theme.of(context).textTheme.bodySmall
                                      ?.copyWith(color: Colors.black54),
                                ),
                              ],
                            ),
                          ),
                        ],
                      ),
                    ),
                  ),

                  const SizedBox(height: 12),

                  // ===== CARD: Prev auto =====
                  Card(
                    elevation: 3,
                    shadowColor: Colors.blue.withOpacity(0.15),
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(18),
                    ),
                    child: ListTile(
                      contentPadding: const EdgeInsets.symmetric(
                        horizontal: 16,
                        vertical: 10,
                      ),
                      leading: Container(
                        padding: const EdgeInsets.all(10),
                        decoration: BoxDecoration(
                          color: const Color(0xFFBBDEFB),
                          borderRadius: BorderRadius.circular(14),
                        ),
                        child: const Icon(
                          Icons.history,
                          color: Color(0xFF1565C0),
                        ),
                      ),
                      title: const Text(
                        'Prev (auto)',
                        style: TextStyle(
                          fontWeight: FontWeight.w700,
                          color: Color(0xFF0D47A1),
                        ),
                      ),
                      subtitle: Text(
                        '$_prevAuto',
                        style: const TextStyle(color: Colors.black54),
                      ),
                    ),
                  ),

                  const Spacer(),

                  // ===== CTA button =====
                  SizedBox(
                    width: double.infinity,
                    height: 56,
                    child: FilledButton.icon(
                      style: FilledButton.styleFrom(
                        backgroundColor: const Color(0xFF1565C0),
                        foregroundColor: Colors.white,
                        shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(16),
                        ),
                        textStyle: const TextStyle(
                          fontSize: 16,
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                      onPressed: _takePhotoAndInput,
                      icon: const Icon(Icons.photo_camera),
                      label: const Text('Ghi số điện (chụp ảnh)'),
                    ),
                  ),
                ],
              ),
            ),
    );
  }
}

Widget _buildReadingImage(BuildContext context, String? imageReading) {
  debugPrint('Img_reading: $imageReading');

  if (imageReading == null || imageReading.isEmpty) {
    debugPrint('→ imageReading null or empty');
  } else {
    debugPrint('→ file exists: ${File(imageReading).existsSync()}');
  }

  if (imageReading != null &&
      imageReading.isNotEmpty &&
      File(imageReading).existsSync()) {
    return Image.file(File(imageReading), fit: BoxFit.cover);
  }

  return Container(
    color: Theme.of(context).colorScheme.surfaceContainerHighest,
    alignment: Alignment.center,
    child: const Icon(Icons.camera_alt, size: 32),
  );
}
