package com.hms.controller;

import com.hms.dao.MedicineDAO;
import com.hms.dao.PatientDAO;
import com.hms.dao.PharmacySaleDAO;
import com.hms.dao.PrescriptionDAO;
import com.hms.model.*;
import com.hms.util.ReportGenerator;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class PharmacyController implements Initializable {

    // Prescriptions tab
    @FXML private TextField                          presSearchField;
    @FXML private TableView<Prescription>            prescriptionTable;
    @FXML private TableColumn<Prescription, Integer> colPresId;
    @FXML private TableColumn<Prescription, Integer> colPresApptId;
    @FXML private TableColumn<Prescription, String>  colPresPatient;
    @FXML private TableColumn<Prescription, String>  colPresDoctor;
    @FXML private TableColumn<Prescription, String>  colPresDate;
    @FXML private TableColumn<Prescription, Void>    colPresActions;

    // Prescription items panel
    @FXML private TableView<PrescriptionItem>            prescriptionItemsTable;
    @FXML private TableColumn<PrescriptionItem, String>  colItemMedicine;
    @FXML private TableColumn<PrescriptionItem, String>  colItemDosage;
    @FXML private TableColumn<PrescriptionItem, Integer> colItemQty;
    @FXML private TableColumn<PrescriptionItem, String>  colItemPrice;
    @FXML private TableColumn<PrescriptionItem, String>  colItemSubtotal;

    // Sales tab
    @FXML private TextField                       saleSearchField;
    @FXML private TableView<PharmacySale>         salesTable;
    @FXML private TableColumn<PharmacySale, Integer> colSaleId;
    @FXML private TableColumn<PharmacySale, String>  colSalePatient;
    @FXML private TableColumn<PharmacySale, String>  colSaleAmount;
    @FXML private TableColumn<PharmacySale, String>  colSaleDate;
    @FXML private TableColumn<PharmacySale, Void>    colSaleActions;

    private final ObservableList<Prescription>   presList  = FXCollections.observableArrayList();
    private final ObservableList<PrescriptionItem> itemList = FXCollections.observableArrayList();
    private final ObservableList<PharmacySale>   salesList = FXCollections.observableArrayList();

    private final PrescriptionDAO prescriptionDAO = new PrescriptionDAO();
    private final PharmacySaleDAO saleDAO         = new PharmacySaleDAO();
    private final MedicineDAO     medicineDAO     = new MedicineDAO();

    // Medicine price cache — medicine_id → price
    private Map<Integer, Double> medicinePrices = new HashMap<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadMedicinePrices();
        setupPrescriptionColumns();
        setupItemsColumns();
        setupSalesColumns();
        loadAllSales();
    }

    // ── SETUP ─────────────────────────────────────────────────

    private void loadMedicinePrices() {
        try {
            medicineDAO.getAllMedicines()
                    .forEach(m -> medicinePrices.put(m.getMedicineId(), m.getPrice()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupPrescriptionColumns() {
        colPresId.setCellValueFactory(new PropertyValueFactory<>("prescriptionId"));
        colPresApptId.setCellValueFactory(new PropertyValueFactory<>("appointmentId"));
        colPresPatient.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        colPresDoctor.setCellValueFactory(new PropertyValueFactory<>("doctorName"));
        colPresDate.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getDate().toString()));

        colPresActions.setCellFactory(col -> new TableCell<>() {
            private final Button viewBtn    = new Button("View Items");
            private final Button sellBtn    = new Button("Process Sale");
            private final HBox   box        = new HBox(6, viewBtn, sellBtn);
            {
                viewBtn.getStyleClass().add("btn-edit");
                sellBtn.getStyleClass().add("btn-complete");

                viewBtn.setOnAction(e -> {
                    Prescription p = getTableView().getItems().get(getIndex());
                    loadPrescriptionItems(p);
                });
                sellBtn.setOnAction(e -> {
                    Prescription p = getTableView().getItems().get(getIndex());
                    openSaleForm(p);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        prescriptionTable.setItems(presList);

        // When a prescription row is selected, auto-load its items below
        prescriptionTable.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> {
                    if (newVal != null) loadPrescriptionItems(newVal);
                });
    }

    private void setupItemsColumns() {
        colItemMedicine.setCellValueFactory(new PropertyValueFactory<>("medicineName"));
        colItemDosage.setCellValueFactory(new PropertyValueFactory<>("dosage"));
        colItemQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colItemPrice.setCellValueFactory(data -> {
            double price = medicinePrices.getOrDefault(
                    data.getValue().getMedicineId(), 0.0);
            return new SimpleStringProperty(String.format("৳ %.2f", price));
        });
        colItemSubtotal.setCellValueFactory(data -> {
            double price = medicinePrices.getOrDefault(
                    data.getValue().getMedicineId(), 0.0);
            double subtotal = price * data.getValue().getQuantity();
            return new SimpleStringProperty(String.format("৳ %.2f", subtotal));
        });

        prescriptionItemsTable.setItems(itemList);
    }

    private void setupSalesColumns() {
        colSaleId.setCellValueFactory(new PropertyValueFactory<>("saleId"));
        colSalePatient.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        colSaleAmount.setCellValueFactory(data ->
                new SimpleStringProperty(
                        String.format("৳ %.2f", data.getValue().getTotalAmount())));
        colSaleDate.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getSaleDate() != null
                                ? data.getValue().getSaleDate().toLocalDate().toString()
                                : "—"));

        colSaleActions.setCellFactory(col -> new TableCell<>() {
            private final Button receiptBtn = new Button("Receipt");
            {
                receiptBtn.getStyleClass().add("btn-edit");
                receiptBtn.setOnAction(e -> {
                    PharmacySale s = getTableView().getItems().get(getIndex());
                    reprintReceipt(s);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : receiptBtn);
            }
        });

        salesTable.setItems(salesList);
    }

    // ── DATA ──────────────────────────────────────────────────

    private void loadPrescriptionItems(Prescription prescription) {
        try {
            List<PrescriptionItem> items =
                    prescriptionDAO.getItems(prescription.getPrescriptionId());
            itemList.setAll(items);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Could not load items: " + e.getMessage());
        }
    }

    private void loadAllSales() {
        try {
            salesList.setAll(saleDAO.getAllSales());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Could not load sales: " + e.getMessage());
        }
    }

    // ── SEARCH ────────────────────────────────────────────────

    @FXML
    private void handlePresSearch() {
        String kw = presSearchField.getText().trim();
        if (kw.isEmpty()) {
            presList.clear();
            itemList.clear();
            return;
        }
        try {
            presList.setAll(prescriptionDAO.searchForPharmacy(kw));
            itemList.clear();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Search failed: " + e.getMessage());
        }
    }

    @FXML
    private void handleClearPresSearch() {
        presSearchField.clear();
        presList.clear();
        itemList.clear();
    }

    @FXML
    private void handleSaleSearch() {
        String kw = saleSearchField.getText().trim();
        try {
            salesList.setAll(
                    kw.isEmpty()
                            ? saleDAO.getAllSales()
                            : saleDAO.searchByPatientName(kw)
            );
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Search failed: " + e.getMessage());
        }
    }

    // ── ACTIONS ───────────────────────────────────────────────

    private void openSaleForm(Prescription prescription) {
        try {
            // Load prescription items
            List<PrescriptionItem> items =
                    prescriptionDAO.getItems(prescription.getPrescriptionId());
            if (items.isEmpty()) {
                showAlert(Alert.AlertType.WARNING,
                        "This prescription has no medicine items.");
                return;
            }

            // Find the patient
            Patient patient = new PatientDAO().getAllPatients().stream()
                    .filter(p -> p.getName().equals(prescription.getPatientName()))
                    .findFirst().orElse(null);
            if (patient == null) {
                showAlert(Alert.AlertType.ERROR, "Could not find patient record.");
                return;
            }

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/hms/view/PharmacySaleFormView.fxml"));
            Stage dialog = new Stage();
            dialog.setTitle("Process Sale — " + prescription.getPatientName());
            dialog.setScene(new Scene(loader.load()));
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setResizable(false);

            PharmacySaleFormController fc = loader.getController();
            fc.setData(prescription, patient, items, medicinePrices);

            dialog.showAndWait();
            loadAllSales();
            loadMedicinePrices(); // refresh prices after stock changes

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error: " + e.getMessage());
        }
    }

    private void reprintReceipt(PharmacySale sale) {
        try {
            List<PharmacySaleItem> items = saleDAO.getItemsBySaleId(sale.getSaleId());
            sale.setItems(items);

            Patient patient = new PatientDAO().getAllPatients().stream()
                    .filter(p -> p.getPatientId() == sale.getPatientId())
                    .findFirst().orElse(null);
            if (patient == null) return;

            FileChooser fc = new FileChooser();
            fc.setTitle("Save Receipt");
            fc.setInitialFileName("Receipt_" + sale.getSaleId() + ".pdf");
            fc.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
            File file = fc.showSaveDialog(salesTable.getScene().getWindow());

            if (file != null) {
                // Fetch linked prescription if available
                Prescription prescription = null;
                if (sale.getPrescriptionId() > 0) {
                    prescription = new PrescriptionDAO()
                            .getAllPrescriptions().stream()
                            .filter(p -> p.getPrescriptionId() == sale.getPrescriptionId())
                            .findFirst().orElse(null);
                }
                ReportGenerator.generatePharmacyReceipt(
                        sale, patient, prescription, file.getAbsolutePath());
                showAlert(Alert.AlertType.INFORMATION, "Receipt saved.");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Failed: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String msg) {
        new Alert(type, msg).showAndWait();
    }
}