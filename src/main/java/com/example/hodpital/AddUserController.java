package com.example.hodpital;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ResourceBundle;

public class AddUserController implements Initializable {

    @FXML private Label lblWindowHeader;
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private TextField txtFullName;
    @FXML private ComboBox<String> cbRole;
    @FXML private TextField txtEmail;
    @FXML private TextField txtDepartment;
    @FXML private Button btnSave;

    private boolean updateMode = false;
    private String oldUsername;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // تعبئة قائمة الأدوار
        cbRole.getItems().addAll("Admin", "Doctor", "Nurse", "Receptionist");
    }

    // ==========================================
    // دالة لاستقبال البيانات عند التعديل
    // ==========================================
    public void setUpdateData(User user) {
        this.updateMode = true;
        this.oldUsername = user.getUsername();

        // تغيير نصوص الواجهة لتناسب التعديل
        lblWindowHeader.setText("Edit User");
        btnSave.setText("Update User");

        // ملء الخانات بالبيانات القديمة
        txtUsername.setText(user.getUsername());
        txtUsername.setDisable(true); // منع تغيير اسم المستخدم
        txtPassword.setText(user.getPassword()); // إظهار الباسورد القديم (اختياري)
        txtFullName.setText(user.getFullName());
        cbRole.setValue(user.getRole());
        txtEmail.setText(user.getEmail());
        txtDepartment.setText(user.getDepartment());
    }

    // ==========================================
    // دالة الحفظ (تشتغل للإضافة والتعديل)
    // ==========================================
    @FXML
    private void saveUser() {
        // التحقق من الحقول الإلزامية
        if (txtUsername.getText().isEmpty() || txtPassword.getText().isEmpty() || cbRole.getValue() == null || txtFullName.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please fill all required fields!");
            return;
        }

        DBConnection connectNow = new DBConnection();
        Connection connectDB = connectNow.getConnection();
        String sql;

        try {
            PreparedStatement statement;
            if (updateMode) {
                // --- كود التعديل (UPDATE) ---
                sql = "UPDATE users SET full_name=?, password=?, role=?, email=?, department=? WHERE username=?";
                statement = connectDB.prepareStatement(sql);
                statement.setString(1, txtFullName.getText());
                statement.setString(2, txtPassword.getText());
                statement.setString(3, cbRole.getValue());
                statement.setString(4, txtEmail.getText());
                statement.setString(5, txtDepartment.getText());
                statement.setString(6, oldUsername);
            } else {
                // --- كود الإضافة (INSERT) ---
                sql = "INSERT INTO users (username, full_name, password, role, email, department) VALUES (?, ?, ?, ?, ?, ?)";
                statement = connectDB.prepareStatement(sql);
                statement.setString(1, txtUsername.getText());
                statement.setString(2, txtFullName.getText());
                statement.setString(3, txtPassword.getText());
                statement.setString(4, cbRole.getValue());
                statement.setString(5, txtEmail.getText());
                statement.setString(6, txtDepartment.getText());
            }

            int rows = statement.executeUpdate();
            if (rows > 0) {
                // showAlert(Alert.AlertType.INFORMATION, "Success", updateMode ? "User updated successfully!" : "User added successfully!");
                closeWindow(); // إغلاق النافذة عند النجاح
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Operation failed!");
            }
            connectDB.close();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Database error: " + e.getMessage());
        }
    }

    @FXML
    private void closeWindow() {
        Stage stage = (Stage) txtUsername.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}