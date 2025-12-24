package com.example.hodpital;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class RegisterPatientController {

    @FXML private TextField txtName, txtPhone;
    @FXML private DatePicker dpDOB;
    @FXML private ComboBox<String> comboGender;
    @FXML private TextArea txtAllergies;

    @FXML
    public void initialize() {
        // ملء اختيارات الجنس عند فتح النافذة
        if (comboGender != null) {
            comboGender.getItems().addAll("Male", "Female", "Other");
        }
    }

    @FXML
    private void handleSave(ActionEvent event) {
        String name = txtName.getText();
        String phone = txtPhone.getText();

        if (name.isEmpty() || phone.isEmpty() || dpDOB.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Missing Info", "Please fill name, phone and date of birth.");
            return;
        }

        DBConnection connect = new DBConnection();
        String sql = "INSERT INTO patients (patient_id, name, phone, date_of_birth, gender, allergies, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, 'active')";

        try (Connection conn = connect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // توليد معرف بسيط أو استبدله بنظامك الخاص
            String pID = "P" + (System.currentTimeMillis() % 10000);

            pstmt.setString(1, pID);
            pstmt.setString(2, name);
            pstmt.setString(3, phone);
            pstmt.setDate(4, java.sql.Date.valueOf(dpDOB.getValue()));
            pstmt.setString(5, comboGender.getValue());
            pstmt.setString(6, txtAllergies.getText());

            pstmt.executeUpdate();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Patient registered successfully!");
            handleCancel(event);

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", e.getMessage());
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}