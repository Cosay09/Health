package com.hms.controller;

import com.hms.dao.DoctorDAO;
import com.hms.dao.MedicineDAO;
import com.hms.dao.PrescriptionDAO;
import com.hms.model.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class PrescriptionFormController implements Initializable {

    @FXML private TextField          appointmentIdField;
    @FXML private ComboBox<Doctor>   doctorCombo;
    @FXML private DatePicker         datePicker;
    @FXML private TextArea           noteArea;
    @FXML private ComboBox<Medicine> medicineCombo;
    @FXML private TextField          dosageField;
    @FXML private TextField          quantityField;
    @FXML private TableView<PrescriptionItem>           itemsTable;
    @FXML private TableColumn<PrescriptionItem, String> colMedicine;
    @FXML private TableColumn<PrescriptionItem, String> colDosage;
    @FXML private TableColumn<PrescriptionItem, Integer> colQty;
    @FXML private TableColumn<PrescriptionItem, Void>   colRemove;
    @FXML private Label errorLabel;

    private final ObservableList<PrescriptionItem> itemList = FXCollections.observableArrayList();
    private final PrescriptionDAO prescriptionDAO = new PrescriptionDAO();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        datePicker.setValue(LocalDate.now());
        setupItemsTable();
        loadDoctors();
        loadMedicines();
    }

    private void setupItemsTable() {
        colMedicine.setCellValueFactory(new PropertyValueFactory<>("medicineName"));
        colDosage.setCellValueFactory(new PropertyValueFactory<>("dosage"));
        colQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        // Remove button per item row
        colRemove.setCellFactory(col -> new TableCell<>() {
            private final Button removeBtn = new Button("Remove");
            {
                removeBtn.getStyleClass().add("btn-delete");
                removeBtn.setOnAction(e -> {
                    PrescriptionItem item = getTableView().getItems().get(getIndex());
                    itemList.remove(item);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : removeBtn);
            }
        });

        itemsTable.setItems(itemList);
    }

    private void loadDoctors() {
        try {
            List<Doctor> doctors = new DoctorDAO().getAllDoctors();
            doctorCombo.setItems(FXCollections.observableArrayList(doctors));
            doctorCombo.setConverter(new javafx.util.StringConverter<>() {
                @Override public String toString(Doctor d) {
                    return d == null ? "" : "Dr. " + d.getName();
                }
                @Override public Doctor fromString(String s) { return null; }
            });
        } catch (Exception e) {
            showError("Could not load doctors.");
        }
    }

    private void loadMedicines() {
        try {
            List<Medicine> medicines = new MedicineDAO().getAllMedicines();
            medicineCombo.setItems(FXCollections.observableArrayList(medicines));
            // Medicine.toString() already returns "Name (unit)" — no converter needed
        } catch (Exception e) {
            showError("Could not load medicines.");
        }
    }

    // Adds a medicine row to the local items table
    @FXML
    private void handleAddMedicine() {
        Medicine medicine = medicineCombo.getValue();
        String dosage     = dosageField.getText().trim();
        String qtyText    = quantityField.getText().trim();

        if (medicine == null) { showError("Select a medicine."); return; }
        if (dosage.isEmpty()) { showError("Enter dosage instructions."); return; }
        if (qtyText.isEmpty()) { showError("Enter quantity."); return; }

        int quantity;
        try {
            quantity = Integer.parseInt(qtyText);
        } catch (NumberFormatException e) {
            showError("Quantity must be a whole number.");
            return;
        }

        if (quantity <= 0) { showError("Quantity must be at least 1."); return; }

        // Check for duplicate medicine in the list
        boolean alreadyAdded = itemList.stream()
                .anyMatch(item -> item.getMedicineId() == medicine.getMedicineId());
        if (alreadyAdded) {
            showError(medicine.getName() + " is already in the list.");
            return;
        }

        itemList.add(new PrescriptionItem(
                medicine.getMedicineId(),
                medicine.getName(),
                dosage,
                quantity
        ));

        // Clear the input fields ready for next medicine
        medicineCombo.setValue(null);
        dosageField.clear();
        quantityField.clear();
        errorLabel.setVisible(false);
    }

    @FXML
    private void handleSave() {
        String apptIdText = appointmentIdField.getText().trim();
        Doctor doctor     = doctorCombo.getValue();
        LocalDate date    = datePicker.getValue();

        if (apptIdText.isEmpty()) { showError("Appointment ID is required."); return; }
        if (doctor == null)       { showError("Please select a doctor."); return; }
        if (date == null)         { showError("Please select a date."); return; }
        if (itemList.isEmpty())   { showError("Add at least one medicine."); return; }

        int appointmentId;
        try {
            appointmentId = Integer.parseInt(apptIdText);
        } catch (NumberFormatException e) {
            showError("Appointment ID must be a number.");
            return;
        }

        try {
            Prescription p = new Prescription(
                    0, appointmentId, doctor.getDoctorId(),
                    doctor.getName(), "",
                    date, noteArea.getText().trim()
            );
            p.setItems(itemList);
            prescriptionDAO.addPrescription(p);
            closeDialog();
        } catch (Exception e) {
            showError("Failed to save: " + e.getMessage());
        }
    }

    @FXML private void handleCancel() { closeDialog(); }

    private void closeDialog() {
        ((Stage) appointmentIdField.getScene().getWindow()).close();
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
    }
}