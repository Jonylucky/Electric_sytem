class Company {
  final int? companyId;
  final String? companyCode;
  final String companyName;
  final String? createdAt;

  Company({
    this.companyId,
    this.companyCode,
    required this.companyName,
    this.createdAt,
  });

  factory Company.fromMap(Map<String, dynamic> map) {
    return Company(
      companyId: map['company_id'],
      companyCode: map['company_code'],
      companyName: map['company_name'],
      createdAt: map['created_at'],
    );
  }

  Map<String, dynamic> toMap() {
    return {
      'company_id': companyId,
      'company_code': companyCode,
      'company_name': companyName,
      'created_at': createdAt,
    };
  }
}
