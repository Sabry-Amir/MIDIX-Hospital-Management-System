package com.example.hodpital;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.sql.*;

public class RecordVitalsController {
    @FXML private Label lblPatientHeader; // يعرض الاسم والـ ID
    @FXML private TextField txtBP, txtTemp, txtHeartRate, txtWeight;
    @FXML private TextArea txtNotes;

    private String currentPatientID;

    public void setPatientData(String id, String name) {
        this.currentPatientID = id;
        lblPatientHeader.setText(name + " - " + id);
    }

    @FXML
    private void handleSave() {
        DBConnection connect = new DBConnection();
        String sql = "INSERT INTO medical_records (patient_id, visit_date, blood_pressure, temperature_f, heart_rate_bpm, weight_lbs, notes) " +
                "VALUES (?, CAST(GETDATE() AS DATE), ?, ?, ?, ?, ?)";

        try (Connection conn = connect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, currentPatientID);
            pstmt.setString(2, txtBP.getText());
            pstmt.setDouble(3, Double.parseDouble(txtTemp.getText()));
            pstmt.setInt(4, Integer.parseInt(txtHeartRate.getText()));
            pstmt.setInt(5, Integer.parseInt(txtWeight.getText()));
            pstmt.setString(6, txtNotes.getText());

            pstmt.executeUpdate();
            handleCancel(); // إغلاق النافذة

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Check your inputs! " + e.getMessage());
            alert.show();
        }
    }

    @FXML private void handleCancel() {
        ((Stage) txtBP.getScene().getWindow()).close();
    }
}