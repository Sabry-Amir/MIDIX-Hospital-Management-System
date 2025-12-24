package com.example.hodpital;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class DoctorController implements Initializable {

    // عناصر الواجهة العلوية والإحصائيات
    @FXML private Label lblTotalPatients, lblTodayAppt, lblTotalRecords, lblDoctorName;
    @FXML private VBox pnlUpcomingAppt; // حاوية المواعيد (يمين)
    @FXML private VBox pnlMedicalRecords; // حاوية السجلات التفصيلية (يسار)

    // جدول المرضى
    @FXML private TableView<Patient> tblPatients;
    @FXML private TableColumn<Patient, String> colPatID, colPatName, colPatBlood, colPatStatus;
    @FXML private TableColumn<Patient, Integer> colPatAge;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        loadDashboardData();
    }

    private void setupTableColumns() {
        if (tblPatients != null && colPatID != null) {
            colPatID.setCellValueFactory(new PropertyValueFactory<>("id"));
            colPatName.setCellValueFactory(new PropertyValueFactory<>("name"));
            colPatAge.setCellValueFactory(new PropertyValueFactory<>("age"));
            colPatBlood.setCellValueFactory(new PropertyValueFactory<>("bloodType"));
            colPatStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
            tblPatients.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        }
    }

    private void loadDashboardData() {
        DBConnection connect = new DBConnection();
        try (Connection conn = connect.getConnection()) {
            updateStatistics(conn);
            loadPatientsTable(conn);
            loadAppointmentsList(conn);
            loadDetailedMedicalHistory(conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // بناء قائمة المواعيد (الجزء الأيمن)
    private void loadAppointmentsList(Connection conn) throws SQLException {
        if (pnlUpcomingAppt == null) return;
        pnlUpcomingAppt.getChildren().clear();

        String sql = "SELECT a.appointment_id, a.patient_id, p.name AS patient_name, " +
                "a.doctor_id, d.name AS doctor_name, a.appointment_date, " +
                "a.appointment_time, a.reason, a.status, a.notes " +
                "FROM appointments a " +
                "JOIN patients p ON a.patient_id = p.patient_id " +
                "JOIN doctors d ON a.doctor_id = d.doctor_id";

        ResultSet rs = conn.createStatement().executeQuery(sql);
        while (rs.next()) {
            Appointment appt = new Appointment(
                    String.valueOf(rs.getInt("appointment_id")), rs.getString("patient_id"),
                    rs.getString("patient_name"), rs.getInt("doctor_id"), rs.getString("doctor_name"),
                    rs.getString("appointment_date"), rs.getString("appointment_time"),
                    rs.getString("reason"), rs.getString("status"), rs.getString("notes")
            );
            pnlUpcomingAppt.getChildren().add(createAppointmentCardUI(appt));
        }
    }

    // إنشاء كارت الموعد مع تفعيل زر Add Record
    private HBox createAppointmentCardUI(Appointment appt) {
        HBox card = new HBox(20);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 12; -fx-border-color: #f1f5f9;");

        VBox info = new VBox(5);
        Label lblName = new Label(appt.getPatientName());
        lblName.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");
        Label lblDetails = new Label(appt.getReason() + "\n" + appt.getDate() + " at " + appt.getTime());
        lblDetails.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11;");
        info.getChildren().addAll(lblName, lblDetails);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // زر إضافة سجل طبي
        Button btnAdd = new Button("Add Record");
        btnAdd.setStyle("-fx-background-color: #6366f1; -fx-text-fill: white; -fx-background-radius: 8; -fx-font-weight: bold; -fx-cursor: hand;");

        // عند الضغط يفتح نافذة الإضافة
        btnAdd.setOnAction(event -> openAddRecordWindow(appt.getPatientId(), appt.getDoctorId()));

        card.getChildren().addAll(info, spacer, btnAdd);
        return card;
    }

    // ميثود فتح نافذة إضافة السجل الطبي
    private void openAddRecordWindow(String patientId, int doctorId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("AddRecord.fxml"));
            Parent root = loader.load();

            // تمرير بيانات المريض للطبيب للنافذة الجديدة
            AddRecordController controller = loader.getController();
            controller.setPatientData(patientId, doctorId);

            Stage stage = new Stage();
            stage.setTitle("Add Medical Record");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // بناء كروت السجلات الطبية (الجزء الأيسر)
    private void loadDetailedMedicalHistory(Connection conn) throws SQLException {
        if (pnlMedicalRecords == null) return;
        pnlMedicalRecords.getChildren().clear();

        String sql = "SELECT m.*, p.name FROM medical_records m JOIN patients p ON m.patient_id = p.patient_id";
        ResultSet rs = conn.createStatement().executeQuery(sql);
        while (rs.next()) {
            pnlMedicalRecords.getChildren().add(createDetailedRecordCard(
                    rs.getString("name"), rs.getString("visit_date"), rs.getString("blood_pressure"),
                    String.valueOf(rs.getFloat("temperature_f")), String.valueOf(rs.getInt("heart_rate_bpm")),
                    String.valueOf(rs.getInt("weight_lbs")), rs.getString("diagnosis")
            ));
        }
    }

    private VBox createDetailedRecordCard(String name, String date, String bp, String temp, String hr, String weight, String diag) {
        VBox card = new VBox(15);
        card.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 15; -fx-border-color: #e2e8f0;");

        HBox vitals = new HBox(10);
        vitals.getChildren().addAll(
                createVitalBox("BP", bp), createVitalBox("Temp", temp + "°F"),
                createVitalBox("HR", hr), createVitalBox("Weight", weight)
        );

        Label lblN = new Label(name + " - " + date);
        lblN.setStyle("-fx-font-weight: bold; -fx-font-size: 15;");

        card.getChildren().addAll(lblN, vitals, new Separator(), new Label("Diagnosis: " + diag));
        return card;
    }

    private VBox createVitalBox(String title, String value) {
        VBox box = new VBox(5); box.setAlignment(Pos.CENTER_LEFT);
        box.setStyle("-fx-background-color: #f8fafc; -fx-padding: 8; -fx-background-radius: 8; -fx-min-width: 120;");
        box.getChildren().addAll(new Label(title), new Label(value));
        return box;
    }

    private void loadPatientsTable(Connection conn) throws SQLException {
        if (tblPatients == null) return;
        ObservableList<Patient> list = FXCollections.observableArrayList();
        ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM patients");
        while (rs.next()) {
            list.add(new Patient(rs.getString("patient_id"), rs.getString("name"), rs.getInt("age"), rs.getString("gender"), rs.getString("phone"), rs.getString("blood_type"), rs.getString("status"), rs.getString("address"), rs.getString("emergency_contact"), rs.getString("emergency_phone"), rs.getString("allergies"), rs.getString("date_of_birth")));
        }
        tblPatients.setItems(list);
    }

    private void updateStatistics(Connection conn) throws SQLException {
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery("SELECT (SELECT COUNT(*) FROM patients), (SELECT COUNT(*) FROM appointments), (SELECT COUNT(*) FROM medical_records)");
        if (rs.next()) {
            if (lblTotalPatients != null) lblTotalPatients.setText(String.valueOf(rs.getInt(1)));
            if (lblTodayAppt != null) lblTodayAppt.setText(String.valueOf(rs.getInt(2)));
            if (lblTotalRecords != null) lblTotalRecords.setText(String.valueOf(rs.getInt(3)));
        }
    }

    @FXML private void handleLogout(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("Login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }
}