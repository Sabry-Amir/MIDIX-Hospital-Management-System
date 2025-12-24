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
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ResourceBundle;

public class BillingController implements Initializable {

    @FXML private Label lblTotalRevenue, lblTotalCollected, lblPending;
    @FXML private TableView<Bill> table;
    @FXML private TableColumn<Bill, String> colBillID, colPatient, colDate, colStatus;
    @FXML private TableColumn<Bill, Double> colTotal, colPaid, colBalance;
    @FXML private TableColumn<Bill, String> colActions;
    @FXML private TextField searchField;

    private ObservableList<Bill> list = FXCollections.observableArrayList();
    private double xOffset = 0, yOffset = 0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupColumns();
        loadData();
        setupSearch();
        applyTableStyles();
    }

    private void setupColumns() {
        colBillID.setCellValueFactory(new PropertyValueFactory<>("billId"));
        colPatient.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        colPaid.setCellValueFactory(new PropertyValueFactory<>("paidAmount"));
        colBalance.setCellValueFactory(new PropertyValueFactory<>("balanceDue"));

        // تنسيق العملة
        colTotal.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText("$" + String.format("%.2f", item));
            }
        });
        colPaid.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText("$" + String.format("%.2f", item));
            }
        });
        colBalance.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText("$" + String.format("%.2f", item));
            }
        });

        // حالة الدفع (Badge)
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); setText(null); }
                else {
                    Label lbl = new Label(item);
                    lbl.setPrefWidth(80); lbl.setPrefHeight(25); lbl.setAlignment(Pos.CENTER);
                    String style = "-fx-font-weight: bold; -fx-background-radius: 15; -fx-font-size: 11px;";

                    if ("paid".equalsIgnoreCase(item)) lbl.setStyle(style + "-fx-background-color: #dcfce7; -fx-text-fill: #16a34a;");
                    else if ("pending".equalsIgnoreCase(item)) lbl.setStyle(style + "-fx-background-color: #fee2e2; -fx-text-fill: #dc2626;");
                    else if ("partial".equalsIgnoreCase(item)) lbl.setStyle(style + "-fx-background-color: #fef9c3; -fx-text-fill: #ca8a04;");
                    else lbl.setStyle(style + "-fx-background-color: #f3f4f6; -fx-text-fill: #374151;");

                    setGraphic(lbl); setText(null);
                }
            }
        });

        // زرار التفاصيل
        colActions.setCellFactory(param -> new TableCell<>() {
            Button viewBtn = new Button("", new FontIcon("fas-eye:13:#3b82f6"));
            {
                viewBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
                viewBtn.setOnAction(e -> openBillDetails(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : new HBox(viewBtn) {{ setAlignment(Pos.CENTER); }});
            }
        });
    }

    private void loadData() {
        list.clear();
        DBConnection connect = new DBConnection();
        // JOIN عشان نجيب اسم المريض
        String sql = "SELECT b.*, p.name AS patient_name FROM billing b JOIN patients p ON b.patient_id = p.patient_id";

        double totalRev = 0, totalCol = 0, totalPend = 0;

        try (Connection conn = connect.getConnection(); Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                double t = rs.getDouble("total_amount");
                double p = rs.getDouble("paid_amount");
                double bal = rs.getDouble("balance_due");

                totalRev += t;
                totalCol += p;
                totalPend += bal;

                list.add(new Bill(
                        rs.getString("bill_id"),
                        rs.getString("patient_id"),
                        rs.getString("patient_name"),
                        rs.getString("bill_date"),
                        t, p, bal,
                        rs.getString("status")
                ));
            }
            table.setItems(list);

            // تحديث الإحصائيات
            lblTotalRevenue.setText("$" + String.format("%.2f", totalRev));
            lblTotalCollected.setText("$" + String.format("%.2f", totalCol));
            lblPending.setText("$" + String.format("%.2f", totalPend));

        } catch (Exception e) { e.printStackTrace(); }
    }

    private void openBillDetails(Bill bill) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("BillDetails.fxml"));
            Parent root = loader.load();
            BillDetailsController ctrl = loader.getController();
            ctrl.setBill(bill);

            Stage stage = new Stage();
            stage.initStyle(StageStyle.TRANSPARENT);
            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);

            root.setOnMousePressed(e -> { xOffset = e.getSceneX(); yOffset = e.getSceneY(); });
            root.setOnMouseDragged(e -> { stage.setX(e.getScreenX() - xOffset); stage.setY(e.getScreenY() - yOffset); });

            stage.showAndWait();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void setupSearch() {
        FilteredList<Bill> filtered = new FilteredList<>(list, b -> true);
        searchField.textProperty().addListener((obs, oldV, newV) -> {
            filtered.setPredicate(b -> {
                if (newV == null || newV.isEmpty()) return true;
                String lower = newV.toLowerCase();
                return b.getPatientName().toLowerCase().contains(lower) ||
                        b.getBillId().toLowerCase().contains(lower);
            });
        });
        SortedList<Bill> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sorted);
    }

    private void applyTableStyles() {
        String css = "data:text/css,.table-view{-fx-background-color:white;-fx-border-color:transparent;}.table-view .column-header-background{-fx-background-color:#F9FAFB;-fx-border-color:#E5E7EB;-fx-border-width:0 0 1 0;}.table-view .column-header{-fx-background-color:transparent;-fx-padding:10px;}.table-view .column-header .label{-fx-font-weight:bold;-fx-text-fill:#6B7280;-fx-font-size:13px;-fx-alignment:CENTER;}.table-row-cell{-fx-background-color:white;-fx-border-color:transparent transparent #F3F4F6 transparent;-fx-border-width:0 0 1 0;-fx-padding:5px 0;}.table-row-cell:hover{-fx-background-color:#F9FAFB;}.table-cell{-fx-text-fill:#374151;-fx-font-size:13px;-fx-alignment:CENTER;-fx-border-color:transparent;}";
        table.getStylesheets().add(css);
    }
}