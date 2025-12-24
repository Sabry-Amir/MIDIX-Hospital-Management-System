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

public class PatientsController implements Initializable {

    @FXML private TableView<Patient> patientsTable;
    @FXML private TableColumn<Patient, String> colID;
    @FXML private TableColumn<Patient, String> colName;
    @FXML private TableColumn<Patient, Integer> colAge;
    @FXML private TableColumn<Patient, String> colGender;
    @FXML private TableColumn<Patient, String> colPhone;
    @FXML private TableColumn<Patient, String> colBlood;
    @FXML private TableColumn<Patient, String> colStatus;
    @FXML private TableColumn<Patient, String> colActions;
    @FXML private TextField searchField;

    private ObservableList<Patient> list = FXCollections.observableArrayList();
    private double xOffset = 0, yOffset = 0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupColumns();
        loadData(); // هنا هيبدأ يطبعلك تفاصيل التحميل في الكونسول
        setupSearch();
        applyTableStyles();
    }

    private void setupColumns() {
        colID.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colAge.setCellValueFactory(new PropertyValueFactory<>("age"));
        colGender.setCellValueFactory(new PropertyValueFactory<>("gender"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colBlood.setCellValueFactory(new PropertyValueFactory<>("bloodType"));

        // تلوين الحالة
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); setText(null); }
                else {
                    Label lbl = new Label(item);
                    lbl.setPrefWidth(90); lbl.setPrefHeight(25); lbl.setAlignment(Pos.CENTER);
                    String style = "-fx-font-weight: bold; -fx-background-radius: 15; -fx-font-size: 11px;";

                    if ("active".equalsIgnoreCase(item)) lbl.setStyle(style + "-fx-background-color: #dbeafe; -fx-text-fill: #2563eb;");
                    else if ("admitted".equalsIgnoreCase(item)) lbl.setStyle(style + "-fx-background-color: #dcfce7; -fx-text-fill: #16a34a;");
                    else lbl.setStyle(style + "-fx-background-color: #f3f4f6; -fx-text-fill: #4b5563;");

                    setGraphic(lbl); setText(null);
                }
            }
        });

        // الأزرار
        colActions.setCellFactory(param -> new TableCell<>() {
            Button editBtn = new Button("", new FontIcon("fas-pen:13:#3b82f6"));
            Button deleteBtn = new Button("", new FontIcon("fas-trash:13:#ef4444"));
            {
                editBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
                deleteBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
                editBtn.setOnAction(e -> handleEdit(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : new HBox(5, editBtn, deleteBtn) {{ setAlignment(Pos.CENTER); }});
            }
        });
    }

    // ==========================================
    // دالة تحميل البيانات (المعدلة لاكتشاف الأخطاء)
    // ==========================================
    private void loadData() {
        System.out.println("=== STARTING DATA LOAD ==="); // علامة بداية

        list.clear();
        DBConnection connect = new DBConnection();

        try (Connection conn = connect.getConnection()) {
            if (conn == null) {
                System.out.println("❌ ERROR: Connection is NULL. Check DBConnection.java.");
                return;
            }
            System.out.println("✅ Database Connected.");

            Statement stmt = conn.createStatement();

            // تنفيذ الاستعلام
            System.out.println("⏳ Executing Query: SELECT * FROM patients");
            ResultSet rs = stmt.executeQuery("SELECT * FROM patients");

            int count = 0;
            while (rs.next()) {
                count++;
                String id = rs.getString("patient_id");
                String name = rs.getString("name");
                System.out.println("   -> Found Patient: " + id + " - " + name);

                // التأكد من أسماء الأعمدة (ممكن يكون فيه عمود ناقص في الداتابيز عندك)
                // لو حصل Error هنا، معناه إن الجدول في الداتابيز ناقص أعمدة
                list.add(new Patient(
                        id,
                        name,
                        rs.getInt("age"),
                        rs.getString("gender"),
                        rs.getString("phone"),
                        rs.getString("blood_type"),
                        rs.getString("status"),
                        rs.getString("address"), // تأكد إن العمود ده موجود في الداتابيز
                        rs.getString("emergency_contact"), // وده كمان
                        rs.getString("emergency_phone"), // وده كمان
                        rs.getString("allergies"),
                        rs.getString("date_of_birth")  // وده كمان
                ));
            }

            System.out.println("✅ Total Patients Loaded: " + count);
            patientsTable.setItems(list);

        } catch (Exception e) {
            System.out.println("❌ EXCEPTION OCCURRED DURING LOAD DATA:");
            e.printStackTrace(); // ده هيطبعلك الخطأ بالتفصيل في الكونسول

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Database Error");
            alert.setContentText("Error loading data: " + e.getMessage());
            alert.show();
        }
    }

    @FXML public void openAddPatientWindow() { openWindow(null); }
    private void handleEdit(Patient p) { openWindow(p); }

    private void openWindow(Patient p) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("AddPatient.fxml"));
            Parent root = loader.load();
            AddPatientController ctrl = loader.getController();
            if (p != null) ctrl.setUpdateData(p);

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

    private void handleDelete(Patient p) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Delete " + p.getName() + "?");
        alert.initStyle(StageStyle.UNDECORATED);
        alert.setHeaderText(null);
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-border-color: #4f46e5; -fx-border-width: 2px; -fx-background-radius: 8; -fx-border-radius: 8; -fx-background-color: white;");

        if (alert.showAndWait().get() == ButtonType.OK) {
            DBConnection connect = new DBConnection();
            try (Connection conn = connect.getConnection(); PreparedStatement pstmt = conn.prepareStatement("DELETE FROM patients WHERE patient_id = ?")) {
                pstmt.setString(1, p.getId());
                pstmt.executeUpdate();
                loadData();
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    private void setupSearch() {
        FilteredList<Patient> filtered = new FilteredList<>(list, b -> true);
        searchField.textProperty().addListener((obs, oldV, newV) -> {
            filtered.setPredicate(p -> {
                if (newV == null || newV.isEmpty()) return true;
                String lower = newV.toLowerCase();
                return p.getName().toLowerCase().contains(lower) ||
                        p.getPhone().contains(lower) ||
                        p.getId().toLowerCase().contains(lower);
            });
        });
        SortedList<Patient> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(patientsTable.comparatorProperty());
        patientsTable.setItems(sorted);
    }

    private void applyTableStyles() {
        String css = "data:text/css," +
                // 1. إعدادات الجدول العامة
                ".table-view { -fx-background-color: white; -fx-border-color: transparent; } " +

                // 2. خلفية الهيدر
                ".table-view .column-header-background { -fx-background-color: #F9FAFB; -fx-border-color: #E5E7EB; -fx-border-width: 0 0 1 0; } " +

                // 3. ضبط الهيدر نفسه (العناوين)
                ".table-view .column-header { -fx-background-color: transparent; -fx-padding: 10px; } " +
                // أهم سطر: محاذاة عنوان العمود في المنتصف (CENTER)
                ".table-view .column-header .label { -fx-font-weight: bold; -fx-text-fill: #6B7280; -fx-font-size: 13px; -fx-alignment: CENTER; } " +

                // 4. ضبط الصفوف
                ".table-row-cell { -fx-background-color: white; -fx-border-color: transparent transparent #F3F4F6 transparent; -fx-border-width: 0 0 1 0; -fx-padding: 5px 0; } " +
                ".table-row-cell:hover { -fx-background-color: #F9FAFB; } " +

                // 5. ضبط الخلايا (البيانات)
                // أهم سطر: محاذاة البيانات في المنتصف (CENTER) عشان تيجي تحت العنوان بالظبط
                ".table-cell { -fx-text-fill: #374151; -fx-font-size: 13px; -fx-alignment: CENTER; -fx-border-color: transparent; }";

        patientsTable.getStylesheets().add(css);
    }
}