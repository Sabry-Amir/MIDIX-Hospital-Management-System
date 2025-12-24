package com.example.hodpital;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.sql.*;

public class ScheduleApptController {

    @FXML private ComboBox<String> comboPatient, comboDoctor;
    @FXML private DatePicker dpDate;
    @FXML private TextField txtTime;

    @FXML
    public void initialize() {
        loadData();
    }

    private void loadData() {
        DBConnection connect = new DBConnection();
        try (Connection conn = connect.getConnection()) {
            // جلب المرضى
            ResultSet rsP = conn.createStatement().executeQuery("SELECT name FROM patients");
            while (rsP.next()) comboPatient.getItems().add(rsP.getString("name"));

            // جلب الأطباء
            ResultSet rsD = conn.createStatement().executeQuery("SELECT name FROM doctors");
            while (rsD.next()) comboDoctor.getItems().add(rsD.getString("name"));
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void handleSchedule(ActionEvent event) {
        if (comboPatient.getValue() == null || dpDate.getValue() == null) return;

        // تنفيذ كود INSERT في جدول appointments هنا
        System.out.println("Appointment Scheduled for: " + comboPatient.getValue());
        handleCancel(event);
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }
}