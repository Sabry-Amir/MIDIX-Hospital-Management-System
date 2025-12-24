package com.example.hodpital;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class BillDetailsController {

    @FXML private Label lblHeader, lblPatientName, lblPatientID, lblDate;
    @FXML private Label lblTotalLine, lblTotalSummary, lblPaidSummary, lblBalanceSummary;

    public void setBill(Bill bill) {
        lblHeader.setText("Bill Details - " + bill.getBillId());
        lblPatientName.setText(bill.getPatientName());
        lblPatientID.setText(bill.getPatientId());
        lblDate.setText(bill.getDate());

        // تعبئة الأرقام
        lblTotalLine.setText("$" + String.format("%.2f", bill.getTotalAmount()));
        lblTotalSummary.setText("$" + String.format("%.2f", bill.getTotalAmount()));
        lblPaidSummary.setText("-$" + String.format("%.2f", bill.getPaidAmount()));
        lblBalanceSummary.setText("$" + String.format("%.2f", bill.getBalanceDue()));
    }

    @FXML
    private void closeWindow() {
        ((Stage) lblHeader.getScene().getWindow()).close();
    }
}