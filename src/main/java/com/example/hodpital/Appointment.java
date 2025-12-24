package com.example.hodpital;

public class Appointment {
    private String id;
    private String patientId;
    private String patientName;
    private int doctorId;
    private String doctorName;
    private String date;
    private String time;
    private String reason;
    private String status;
    private String notes;


    public Appointment(String id, String patientId, String patientName, int doctorId, String doctorName, String date, String time, String reason, String status, String notes) {
        this.id = id;
        this.patientId = patientId;
        this.patientName = patientName;
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.date = date;
        this.time = time;
        this.reason = reason;
        this.status = status;
        this.notes = notes;
    }

    public String getId() {
        return id;
    }
    public String getPatientId() {
        return patientId;
    }
    public String getPatientName() {
        return patientName;
    }
    public int getDoctorId() {
        return doctorId;
    }
    public String getDoctorName() {
        return doctorName;
    }
    public String getDate() {
        return date;
    }
    public String getTime() {
        return time;
    }
    public String getReason() {
        return reason;
    }
    public String getStatus() {
        return status;
    }
    public String getNotes() {
        return notes;
    }


    public void createAppointment() { status = "scheduled"; }


    public void cancelAppointment() { status = "cancelled"; }


    public void confirm() { status = "confirmed"; }

}