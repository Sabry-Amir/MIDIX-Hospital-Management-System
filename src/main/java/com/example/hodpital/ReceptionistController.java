package com.example.hodpital;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class ReceptionistController implements Initializable {

    @FXML private Label lblTotalPatients, lblTodayAppt, lblPendingAppt;
    @FXML private TableView<Appointment> tblAppointments;
    @FXML private TableColumn<Appointment, String> colID, colPatient, colDoctor, colDate, colTime, colStatus;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableStyle();
        setupTableColumns();
        Platform.runLater(this::loadDashboardData);
    }

    private void setupTableColumns() {
        colID.setCellValueFactory(new PropertyValueFactory<>("id"));
        colPatient.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        colDoctor.setCellValueFactory(new PropertyValueFactory<>("doctorName"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colTime.setCellValueFactory(new PropertyValueFactory<>("time"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // ستايل الـ Status Badges (تصميم راقي للحالة)
        colStatus.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(item.toUpperCase());
                    badge.setPadding(new Insets(4, 12, 4, 12));
                    String baseStyle = "-fx-background-radius: 15; -fx-font-size: 10px; -fx-font-weight: bold;";
                    if (item.equalsIgnoreCase("confirmed")) {
                        badge.setStyle(baseStyle + "-fx-background-color: #dcfce7; -fx-text-fill: #166534;");
                    } else if (item.equalsIgnoreCase("scheduled")) {
                        badge.setStyle(baseStyle + "-fx-background-color: #dbeafe; -fx-text-fill: #1e40af;");
                    } else {
                        badge.setStyle(baseStyle + "-fx-background-color: #f1f5f9; -fx-text-fill: #475569;");
                    }
                    setGraphic(badge);
                }
            }
        });
    }

    private void setupTableStyle() {
        tblAppointments.setStyle("-fx-background-color: transparent; -fx-selection-bar: #eff6ff;");
        String cellStyle = "-fx-alignment: CENTER-LEFT; -fx-font-size: 13px; -fx-text-fill: #334155; -fx-padding: 12 5 12 5;";
        colPatient.setStyle(cellStyle + "-fx-font-weight: bold;");
        colDoctor.setStyle(cellStyle);
        colDate.setStyle(cellStyle);
        colTime.setStyle(cellStyle);

        // تعديل الهيدر برمجياً ليكون Flat
        tblAppointments.widthProperty().addListener((obs, oldVal, newVal) -> {
            Pane header = (Pane) tblAppointments.lookup("TableHeaderRow");
            if (header != null) header.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #e2e8f0; -fx-border-width: 0 0 1 0;");
        });
    }

    private void loadDashboardData() {
        ObservableList<Appointment> list = FXCollections.observableArrayList();
        DBConnection connect = new DBConnection();
        try (Connection conn = connect.getConnection()) {
            String sql = "SELECT a.appointment_id, a.patient_id, p.name as p_name, a.doctor_id, d.name as d_name, " +
                    "a.appointment_date, a.appointment_time, a.status FROM appointments a " +
                    "JOIN patients p ON a.patient_id = p.patient_id JOIN doctors d ON a.doctor_id = d.doctor_id";
            ResultSet rs = conn.createStatement().executeQuery(sql);
            while (rs.next()) {
                list.add(new Appointment(String.valueOf(rs.getInt("appointment_id")), rs.getString("patient_id"),
                        rs.getString("p_name"), rs.getInt("doctor_id"), rs.getString("d_name"),
                        rs.getString("appointment_date"), rs.getString("appointment_time"), "", rs.getString("status"), ""));
            }
            tblAppointments.setItems(list);
            ResultSet rsStat = conn.createStatement().executeQuery("SELECT COUNT(*) FROM patients");
            if (rsStat.next()) lblTotalPatients.setText(String.valueOf(rsStat.getInt(1)));
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML private void handleLogout() throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("Login.fxml"));
        Stage stage = (Stage) lblTotalPatients.getScene().getWindow();
        stage.setScene(new Scene(root));
    }

    @FXML private void openRegisterPatient() { loadPopup("RegisterPatient.fxml", "Register New Patient"); }
    @FXML private void openScheduleAppointment() { loadPopup("ScheduleAppointment.fxml", "Schedule Appointment"); }
    @FXML private void openAssignRoom() { loadPopup("AssignRoom.fxml", "Assign Room"); }

    private void loadPopup(String fxml, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.showAndWait();
            loadDashboardData();
        } catch (Exception e) { e.printStackTrace(); }
    }
}