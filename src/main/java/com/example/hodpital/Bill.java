package com.example.hodpital;

public class Bill {
    private String billId;
    private String patientId;
    private String patientName; // للعرض
    private String date;
    private double totalAmount;
    private double paidAmount;
    private double balanceDue;
    private String status;

    public Bill(String billId, String patientId, String patientName, String date,
                double totalAmount, double paidAmount, double balanceDue, String status) {
        this.billId = billId;
        this.patientId = patientId;
        this.patientName = patientName;
        this.date = date;
        this.totalAmount = totalAmount;
        this.paidAmount = paidAmount;
        this.balanceDue = balanceDue;
        this.status = status;
    }

    public String getBillId() { return billId; }
    public String getPatientId() { return patientId; }
    public String getPatientName() { return patientName; }
    public String getDate() { return date; }
    public double getTotalAmount() { return totalAmount; }
    public double getPaidAmount() { return paidAmount; }
    public double getBalanceDue() { return balanceDue; }
    public String getStatus() { return status; }

    public void createBill() {}

    public void pay(double amount) {
        if (amount > 0) {
            paidAmount += amount;
            balanceDue = totalAmount - paidAmount;
            if (balanceDue <= 0) status = "paid";
        }
    }
    public double getBalance() { return balanceDue; }
}