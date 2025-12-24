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
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
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
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class InventoryController implements Initializable {

    @FXML private HBox warningContainer;
    @FXML private Label lblWarningCount, lblWarningItems;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> cbCategoryFilter;

    @FXML private TableView<InventoryItem> table;
    @FXML private TableColumn<InventoryItem, String> colID, colName, colCategory, colUnit, colExpiry, colSupplier, colActions;
    @FXML private TableColumn<InventoryItem, String> colQuantity;
    @FXML private TableColumn<InventoryItem, Double> colPrice;

    private ObservableList<InventoryItem> list = FXCollections.observableArrayList();
    private double xOffset = 0, yOffset = 0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupColumns();
        setupRowFactory(); // دالة تلوين الصف بالكامل
        cbCategoryFilter.getItems().addAll("All Categories", "Medicine", "Equipment", "Supplies");
        loadData();
        setupSearch();
        applyTableStyles();
    }

    // 1. إعداد الأعمدة وتلوين الخلية
    private void setupColumns() {
        colID.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colUnit.setCellValueFactory(new PropertyValueFactory<>("unit"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colExpiry.setCellValueFactory(new PropertyValueFactory<>("expiryDate"));
        colSupplier.setCellValueFactory(new PropertyValueFactory<>("supplier"));

        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colCategory.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); setText(null); }
                else {
                    Label lbl = new Label(item);
                    lbl.setPadding(new javafx.geometry.Insets(3, 10, 3, 10));
                    lbl.setStyle("-fx-font-weight: bold; -fx-background-radius: 15; -fx-font-size: 11px;");
                    if (item.equalsIgnoreCase("medicine")) lbl.setStyle(lbl.getStyle() + "-fx-background-color: #dbeafe; -fx-text-fill: #2563eb;");
                    else if (item.equalsIgnoreCase("equipment")) lbl.setStyle(lbl.getStyle() + "-fx-background-color: #dcfce7; -fx-text-fill: #16a34a;");
                    else lbl.setStyle(lbl.getStyle() + "-fx-background-color: #f3f4f6; -fx-text-fill: #374151;");
                    setGraphic(lbl); setText(null);
                }
            }
        });

        // تلوين نص الكمية بالأحمر في حالة النقص
        colQuantity.setCellValueFactory(cell ->
                new javafx.beans.property.SimpleStringProperty(cell.getValue().getQuantity() + " / " + cell.getValue().getMinQuantity()));
        colQuantity.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); }
                else {
                    setText(item);
                    InventoryItem row = getTableView().getItems().get(getIndex());
                    if (row.isLowStock()) {
                        setTextFill(Color.RED);
                        setStyle("-fx-font-weight: bold; -fx-alignment: CENTER;");
                    } else {
                        setTextFill(Color.web("#374151"));
                        setStyle("-fx-alignment: CENTER;");
                    }
                }
            }
        });

        colActions.setCellFactory(param -> new TableCell<>() {
            Button editBtn = new Button("", new FontIcon("fas-pen:13:#3b82f6"));
            Button deleteBtn = new Button("", new FontIcon("fas-trash:13:#ef4444"));
            {
                editBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
                deleteBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
                deleteBtn.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));
                editBtn.setOnAction(e -> openAddWindow(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : new HBox(5, editBtn, deleteBtn) {{ setAlignment(Pos.CENTER); }});
            }
        });
    }

    // 2. تلوين الصف بالكامل بالأحمر الخفيف لو عليه تحذير
    private void setupRowFactory() {
        table.setRowFactory(tv -> new TableRow<InventoryItem>() {
            @Override
            protected void updateItem(InventoryItem item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle("");
                } else if (item.isLowStock()) {
                    // خلفية حمراء خفيفة مع حدود سفلية حمراء
                    setStyle("-fx-background-color: #fff1f2; -fx-border-color: #fecaca; -fx-border-width: 0 0 1 0;");
                } else {
                    setStyle(""); // الصف العادي
                }
            }
        });
    }

    private void loadData() {
        list.clear();
        List<String> lowStockItems = new ArrayList<>();
        DBConnection connect = new DBConnection();
        try (Connection conn = connect.getConnection(); Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT * FROM inventory");
            while (rs.next()) {
                InventoryItem item = new InventoryItem(
                        rs.getString("item_id"), rs.getString("item_name"), rs.getString("category"),
                        rs.getInt("quantity_in_stock"), rs.getInt("low_stock_threshold"),
                        rs.getString("unit"), rs.getDouble("price"), rs.getString("expiry_date"), rs.getString("supplier")
                );
                list.add(item);
                if (item.isLowStock()) lowStockItems.add(item.getName());
            }
            table.setItems(list);

            if (lowStockItems.isEmpty()) {
                warningContainer.setVisible(false);
                warningContainer.setManaged(false);
            } else {
                warningContainer.setVisible(true);
                warningContainer.setManaged(true);
                lblWarningCount.setText(lowStockItems.size() + " items are running low on stock!");
                lblWarningItems.setText(String.join(", ", lowStockItems));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML public void openAddWindow() { openAddWindow(null); }

    private void openAddWindow(InventoryItem item) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("AddInventory.fxml"));
            Parent root = loader.load();
            AddInventoryController ctrl = loader.getController();
            if (item != null) ctrl.setUpdateData(item);
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

    private void handleDelete(InventoryItem item) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Delete " + item.getName() + "?");
        if (alert.showAndWait().get() == ButtonType.OK) {
            try (Connection conn = new DBConnection().getConnection(); PreparedStatement pstmt = conn.prepareStatement("DELETE FROM inventory WHERE item_id = ?")) {
                pstmt.setString(1, item.getId());
                pstmt.executeUpdate();
                loadData();
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    private void setupSearch() {
        FilteredList<InventoryItem> filtered = new FilteredList<>(list, b -> true);
        searchField.textProperty().addListener((obs, oldV, newV) -> updateFilter(filtered));
        cbCategoryFilter.valueProperty().addListener((obs, oldV, newV) -> updateFilter(filtered));
        SortedList<InventoryItem> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sorted);
    }

    private void updateFilter(FilteredList<InventoryItem> filtered) {
        String searchText = searchField.getText() == null ? "" : searchField.getText().toLowerCase();
        String category = cbCategoryFilter.getValue();
        filtered.setPredicate(item -> {
            boolean matchSearch = item.getName().toLowerCase().contains(searchText) || item.getId().toLowerCase().contains(searchText);
            boolean matchCat = category == null || category.equals("All Categories") || item.getCategory().equalsIgnoreCase(category);
            return matchSearch && matchCat;
        });
    }

    private void applyTableStyles() {
        String css = "data:text/css,.table-view{-fx-background-color:white;-fx-border-color:transparent;}.table-view .column-header-background{-fx-background-color:#F9FAFB;-fx-border-color:#E5E7EB;-fx-border-width:0 0 1 0;}.table-view .column-header{-fx-background-color:transparent;-fx-padding:10px;}.table-view .column-header .label{-fx-font-weight:bold;-fx-text-fill:#6B7280;-fx-font-size:12px;-fx-alignment:CENTER;}.table-row-cell{-fx-background-color:white;-fx-border-color:transparent transparent #F3F4F6 transparent;-fx-border-width:0 0 1 0;-fx-padding:5px 0;}.table-cell{-fx-text-fill:#374151;-fx-font-size:13px;-fx-alignment:CENTER;-fx-border-color:transparent;}";
        table.getStylesheets().add(css);
    }
}