package com.hms.controller;

import com.hms.dao.BillingDAO;
import com.hms.dao.PatientDAO;
import com.hms.model.Billing;
import com.hms.model.Patient;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class BillingFormController implements Initializable {

    @FXML private Label              formTitle;
    @FXML private TextField          appointmentIdField;
    @FXML private ComboBox<Patient>  patientCombo;
    @FXML private TextField          amountField;
    @FXML private ComboBox<String>   statusCombo;
    @FXML private Label              errorLabel;

    private final BillingDAO billingDAO = new BillingDAO();
    private Billing existingBilling = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        statusCombo.setItems(FXCollections.observableArrayList(
                "Unpaid", "Partial", "Paid"
        ));
        statusCombo.setValue("Unpaid");
        loadPatients();
    }

    private void loadPatients() {
        try {
            List<Patient> patients = new PatientDAO().getAllPatients();
            patientCombo.setItems(FXCollections.observableArrayList(patients));
            patientCombo.setConverter(new javafx.util.StringConverter<>() {
                @Override public String toString(Patient p) {
                    return p == null ? "" : p.getName();
                }
                @Override public Patient fromString(String s) { return null; }
            });
        } catch (Exception e) {
            showError("Could not load patients.");
        }
    }

    public void setBilling(Billing billing) {
        this.existingBilling = billing;
        if (billing == null) {
            formTitle.setText("New Bill");
        } else {
            formTitle.setText("Edit Bill");
            appointmentIdField.setText(String.valueOf(billing.getAppointmentId()));
            amountField.setText(String.valueOf(billing.getTotalAmount()));
            statusCombo.setValue(billing.getStatus());

            patientCombo.getItems().stream()
                    .filter(p -> p.getPatientId() == billing.getPatientId())
                    .findFirst()
                    .ifPresent(patientCombo::setValue);
        }
    }

    @FXML
    private void handleSave() {
        String apptIdText = appointmentIdField.getText().trim();
        Patient patient   = patientCombo.getValue();
        String amountText = amountField.getText().trim();
        String status     = statusCombo.getValue();

        if (apptIdText.isEmpty()) { showError("Appointment ID is required."); return; }
        if (patient == null)      { showError("Please select a patient.");    return; }
        if (amountText.isEmpty()) { showError("Amount is required.");         return; }

        int appointmentId;
        double amount;
        try {
            appointmentId = Integer.parseInt(apptIdText);
            amount = Double.parseDouble(amountText);
        } catch (NumberFormatException e) {
            showError("Appointment ID and amount must be numbers.");
            return;
        }

        try {
            if (existingBilling == null) {
                Billing b = new Billing(0, patient.getPatientId(),
                        appointmentId, patient.getName(), amount, status, null);
                billingDAO.addBill(b);
            } else {
                existingBilling.setTotalAmount(amount);
                existingBilling.setStatus(status);
                billingDAO.updateBill(existingBilling);
            }
            closeDialog();
        } catch (Exception e) {
            showError("Failed to save: " + e.getMessage());
        }
    }

    @FXML private void handleCancel() { closeDialog(); }

    private void closeDialog() {
        ((Stage) amountField.getScene().getWindow()).close();
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
    }
}