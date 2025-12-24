package com.example.hodpital;

public class MedicalRecord {
    private String recordId, patientName, doctorName, visitDate, diagnosis, bloodType;

    public MedicalRecord(String recordId, String patientName, String doctorName, String visitDate, String diagnosis, String bloodType) {
        this.recordId = recordId;
        this.patientName = patientName;
        this.doctorName = doctorName;
        this.visitDate = visitDate;
        this.diagnosis = diagnosis;
        this.bloodType = bloodType;
    }

    public String getRecordId() { return recordId; }
    public String getPatientName() { return patientName; }
    public String getDoctorName() { return doctorName; }
    public String getVisitDate() { return visitDate; }
    public String getDiagnosis() { return diagnosis; }
    public String getBloodType() { return bloodType; }

    public void addRecord() {}

    public void updateDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }


    public void displayRecord() {
        System.out.println("Patient name: " + patientName);
        System.out.println("Doctor: " + doctorName);
        System.out.println("Visit Date: " + visitDate);
        System.out.println("Diagnosis: " + diagnosis);
    }
}