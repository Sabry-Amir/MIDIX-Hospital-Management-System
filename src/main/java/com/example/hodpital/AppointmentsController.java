package com.example.hodpital;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ResourceBundle;

public class AppointmentsController implements Initializable {

    @FXML private TableView<Appointment> table;
    @FXML private TableColumn<Appointment, String> colID;
    @FXML private TableColumn<Appointment, String> colPatient;
    @FXML private TableColumn<Appointment, String> colDoctor;
    @FXML private TableColumn<Appointment, String> colDate;
    @FXML private TableColumn<Appointment, String> colTime;
    @FXML private TableColumn<Appointment, String> colReason;
    @FXML private TableColumn<Appointment, String> colStatus;
    @FXML private TableColumn<Appointment, String> colActions;
    @FXML private TextField searchField;

    private ObservableList<Appointment> list = FXCollections.observableArrayList();
    private double xOffset = 0, yOffset = 0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupColumns();
        loadData();
        setupSearch();
        applyTableStyles();
    }

    private void setupColumns() {
        colID.setCellValueFactory(new PropertyValueFactory<>("id"));
        colPatient.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        colDoctor.setCellValueFactory(new PropertyValueFactory<>("doctorName"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colTime.setCellValueFactory(new PropertyValueFactory<>("time"));
        colReason.setCellValueFactory(new PropertyValueFactory<>("reason"));

        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); setText(null); }
                else {
                    Label lbl = new Label(item);
                    lbl.setPrefWidth(90); lbl.setPrefHeight(25); lbl.setAlignment(Pos.CENTER);
                    String style = "-fx-font-weight: bold; -fx-background-radius: 15; -fx-font-size: 11px;";
                    if ("confirmed".equalsIgnoreCase(item)) lbl.setStyle(style + "-fx-background-color: #dcfce7; -fx-text-fill: #16a34a;");
                    else if ("completed".equalsIgnoreCase(item)) lbl.setStyle(style + "-fx-background-color: #dbeafe; -fx-text-fill: #2563eb;");
                    else if ("cancelled".equalsIgnoreCase(item)) lbl.setStyle(style + "-fx-background-color: #fee2e2; -fx-text-fill: #ef4444;");
                    else lbl.setStyle(style + "-fx-background-color: #e0e7ff; -fx-text-fill: #4338ca;");
                    setGraphic(lbl); setText(null);
                }
            }
        });

        colActions.setCellFactory(param -> new TableCell<>() {
            Button editBtn = new Button("", new FontIcon("fas-pen:13:#3b82f6"));
            Button deleteBtn = new Button("", new FontIcon("fas-trash:13:#ef4444"));
            {
                editBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
                deleteBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
                editBtn.setOnAction(e -> openWindow(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : new HBox(5, editBtn, deleteBtn) {{ setAlignment(Pos.CENTER); }});
            }
        });
    }

    private void loadData() {
        System.out.println(">>> STARTING LOAD DATA (DEBUG MODE) <<<");

        // 1. التأكد إن الجدول مربوط بالملف
        if (table == null) {
            System.err.println("❌ ERROR: TableView 'table' is NULL! Check fx:id in SceneBuilder.");
            return;
        }

        list.clear();
        DBConnection connect = new DBConnection();

        // جملة الاستعلام (لاحظ إننا بنطبعها عشان نتأكد منها)
        String sql = "SELECT a.*, p.name AS patient_name, d.name AS doctor_name " +
                "FROM appointments a " +
                "LEFT JOIN patients p ON a.patient_id = p.patient_id " +
                "LEFT JOIN doctors d ON a.doctor_id = d.doctor_id";

        try (Connection conn = connect.getConnection()) {
            if (conn == null) {
                System.err.println("❌ ERROR: Database Connection failed!");
                return;
            }

            System.out.println("✅ Database Connected.");
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            int counter = 0;
            while (rs.next()) {
                counter++;
                // قراءة البيانات في متغيرات الأول عشان لو فيه Error نعرف هو فين
                String aId = rs.getString("appointment_id");
                String pName = rs.getString("patient_name");
                String dName = rs.getString("doctor_name");

                System.out.println("   -> Row " + counter + ": Found ID=" + aId + ", Patient=" + pName + ", Doctor=" + dName);

                list.add(new Appointment(
                        aId,
                        rs.getString("patient_id"),
                        pName == null ? "Unknown" : pName, // حماية من الـ Null
                        rs.getInt("doctor_id"),
                        dName == null ? "Unknown" : dName, // حماية من الـ Null
                        rs.getString("appointment_date"),
                        rs.getString("appointment_time"),
                        rs.getString("reason"),
                        rs.getString("status"),
                        rs.getString("notes")
                ));
            }

            System.out.println(">>> TOTAL LOADED: " + counter + " records.");
            table.setItems(list);

            if (counter > 0) {
                System.out.println("✅ Data should be visible now.");
            } else {
                System.err.println("⚠️ Warning: Query returned 0 rows. Table is empty because DB is empty.");
            }

        } catch (Exception e) {
            System.err.println("❌ EXCEPTION in loadData:");
            e.printStackTrace();
        }
    }

    @FXML public void openAddWindow() { openWindow(null); }
    private void openWindow(Appointment apt) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("AddAppointment.fxml"));
            Parent root = loader.load();
            AddAppointmentController ctrl = loader.getController();
            if (apt != null) ctrl.setUpdateData(apt);

            Stage stage = new Stage();
            stage.initStyle(StageStyle.TRANSPARENT);
            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);

            root.setOnMousePressed(e -> { xOffset = e.getSceneX(); yOffset = e.getSceneY(); });
            root.setOnMouseDragged(e -> { stage.setX(e.getScreenX() - xOffset); stage.setY(e.getScreenY() - yOffset); });

            stage.showAndWait();
            loadData();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void handleDelete(Appointment apt) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Delete Appointment " + apt.getId() + "?");
        alert.initStyle(StageStyle.UNDECORATED);
        alert.setHeaderText(null);
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-border-color: #4f46e5; -fx-border-width: 2px; -fx-background-radius: 8; -fx-border-radius: 8; -fx-background-color: white;");

        if (alert.showAndWait().get() == ButtonType.OK) {
            DBConnection connect = new DBConnection();
            try (Connection conn = connect.getConnection(); PreparedStatement pstmt = conn.prepareStatement("DELETE FROM appointments WHERE appointment_id = ?")) {
                pstmt.setString(1, apt.getId());
                pstmt.executeUpdate();
                loadData();
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    private void setupSearch() {
        FilteredList<Appointment> filtered = new FilteredList<>(list, b -> true);
        searchField.textProperty().addListener((obs, oldV, newV) -> {
            filtered.setPredicate(p -> {
                if (newV == null || newV.isEmpty()) return true;
                String lower = newV.toLowerCase();
                return p.getPatientName().toLowerCase().contains(lower) ||
                        p.getDoctorName().toLowerCase().contains(lower) ||
                        p.getId().toLowerCase().contains(lower);
            });
        });
        SortedList<Appointment> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sorted);
    }

    private void applyTableStyles() {
        String css = "data:text/css,.table-view{-fx-background-color:white;-fx-border-color:transparent;}.table-view .column-header-background{-fx-background-color:#F9FAFB;-fx-border-color:#E5E7EB;-fx-border-width:0 0 1 0;}.table-view .column-header{-fx-background-color:transparent;-fx-padding:10px;}.table-view .column-header .label{-fx-font-weight:bold;-fx-text-fill:#6B7280;-fx-font-size:13px;-fx-alignment:CENTER;}.table-row-cell{-fx-background-color:white;-fx-border-color:transparent transparent #F3F4F6 transparent;-fx-border-width:0 0 1 0;-fx-padding:5px 0;}.table-row-cell:hover{-fx-background-color:#F9FAFB;}.table-cell{-fx-text-fill:#374151;-fx-font-size:13px;-fx-alignment:CENTER;-fx-border-color:transparent;}";
        table.getStylesheets().add(css);
    }
}