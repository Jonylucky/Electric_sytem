class Reading {
  final int? readingId;
  final String meterId;
  final String month;
  final int indexPrevMonth;
  final int indexLastMonth;
  final int indexConsumption;
  final String? imageReading;

  Reading({
    this.readingId,
    required this.meterId,
    required this.month,
    required this.indexPrevMonth,
    required this.indexLastMonth,
    required this.indexConsumption,
    this.imageReading,
  });

  factory Reading.fromMap(Map<String, dynamic> map) {
    return Reading(
      readingId: map['reading_id'],
      meterId: map['meter_id'],
      month: map['month'],
      indexPrevMonth: map['index_prev_month'],
      indexLastMonth: map['index_last_month'],
      indexConsumption: map['index_consumption'],
      imageReading: map['image_reading'],
    );
  }

  Map<String, dynamic> toMap() {
    return {
      'reading_id': readingId,
      'meter_id': meterId,
      'month': month,
      'index_prev_month': indexPrevMonth,
      'index_last_month': indexLastMonth,
      'index_consumption': indexConsumption,
      'image_reading': imageReading,
    };
  }
}
