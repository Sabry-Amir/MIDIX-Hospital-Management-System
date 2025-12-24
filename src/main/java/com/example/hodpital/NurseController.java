package com.example.hodpital;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class NurseController implements Initializable {

    @FXML private Label lblNurseName, lblWard, lblAdmittedCount, lblVitalsCount;
    @FXML private VBox pnlPatientCards;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(this::loadNurseDashboard);
    }

    private void loadNurseDashboard() {
        pnlPatientCards.getChildren().clear();
        DBConnection connect = new DBConnection();

        try (Connection conn = connect.getConnection()) {
            if (conn == null) return;

            Statement stmt = conn.createStatement();

            // 1. تحديث عدد المرضى المحجوزين حالياً (Admitted Patients)
            ResultSet rsAdmitted = stmt.executeQuery("SELECT COUNT(*) FROM patients WHERE status = 'admitted'");
            if (rsAdmitted.next()) lblAdmittedCount.setText(String.valueOf(rsAdmitted.getInt(1)));

            // 2. تحديث عدد العلامات الحيوية المسجلة اليوم (Vitals Recorded Today)
            // نستخدم CAST(GETDATE() AS DATE) لضمان مطابقة التاريخ فقط بدون الوقت
            ResultSet rsVitalsToday = stmt.executeQuery("SELECT COUNT(*) FROM medical_records WHERE visit_date = CAST(GETDATE() AS DATE)");
            if (rsVitalsToday.next()) lblVitalsCount.setText(String.valueOf(rsVitalsToday.getInt(1)));

            // 3. جلب بيانات المرضى مع أحدث العلامات الحيوية
            String query = "SELECT p.patient_id, p.name, p.blood_type, p.allergies, " +
                    "v.blood_pressure, v.temperature_f, v.heart_rate_bpm, v.weight_lbs " +
                    "FROM patients p " +
                    "OUTER APPLY ( " +
                    "    SELECT TOP 1 blood_pressure, temperature_f, heart_rate_bpm, weight_lbs " +
                    "    FROM medical_records " +
                    "    WHERE patient_id = p.patient_id " +
                    "    ORDER BY record_id DESC " +
                    ") v " +
                    "WHERE p.status = 'admitted'";

            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                pnlPatientCards.getChildren().add(createPatientCard(
                        rs.getString("patient_id"),
                        rs.getString("name"),
                        rs.getString("blood_type"),
                        rs.getString("allergies"),
                        rs.getString("blood_pressure"),
                        rs.getString("temperature_f"),
                        rs.getString("heart_rate_bpm"),
                        rs.getString("weight_lbs")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private VBox createPatientCard(String id, String name, String blood, String allergiesText,
                                   String bp, String temp, String hr, String weight) {
        VBox card = new VBox(15);
        card.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 12; -fx-border-color: #f1f5f9;");

        HBox header = new HBox();
        VBox patientInfo = new VBox(2);
        Label lblName = new Label(name != null ? name : "Unknown Patient");
        lblName.setStyle("-fx-font-weight: bold; -fx-font-size: 18; -fx-text-fill: #1e293b;");
        Label lblID = new Label("Patient ID: " + id + " | Blood: " + (blood != null ? blood : "N/A"));
        lblID.setStyle("-fx-text-fill: #64748b; -fx-font-size: 13;");
        patientInfo.getChildren().addAll(lblName, lblID);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnRecord = new Button("Record Vitals");
        btnRecord.setStyle("-fx-background-color: #6366f1; -fx-text-fill: white; -fx-background-radius: 8; -fx-cursor: hand; -fx-font-weight: bold;");
        btnRecord.setPadding(new Insets(8, 20, 8, 20));
        btnRecord.setOnAction(e -> openVitalsPopup(id, name));

        header.getChildren().addAll(patientInfo, spacer, btnRecord);

        Label lblAllergies = new Label("⚠️ Allergies: " + (allergiesText != null && !allergiesText.isEmpty() ? allergiesText : "None"));
        lblAllergies.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");

        HBox vitalsRow = new HBox(30);
        vitalsRow.setStyle("-fx-background-color: #f8fafc; -fx-padding: 15; -fx-background-radius: 10;");
        vitalsRow.setAlignment(Pos.CENTER_LEFT);

        vitalsRow.getChildren().addAll(
                createVitalBox("Blood Pressure", bp),
                createVitalBox("Temperature", (temp != null ? temp + "°F" : "---")),
                createVitalBox("Heart Rate", (hr != null ? hr + " bpm" : "---")),
                createVitalBox("Weight", (weight != null ? weight + " lbs" : "---"))
        );

        card.getChildren().addAll(header, lblAllergies, new Separator(), vitalsRow);
        return card;
    }

    private VBox createVitalBox(String label, String value) {
        VBox box = new VBox(5);
        Label lblTitle = new Label(label);
        lblTitle.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11;");
        Label lblValue = new Label((value != null && !value.equals("null")) ? value : "---");
        lblValue.setStyle("-fx-font-weight: bold; -fx-font-size: 15; -fx-text-fill: #0f172a;");
        box.getChildren().addAll(lblTitle, lblValue);
        box.setMinWidth(110);
        return box;
    }

    private void openVitalsPopup(String patientID, String patientName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/hodpital/RecordVitals.fxml"));
            Parent root = loader.load();
            RecordVitalsController controller = loader.getController();
            controller.setPatientData(patientID, patientName);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadNurseDashboard(); // تحديث الصفحة ليعكس التغيير في العداد فوراً
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML private void handleLogout() throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("Login.fxml"));
        Stage stage = (Stage) lblWard.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }
}