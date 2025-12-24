package com.example.hodpital;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ResourceBundle;

public class AddRoomController implements Initializable {

    @FXML private TextField txtRoomNumber, txtPrice;
    @FXML private ComboBox<String> cbType, cbStatus;
    @FXML private Spinner<Integer> spFloor;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // تعبئة القوائم
        cbType.getItems().addAll("General", "Private", "ICU", "Emergency");
        cbStatus.getItems().addAll("Available", "Occupied", "Maintenance");
        cbStatus.setValue("Available"); // القيمة الافتراضية

        // إعدادات العداد (الدور من 1 لـ 10)
        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 1);
        spFloor.setValueFactory(valueFactory);
    }

    @FXML
    private void saveRoom() {
        // التحقق من البيانات
        if (txtRoomNumber.getText().isEmpty() || cbType.getValue() == null || txtPrice.getText().isEmpty()) {
            showAlert("Please fill all required fields!");
            return;
        }

        try {
            DBConnection connect = new DBConnection();
            Connection conn = connect.getConnection();

            String sql = "INSERT INTO rooms (room_number, room_type, floor_number, price_per_day, status) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, txtRoomNumber.getText());
            pstmt.setString(2, cbType.getValue());
            pstmt.setInt(3, spFloor.getValue());
            pstmt.setDouble(4, Double.parseDouble(txtPrice.getText()));
            pstmt.setString(5, cbStatus.getValue().toLowerCase()); // الداتابيز بتخزن lower case

            pstmt.executeUpdate();

            conn.close();
            closeWindow(); // قفل النافذة بعد الحفظ

        } catch (NumberFormatException e) {
            showAlert("Price must be a valid number!");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error: " + e.getMessage()); // غالباً لو رقم الغرفة مكرر
        }
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setContentText(msg);
        alert.show();
    }

    @FXML
    private void closeWindow() {
        ((Stage) txtRoomNumber.getScene().getWindow()).close();
    }
}