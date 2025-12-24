package com.example.hodpital;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.sql.*;

public class AddRecordController {
    @FXML private TextField txtBP, txtTemp, txtHR, txtWeight;
    @FXML private TextArea txtDiagnosis, txtPrescription;

    private String currentPatientId;
    private int currentDoctorId;

    public void setPatientData(String patientId, int doctorId) {
        this.currentPatientId = patientId;
        this.currentDoctorId = doctorId;
    }

    @FXML
    private void handleSave() {
        DBConnection connect = new DBConnection();
        String sql = "INSERT INTO medical_records (patient_id, doctor_id, visit_date, blood_pressure, temperature_f, heart_rate_bpm, weight_lbs, diagnosis, prescription) VALUES (?, ?, GETDATE(), ?, ?, ?, ?, ?, ?)";

        try (Connection conn = connect.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, currentPatientId);
            pstmt.setInt(2, currentDoctorId);
            pstmt.setString(3, txtBP.getText());
            pstmt.setDouble(4, Double.parseDouble(txtTemp.getText()));
            pstmt.setInt(5, Integer.parseInt(txtHR.getText()));
            pstmt.setInt(6, Integer.parseInt(txtWeight.getText()));
            pstmt.setString(7, txtDiagnosis.getText());
            pstmt.setString(8, txtPrescription.getText());

            pstmt.executeUpdate();

            // إغلاق النافذة بعد الحفظ
            ((Stage) txtBP.getScene().getWindow()).close();

            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Record Saved Successfully!");
            alert.show();

        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML private void handleCancel() {
        ((Stage) txtBP.getScene().getWindow()).close();
    }
}