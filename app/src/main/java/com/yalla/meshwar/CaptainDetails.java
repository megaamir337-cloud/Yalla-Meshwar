package com.yalla.meshwar;

public class CaptainDetails {
    public String captainId;
    public String name;
    public String phone;
    public String vehicleType;  // توك توك / سكوتر / ملاكي
    public String plateNumber;  // رقم اللوحة
    public String photoUrl;     // صورة الكابتن
    public double rating;       // التقييم

    public CaptainDetails() {
        // فارغ لمتطلبات Firebase
    }

    public CaptainDetails(String captainId, String name, String phone, String vehicleType, String plateNumber, String photoUrl, double rating) {
        this.captainId = captainId;
        this.name = name;
        this.phone = phone;
        this.vehicleType = vehicleType;
        this.plateNumber = plateNumber;
        this.photoUrl = photoUrl;
        this.rating = rating;
    }
}
