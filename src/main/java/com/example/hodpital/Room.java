package com.example.hodpital;

public class Room {
    private String roomNumber;
    private String roomType;
    private int floor;
    private double price;
    private String status;
    private String patientName; // عشان نعرض اسم المريض لو الغرفة مشغولة

    public Room(String roomNumber, String roomType, int floor, double price, String status, String patientName) {
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.floor = floor;
        this.price = price;
        this.status = status;
        this.patientName = patientName;
    }

    public String getRoomNumber() { return roomNumber; }
    public String getRoomType() { return roomType; }
    public int getFloor() { return floor; }
    public double getPrice() { return price; }
    public String getStatus() { return status; }
    public String getPatientName() { return patientName; }


}