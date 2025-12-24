package com.example.hodpital;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.Random;
import java.util.ResourceBundle;

public class AddPatientController implements Initializable {

    @FXML private TextField txtName, txtPhone, txtAddress, txtEmergName, txtEmergPhone;
    @FXML private ComboBox<String> cbGender, cbStatus, cbBlood;
    @FXML private DatePicker dpDOB;
    @FXML private Button btnSave;
    @FXML private Label lblHeader;

    private boolean updateMode = false;
    private String patientId;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cbGender.getItems().addAll("Male", "Female");
        cbStatus.getItems().addAll("active", "admitted", "discharged");
        cbBlood.getItems().addAll("A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-");
    }

    public void setUpdateData(Patient p) {
        updateMode = true;
        patientId = p.getId();
        lblHeader.setText("Edit Patient");
        btnSave.setText("Update");

        txtName.setText(p.getName());
        txtPhone.setText(p.getPhone());
        txtAddress.setText(p.getAddress());
        txtEmergName.setText(p.getEmergencyContact());
        txtEmergPhone.setText(p.getEmergencyPhone());
        cbGender.setValue(p.getGender());
        cbStatus.setValue(p.getStatus());
        cbBlood.setValue(p.getBloodType());
        if(p.getDob() != null) dpDOB.setValue(LocalDate.parse(p.getDob()));
    }

    @FXML
    private void savePatient() {
        if(txtName.getText().isEmpty() || txtPhone.getText().isEmpty()) {
            // ممكن تظهر رسالة خطأ هنا
            return;
        }

        String sql;
        DBConnection connect = new DBConnection();

        try (Connection conn = connect.getConnection()) {
            PreparedStatement pstmt;

            if (updateMode) {
                sql = "UPDATE patients SET name=?, gender=?, phone=?, blood_type=?, status=?, address=?, emergency_contact=?, emergency_phone=?, date_of_birth=?, age=? WHERE patient_id=?";
                pstmt = conn.prepareStatement(sql);
            } else {
                // توليد ID جديد (مثال بسيط: P + وقت حالي)
                patientId = "P" + (System.currentTimeMillis() % 10000);
                sql = "INSERT INTO patients (name, gender, phone, blood_type, status, address, emergency_contact, emergency_phone, date_of_birth, age, patient_id) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
                pstmt = conn.prepareStatement(sql);
            }

            pstmt.setString(1, txtName.getText());
            pstmt.setString(2, cbGender.getValue());
            pstmt.setString(3, txtPhone.getText());
            pstmt.setString(4, cbBlood.getValue());
            pstmt.setString(5, cbStatus.getValue());
            pstmt.setString(6, txtAddress.getText());
            pstmt.setString(7, txtEmergName.getText());
            pstmt.setString(8, txtEmergPhone.getText());

            String dob = (dpDOB.getValue() != null) ? dpDOB.getValue().toString() : null;
            pstmt.setString(9, dob);

            int age = 0;
            if (dpDOB.getValue() != null) {
                age = java.time.Period.between(dpDOB.getValue(), LocalDate.now()).getYears();
            }
            pstmt.setInt(10, age);

            if (updateMode) {
                pstmt.setString(11, patientId);
            } else {
                pstmt.setString(11, patientId); // للـ INSERT
            }

            pstmt.executeUpdate();
            closeWindow();

        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML private void closeWindow() {
        ((Stage) txtName.getScene().getWindow()).close();
    }
}