package com.example.hodpital;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class AddAppointmentController implements Initializable {

    @FXML private ComboBox<String> cbPatient, cbDoctor, cbStatus;
    @FXML private DatePicker dpDate;
    @FXML private TextField txtTime, txtReason;
    @FXML private TextArea txtNotes;
    @FXML private Button btnSave;
    @FXML private Label lblHeader;

    private boolean updateMode = false;
    private int appointmentId; // تغيير النوع لـ int ليتوافق مع IDENTITY في DB

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cbStatus.getItems().addAll("scheduled", "confirmed", "completed", "cancelled");
        loadPatients();
        loadDoctors();
    }

    private void loadPatients() {
        cbPatient.getItems().clear();
        DBConnection connect = new DBConnection();
        try (Connection conn = connect.getConnection();
             ResultSet rs = conn.createStatement().executeQuery("SELECT patient_id, name FROM patients")) {
            while (rs.next()) {
                cbPatient.getItems().add(rs.getString("name") + " (" + rs.getString("patient_id") + ")");
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadDoctors() {
        cbDoctor.getItems().clear();
        DBConnection connect = new DBConnection();
        String sql = "SELECT doctor_id, name, specialization FROM doctors";
        try (Connection conn = connect.getConnection();
             ResultSet rs = conn.createStatement().executeQuery(sql)) {
            while (rs.next()) {
                String docDisplay = rs.getString("name") + " (" + rs.getString("specialization") + ") [" + rs.getInt("doctor_id") + "]";
                cbDoctor.getItems().add(docDisplay);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void setUpdateData(Appointment apt) {
        updateMode = true;
        // تحويل الـ ID من String لـ int إذا لزم الأمر
        this.appointmentId = Integer.parseInt(apt.getId());
        lblHeader.setText("Edit Appointment");
        btnSave.setText("Update Appointment");

        cbPatient.getSelectionModel().select(apt.getPatientName() + " (" + apt.getPatientId() + ")");

        for (String docItem : cbDoctor.getItems()) {
            if (docItem.contains("[" + apt.getDoctorId() + "]")) {
                cbDoctor.getSelectionModel().select(docItem);
                break;
            }
        }

        dpDate.setValue(LocalDate.parse(apt.getDate()));
        txtTime.setText(apt.getTime());
        cbStatus.setValue(apt.getStatus());
        txtReason.setText(apt.getReason());
        txtNotes.setText(apt.getNotes());
    }

    @FXML
    private void saveAction() {
        if(cbPatient.getValue() == null || cbDoctor.getValue() == null || dpDate.getValue() == null) {
            new Alert(Alert.AlertType.WARNING, "Please fill all required fields!").show();
            return;
        }

        try {
            // استخراج ID المريض
            String pVal = cbPatient.getValue();
            String pID = pVal.substring(pVal.lastIndexOf("(") + 1, pVal.lastIndexOf(")"));

            // استخراج ID الدكتور
            String dVal = cbDoctor.getValue();
            int dID = Integer.parseInt(dVal.substring(dVal.lastIndexOf("[") + 1, dVal.lastIndexOf("]")));

            DBConnection connect = new DBConnection();
            try (Connection conn = connect.getConnection()) {
                PreparedStatement pstmt;
                String sql;

                if (updateMode) {
                    // في حالة التحديث نستخدم الـ ID الموجود
                    sql = "UPDATE appointments SET patient_id=?, doctor_id=?, appointment_date=?, appointment_time=?, status=?, reason=?, notes=? WHERE appointment_id=?";
                    pstmt = conn.prepareStatement(sql);

                    pstmt.setString(1, pID);
                    pstmt.setInt(2, dID);
                    pstmt.setString(3, dpDate.getValue().toString());
                    pstmt.setString(4, txtTime.getText());
                    pstmt.setString(5, cbStatus.getValue());
                    pstmt.setString(6, txtReason.getText());
                    pstmt.setString(7, txtNotes.getText());
                    pstmt.setInt(8, appointmentId);
                } else {
                    // ✅ التعديل الجوهري: لا نرسل الـ ID يدوياً هنا لأن الداتابيز IDENTITY
                    sql = "INSERT INTO appointments (patient_id, doctor_id, appointment_date, appointment_time, status, reason, notes) VALUES (?,?,?,?,?,?,?)";
                    pstmt = conn.prepareStatement(sql);

                    pstmt.setString(1, pID);
                    pstmt.setInt(2, dID);
                    pstmt.setString(3, dpDate.getValue().toString());
                    pstmt.setString(4, txtTime.getText());
                    pstmt.setString(5, cbStatus.getValue());
                    pstmt.setString(6, txtReason.getText());
                    pstmt.setString(7, txtNotes.getText());
                }

                int rows = pstmt.executeUpdate();
                if (rows > 0) {
                    System.out.println("✅ Appointment Processed Successfully!");
                    closeWindow();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Database Error: " + e.getMessage()).show();
        }
    }

    @FXML private void closeWindow() {
        ((Stage) btnSave.getScene().getWindow()).close();
    }
}