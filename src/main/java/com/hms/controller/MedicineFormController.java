package com.hms.controller;

import com.hms.dao.MedicineDAO;
import com.hms.model.Medicine;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class MedicineFormController implements Initializable {

    @FXML private Label          formTitle;
    @FXML private TextField      nameField;
    @FXML private TextField      stockField;
    @FXML private ComboBox<String> unitCombo;
    @FXML private TextField      priceField;
    @FXML private Label          errorLabel;

    private final MedicineDAO medicineDAO = new MedicineDAO();
    private Medicine existingMedicine = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        unitCombo.setItems(FXCollections.observableArrayList(
                "tablet", "capsule", "ml", "mg", "vial", "strip", "bottle"
        ));
    }

    public void setMedicine(Medicine medicine) {
        this.existingMedicine = medicine;
        if (medicine == null) {
            formTitle.setText("Add Medicine");
        } else {
            formTitle.setText("Edit Medicine");
            nameField.setText(medicine.getName());
            stockField.setText(String.valueOf(medicine.getStock()));
            unitCombo.setValue(medicine.getUnit());
            priceField.setText(String.valueOf(medicine.getPrice()));
        }
    }

    @FXML
    private void handleSave() {
        String name  = nameField.getText().trim();
        String stockText = stockField.getText().trim();
        String unit  = unitCombo.getValue();
        String priceText = priceField.getText().trim();

        if (name.isEmpty())      { showError("Medicine name is required."); return; }
        if (stockText.isEmpty()) { showError("Stock quantity is required."); return; }
        if (unit == null)        { showError("Please select a unit."); return; }
        if (priceText.isEmpty()) { showError("Price is required."); return; }

        int stock;
        double price;
        try {
            stock = Integer.parseInt(stockText);
            price = Double.parseDouble(priceText);
        } catch (NumberFormatException e) {
            showError("Stock must be a whole number. Price must be a decimal.");
            return;
        }

        if (stock < 0) { showError("Stock cannot be negative."); return; }
        if (price < 0) { showError("Price cannot be negative.");  return; }

        try {
            if (existingMedicine == null) {
                medicineDAO.addMedicine(new Medicine(0, name, stock, unit, price));
            } else {
                existingMedicine.setName(name);
                existingMedicine.setStock(stock);
                existingMedicine.setUnit(unit);
                existingMedicine.setPrice(price);
                medicineDAO.updateMedicine(existingMedicine);
            }
            closeDialog();
        } catch (Exception e) {
            showError("Failed to save: " + e.getMessage());
        }
    }

    @FXML private void handleCancel() { closeDialog(); }

    private void closeDialog() {
        ((Stage) nameField.getScene().getWindow()).close();
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
    }
}