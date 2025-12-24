package com.example.hodpital;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.*;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class MedicalRecordsController implements Initializable {

    @FXML private TableView<MedicalRecord> tblRecords;
    @FXML private TableColumn<MedicalRecord, String> colID, colPatient, colDoctor, colDate, colDiagnosis;
    @FXML private TableColumn<MedicalRecord, Void> colAction;

    private ObservableList<MedicalRecord> recordList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        loadData();
    }



    private void setupTable() {
        colID.setCellValueFactory(new PropertyValueFactory<>("recordId"));
        colPatient.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        colDoctor.setCellValueFactory(new PropertyValueFactory<>("doctorName"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("visitDate"));
        colDiagnosis.setCellValueFactory(new PropertyValueFactory<>("diagnosis"));

        colAction.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("👁 View");
            {
                btn.setStyle("-fx-background-color: #4B65BD; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 5;");
                btn.setOnAction(e -> openView(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });
    }

    private void openView(MedicalRecord record) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ViewMedicalRecord.fxml"));
            Parent root = loader.load();
            ViewMedicalRecordController controller = loader.getController();
            controller.setRecordData(record);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setResizable(false); // ممنوع التكبير عشان ميبقاش فيه Scroll
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    public void loadData() {
        recordList.clear();
        try (Connection conn = new DBConnection().getConnection();
             ResultSet rs = conn.createStatement().executeQuery("SELECT r.record_id, p.name, d.name, r.visit_date, r.diagnosis, p.blood_type FROM medical_records r JOIN patients p ON r.patient_id = p.patient_id JOIN doctors d ON r.doctor_id = d.doctor_id")) {
            while (rs.next()) {
                recordList.add(new MedicalRecord(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6)));
            }
            tblRecords.setItems(recordList);
        } catch (Exception e) { e.printStackTrace(); }
    }
}