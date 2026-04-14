package com.hms.controller;

import com.hms.dao.PharmacySaleDAO;
import com.hms.model.*;
import com.hms.util.ReportGenerator;
import com.hms.util.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.converter.IntegerStringConverter;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class PharmacySaleFormController implements Initializable {

    @FXML private Label  patientLabel;
    @FXML private Label  prescriptionLabel;
    @FXML private Label  doctorLabel;
    @FXML private Label  totalLabel;
    @FXML private Label  errorLabel;

    @FXML private TableView<SaleRow>            itemsTable;
    @FXML private TableColumn<SaleRow, String>  colMedicine;
    @FXML private TableColumn<SaleRow, String>  colDosage;
    @FXML private TableColumn<SaleRow, String>  colPrescQty;
    @FXML private TableColumn<SaleRow, Integer> colSellQty;
    @FXML private TableColumn<SaleRow, String>  colUnitPrice;
    @FXML private TableColumn<SaleRow, String>  colSubtotal;

    private final ObservableList<SaleRow> rows = FXCollections.observableArrayList();

    private Prescription prescription;
    private Patient      patient;

    // ── Inner class — one editable row in the sale form ──────
    public static class SaleRow {
        private final PrescriptionItem prescItem;
        private final double           unitPrice;
        private int                    sellQty;

        public SaleRow(PrescriptionItem prescItem, double unitPrice) {
            this.prescItem = prescItem;
            this.unitPrice = unitPrice;
            this.sellQty   = prescItem.getQuantity(); // default: sell all prescribed
        }

        public String getMedicineName() { return prescItem.getMedicineName(); }
        public String getDosage()       { return prescItem.getDosage(); }
        public int    getPrescQty()     { return prescItem.getQuantity(); }
        public int    getMedicineId()   { return prescItem.getMedicineId(); }
        public int    getSellQty()      { return sellQty; }
        public double getUnitPrice()    { return unitPrice; }
        public double getSubtotal()     { return sellQty * unitPrice; }
        public void   setSellQty(int v) { sellQty = v; }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
    }

    private void setupTable() {
        itemsTable.setEditable(true);

        colMedicine.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getMedicineName()));
        colDosage.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getDosage()));
        colPrescQty.setCellValueFactory(data ->
                new SimpleStringProperty(String.valueOf(data.getValue().getPrescQty())));
        colUnitPrice.setCellValueFactory(data ->
                new SimpleStringProperty(
                        String.format("৳ %.2f", data.getValue().getUnitPrice())));
        colSubtotal.setCellValueFactory(data ->
                new SimpleStringProperty(
                        String.format("৳ %.2f", data.getValue().getSubtotal())));

        // Sell Qty column is editable
        colSellQty.setCellValueFactory(data ->
                new javafx.beans.property.SimpleIntegerProperty(
                        data.getValue().getSellQty()).asObject());
        colSellQty.setCellFactory(TextFieldTableCell.forTableColumn(
                new IntegerStringConverter()));
        colSellQty.setOnEditCommit(event -> {
            SaleRow row = event.getRowValue();
            int newQty = event.getNewValue();
            if (newQty < 0) newQty = 0;
            if (newQty > row.getPrescQty()) newQty = row.getPrescQty();
            row.setSellQty(newQty);
            itemsTable.refresh();
            recalculateTotal();
        });

        itemsTable.setItems(rows);
    }

    // Called by PharmacyController when pharmacist clicks "Process Sale"
    public void setData(Prescription prescription, Patient patient,
                        List<PrescriptionItem> items,
                        java.util.Map<Integer, Double> medicinePrices) {
        this.prescription = prescription;
        this.patient      = patient;

        patientLabel.setText(patient.getName());
        prescriptionLabel.setText("# " + prescription.getPrescriptionId());
        doctorLabel.setText("Dr. " + prescription.getDoctorName());

        rows.clear();
        for (PrescriptionItem item : items) {
            double price = medicinePrices.getOrDefault(item.getMedicineId(), 0.0);
            rows.add(new SaleRow(item, price));
        }
        recalculateTotal();
    }

    private void recalculateTotal() {
        double total = rows.stream().mapToDouble(SaleRow::getSubtotal).sum();
        totalLabel.setText(String.format("৳ %.2f", total));
    }

    @FXML
    private void handleSave() {
        // Validate — at least one item must have qty > 0
        List<SaleRow> selling = rows.stream()
                .filter(r -> r.getSellQty() > 0)
                .toList();

        if (selling.isEmpty()) {
            showError("No medicines selected to sell.");
            return;
        }

        double total = selling.stream().mapToDouble(SaleRow::getSubtotal).sum();

        // Build sale object
        PharmacySale sale = new PharmacySale(
                0,
                patient.getPatientId(),
                prescription.getAppointmentId(),
                prescription.getPrescriptionId(),
                SessionManager.getCurrentUser().getUserId(),
                total,
                null,
                patient.getName()
        );

        // Build sale items
        List<PharmacySaleItem> saleItems = new ArrayList<>();
        for (SaleRow row : selling) {
            saleItems.add(new PharmacySaleItem(
                    0, 0,
                    row.getMedicineId(),
                    row.getMedicineName(),
                    row.getSellQty(),
                    row.getUnitPrice()
            ));
        }
        sale.setItems(saleItems);

        try {
            PharmacySaleDAO dao = new PharmacySaleDAO();
            int saleId = dao.addSale(sale);
            sale = new PharmacySale(saleId, sale.getPatientId(),
                    sale.getAppointmentId(), sale.getPrescriptionId(),
                    sale.getSoldBy(), total, null, patient.getName());
            sale.setItems(saleItems);

            // Prompt to save receipt PDF
            FileChooser fc = new FileChooser();
            fc.setTitle("Save Receipt");
            fc.setInitialFileName("Receipt_" + patient.getName() + "_" + saleId + ".pdf");
            fc.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
            File file = fc.showSaveDialog(itemsTable.getScene().getWindow());

            if (file != null) {
                ReportGenerator.generatePharmacyReceipt(
                        sale, patient, prescription, file.getAbsolutePath());
                new Alert(Alert.AlertType.INFORMATION,
                        "Sale completed. Receipt saved.").showAndWait();
            } else {
                new Alert(Alert.AlertType.INFORMATION,
                        "Sale completed successfully.").showAndWait();
            }

            closeDialog();

        }
        catch (Exception e)
        {
            e.printStackTrace();
            showError("Failed to process sale: " + e.getMessage());
        }
    }

    @FXML private void handleCancel() { closeDialog(); }

    private void closeDialog() {
        ((Stage) itemsTable.getScene().getWindow()).close();
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
    }
}