// LabFormController.java
package com.hms.controller;

import com.hms.dao.LabTestDAO;
import com.hms.dao.PatientDAO;
import com.hms.model.LabTest;
import com.hms.model.Patient;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class LabFormController implements Initializable {

    @FXML private TextField         appointmentIdField;
    @FXML private ComboBox<Patient> patientCombo;
    @FXML private TextField         testNameField;
    @FXML private Label             errorLabel;

    private final LabTestDAO labDAO = new LabTestDAO();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
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

    @FXML
    private void handleSave() {
        String apptIdText = appointmentIdField.getText().trim();
        Patient patient   = patientCombo.getValue();
        String testName   = testNameField.getText().trim();

        if (apptIdText.isEmpty()) { showError("Appointment ID is required."); return; }
        if (patient == null)      { showError("Please select a patient.");    return; }
        if (testName.isEmpty())   { showError("Test name is required.");      return; }

        int appointmentId;
        try {
            appointmentId = Integer.parseInt(apptIdText);
        } catch (NumberFormatException e) {
            showError("Appointment ID must be a number.");
            return;
        }

        try {
            LabTest t = new LabTest(
                    0, appointmentId, patient.getPatientId(),
                    patient.getName(), testName, "", "Pending", null
            );
            labDAO.addLabTest(t);
            closeDialog();
        } catch (Exception e) {
            showError("Failed to save: " + e.getMessage());
        }
    }

    @FXML private void handleCancel() { closeDialog(); }

    private void closeDialog() {
        ((Stage) testNameField.getScene().getWindow()).close();
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
    }
}