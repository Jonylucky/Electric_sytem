class Meter {
  final String meterId;
  final int companyId;
  final String? meterName;
  final String? imageMeter;

  Meter({
    required this.meterId,
    required this.companyId,
    this.meterName,
    this.imageMeter,
  });

  factory Meter.fromMap(Map<String, dynamic> map) {
    return Meter(
      meterId: map['meter_id'],
      companyId: map['company_id'],
      meterName: map['meter_name'],
      imageMeter: map['image_meter'],
    );
  }

  Map<String, dynamic> toMap() {
    return {
      'meter_id': meterId,
      'company_id': companyId,
      'meter_name': meterName,
      'image_meter': imageMeter,
    };
  }
}
