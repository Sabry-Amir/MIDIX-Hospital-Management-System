package com.example.hodpital;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.sql.*;

public class AssignRoomController {

    @FXML private ComboBox<String> comboPatient, comboRoom;
    @FXML private Label lblTotalAvailable;

    @FXML
    public void initialize() {
        loadRoomsAndPatients();
    }

    private void loadRoomsAndPatients() {
        DBConnection connect = new DBConnection();
        try (Connection conn = connect.getConnection()) {
            // جلب المرضى المحجوزين فقط الذين لم يسكنوا بعد
            ResultSet rsP = conn.createStatement().executeQuery("SELECT name FROM patients WHERE status = 'admitted'");
            while (rsP.next()) comboPatient.getItems().add(rsP.getString("name"));

            // جلب الغرف المتاحة
            ResultSet rsR = conn.createStatement().executeQuery("SELECT room_number FROM rooms WHERE status = 'available'");
            int count = 0;
            while (rsR.next()) {
                comboRoom.getItems().add(rsR.getString("room_number"));
                count++;
            }
            lblTotalAvailable.setText(count + " Available");
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void handleAssign(ActionEvent event) {
        // تحديث جدول الغرف ليصبح occupied وتحديث بيانات المريض
        System.out.println("Room " + comboRoom.getValue() + " Assigned to " + comboPatient.getValue());
        handleCancel(event);
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }
}