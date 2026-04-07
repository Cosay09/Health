package com.hms.controller;

import com.hms.dao.PatientDAO;
import com.hms.model.Patient;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class PatientFormController implements Initializable {

    @FXML private Label      formTitle;
    @FXML private TextField  nameField;
    @FXML private DatePicker dobPicker;
    @FXML private ComboBox<String> genderCombo;
    @FXML private TextField  phoneField;
    @FXML private TextArea   addressArea;
    @FXML private Label      errorLabel;

    private final PatientDAO patientDAO = new PatientDAO();
    private Patient existingPatient = null; // null means "add mode"

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        genderCombo.setItems(FXCollections.observableArrayList(
                "Male", "Female", "Other"
        ));
    }

    // Called by PatientController right after the dialog loads.
    // If patient is null → add mode. If not null → edit mode.
    public void setPatient(Patient patient) {
        this.existingPatient = patient;

        if (patient == null) {
            formTitle.setText("Add New Patient");
        } else {
            formTitle.setText("Edit Patient");
            // Pre-fill all fields with existing data
            nameField.setText(patient.getName());
            dobPicker.setValue(patient.getDateOfBirth());
            genderCombo.setValue(patient.getGender());
            phoneField.setText(patient.getPhone());
            addressArea.setText(patient.getAddress());
        }
    }

    @FXML
    private void handleSave() {
        // ── Validation ───────────────────────────────────────
        String name = nameField.getText().trim();
        LocalDate dob = dobPicker.getValue();
        String gender = genderCombo.getValue();
        String phone = phoneField.getText().trim();
        String address = addressArea.getText().trim();

        if (name.isEmpty()) {
            showError("Name is required.");
            return;
        }
        if (dob == null) {
            showError("Date of birth is required.");
            return;
        }
        if (dob.isAfter(LocalDate.now())) {
            showError("Date of birth cannot be in the future.");
            return;
        }
        if (gender == null) {
            showError("Please select a gender.");
            return;
        }

        // ── Save or Update ───────────────────────────────────
        try {
            if (existingPatient == null) {
                // Add mode — patient_id is 0, DB will auto-assign it
                Patient newPatient = new Patient(0, name, dob, gender, phone, address);
                patientDAO.addPatient(newPatient);
            } else {
                // Edit mode — update the existing patient
                existingPatient.setName(name);
                existingPatient.setDateOfBirth(dob);
                existingPatient.setGender(gender);
                existingPatient.setPhone(phone);
                existingPatient.setAddress(address);
                patientDAO.updatePatient(existingPatient);
            }

            closeDialog();

        } catch (Exception e) {
            showError("Failed to save: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        closeDialog();
    }

    private void closeDialog() {
        ((Stage) nameField.getScene().getWindow()).close();
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}