package com.example.hodpital;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class LoginController {
    @FXML private Button Cancel;
    @FXML private Label WarningMassage;
    @FXML private TextField Username;
    @FXML private PasswordField Password;

    public void CancelAction(ActionEvent e) {
        Stage stage = (Stage) Cancel.getScene().getWindow();
        stage.close();
    }

    public void LoginAction(ActionEvent e) {
        if (!Username.getText().isBlank() && !Password.getText().isBlank()) {
            ValidateLogin(e);
        } else {
            WarningMassage.setText("Please fill all the fields");
        }
    }

    public void ValidateLogin(ActionEvent e) {
        DBConnection connectNow = new DBConnection();
        Connection connectDB = connectNow.getConnection();

        // 1. الاستعلام يجيب الـ Role والـ Name والـ ID
        String VerifyLogin = "SELECT user_id, full_name, role FROM users WHERE Username = '"
                + Username.getText() + "' AND [Password] = '" + Password.getText() + "'";

        try {
            Statement statement = connectDB.createStatement();
            ResultSet queryResult = statement.executeQuery(VerifyLogin);

            if (queryResult.next()) {
                // 2. استخراج البيانات وحفظها في الـ Session
                UserSession.setUserId(queryResult.getInt("user_id"));
                UserSession.setUserName(queryResult.getString("full_name"));
                String role = queryResult.getString("role");
                UserSession.setUserRole(role);

                // 3. تحديد ملف الـ FXML بناءً على الصلاحية
                String fxmlFile = "";
                String title = "";

                switch (role.toLowerCase()) {
                    case "admin":
                        fxmlFile = "Admin_Home.fxml";
                        title = "MEDIX - Admin Panel";
                        break;
                    case "doctor":
                        fxmlFile = "Doctor_Home.fxml";
                        title = "MEDIX - Doctor Portal";
                        break;
                    case "nurse":
                        fxmlFile = "Nurse_Home.fxml";
                        title = "MEDIX - Nursing Station";
                        break;
                    case "receptionist":
                        fxmlFile = "Receptionist_Home.fxml";
                        title = "MEDIX - Reception Desk";
                        break;
                    default:
                        WarningMassage.setText("Access Denied: Role Unknown");
                        return;
                }

                // 4. فتح الصفحة المطلوبة
                loadDashboard(fxmlFile, title, e);

            } else {
                WarningMassage.setText("Wrong Username or Password");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            WarningMassage.setText("Database Connection Error");
        }
    }

    private void loadDashboard(String fxml, String title, ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
            stage.show();
        } catch (IOException ex) {
            ex.printStackTrace();
            WarningMassage.setText("Error: File " + fxml + " not found!");
        }
    }
}