package com.example.hodpital;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class AddInventoryController implements Initializable {

    @FXML private TextField txtName, txtQty, txtMinQty, txtUnit, txtPrice, txtSupplier;
    @FXML private ComboBox<String> cbCategory;
    @FXML private DatePicker dpExpiry;
    @FXML private Button btnSave;
    @FXML private Label lblHeader;

    private boolean updateMode = false;
    private String itemId;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cbCategory.getItems().addAll("Medicine", "Equipment", "Supplies");
    }

    public void setUpdateData(InventoryItem item) {
        updateMode = true;
        itemId = item.getId();
        lblHeader.setText("Edit Item");
        btnSave.setText("Update Item");

        txtName.setText(item.getName());
        cbCategory.setValue(item.getCategory());
        txtQty.setText(String.valueOf(item.getQuantity()));
        txtMinQty.setText(String.valueOf(item.getMinQuantity()));
        txtUnit.setText(item.getUnit());
        txtPrice.setText(String.valueOf(item.getPrice()));
        txtSupplier.setText(item.getSupplier());
        if(item.getExpiryDate() != null) dpExpiry.setValue(LocalDate.parse(item.getExpiryDate()));
    }

    @FXML
    private void saveAction() {
        if(txtName.getText().isEmpty() || txtQty.getText().isEmpty() || txtPrice.getText().isEmpty()) return;

        try {
            DBConnection connect = new DBConnection();
            Connection conn = connect.getConnection();
            String sql;
            PreparedStatement pstmt;

            if (updateMode) {
                sql = "UPDATE inventory SET item_name=?, category=?, quantity_in_stock=?, low_stock_threshold=?, unit=?, price=?, expiry_date=?, supplier=? WHERE item_id=?";
                pstmt = conn.prepareStatement(sql);
            } else {
                itemId = "INV" + (System.currentTimeMillis() % 10000);
                sql = "INSERT INTO inventory (item_name, category, quantity_in_stock, low_stock_threshold, unit, price, expiry_date, supplier, item_id) VALUES (?,?,?,?,?,?,?,?,?)";
                pstmt = conn.prepareStatement(sql);
            }

            pstmt.setString(1, txtName.getText());
            pstmt.setString(2, cbCategory.getValue());
            pstmt.setInt(3, Integer.parseInt(txtQty.getText()));
            pstmt.setInt(4, Integer.parseInt(txtMinQty.getText()));
            pstmt.setString(5, txtUnit.getText());
            pstmt.setDouble(6, Double.parseDouble(txtPrice.getText()));
            pstmt.setString(7, dpExpiry.getValue() != null ? dpExpiry.getValue().toString() : null);
            pstmt.setString(8, txtSupplier.getText());
            pstmt.setString(9, itemId);

            pstmt.executeUpdate();
            closeWindow();
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML private void closeWindow() { ((Stage) btnSave.getScene().getWindow()).close(); }
}