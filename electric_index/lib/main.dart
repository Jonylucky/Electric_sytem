import 'package:flutter/material.dart';
import 'package:electric_index/db/dao/database_helper.dart';
import 'package:electric_index/screens/home/home_screen.dart';

import 'package:electric_index/db/seed/db_seeder.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();

  final db = await DatabaseHelper.instance.database;

  print('✅ Database opened: ${db.path}');

  await DbSeeder.seedIfNeeded();
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false, // ✅ thêm dòng này
      home: HomeScreen(),
    );
  }
}
