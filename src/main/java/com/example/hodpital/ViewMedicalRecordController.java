package com.example.hodpital;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.ResultSet;

public class ViewMedicalRecordController {

    // الربط مع عناصر الـ FXML (المربعات والنصوص)
    @FXML private Label lblPatientName, lblPatientID, lblBloodType;
    @FXML private Label lblDoctorName, lblDate;
    @FXML private Label lblBP, lblTemp, lblHeartRate;
    @FXML private Label lblDiagnosis, lblPrescription;

    /**
     * الدالة دي هي اللي بتستلم البيانات من الجدول الرئيسي وتوزعها
     */
    public void setRecordData(MedicalRecord record) {
        // بيانات مباشرة من الموديل (كده حلينا الـ Symbols error)
        lblPatientName.setText(record.getPatientName());
        lblBloodType.setText("Blood Type: " + record.getBloodType());
        lblDoctorName.setText("Dr. " + record.getDoctorName());
        lblDate.setText("Date: " + record.getVisitDate());
        lblDiagnosis.setText("Diagnosis: " + record.getDiagnosis());

        // تحميل البيانات الحيوية (Vitals) من الداتابيز بناءً على الـ ID
        loadVitalsFromDB(record.getRecordId());
    }

    private void loadVitalsFromDB(String recordId) {
        DBConnection connect = new DBConnection();
        String sql = "SELECT blood_pressure, temperature_f, heart_rate_bpm, prescription " +
                "FROM medical_records WHERE record_id = '" + recordId + "'";

        try (Connection conn = connect.getConnection();
             ResultSet rs = conn.createStatement().executeQuery(sql)) {

            if (rs.next()) {
                lblBP.setText(rs.getString("blood_pressure"));
                lblTemp.setText(rs.getString("temperature_f") + " °F");
                lblHeartRate.setText(rs.getString("heart_rate_bpm") + " bpm");
                lblPrescription.setText("Prescription: " + rs.getString("prescription"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void closeWindow() {
        // إغلاق النافذة (الزرار اللي تحت)
        Stage stage = (Stage) lblPatientName.getScene().getWindow();
        stage.close();
    }
}