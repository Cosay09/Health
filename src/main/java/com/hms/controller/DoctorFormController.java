package com.hms.controller;

import com.hms.dao.DoctorDAO;
import com.hms.model.Doctor;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class DoctorFormController {

    @FXML private Label     formTitle;
    @FXML private TextField nameField;
    @FXML private TextField specializationField;
    @FXML private TextField phoneField;
    @FXML private CheckBox  availableCheck;
    @FXML private Label     errorLabel;

    private final DoctorDAO doctorDAO = new DoctorDAO();
    private Doctor existingDoctor = null;

    // Called by DoctorController after the dialog loads
    public void setDoctor(Doctor doctor) {
        this.existingDoctor = doctor;

        if (doctor == null) {
            formTitle.setText("Add New Doctor");
        } else {
            formTitle.setText("Edit Doctor");
            nameField.setText(doctor.getName());
            specializationField.setText(doctor.getSpecialization());
            phoneField.setText(doctor.getPhone());
            availableCheck.setSelected(doctor.isAvailable());
        }
    }

    @FXML
    private void handleSave() {
        String name           = nameField.getText().trim();
        String specialization = specializationField.getText().trim();
        String phone          = phoneField.getText().trim();
        boolean available     = availableCheck.isSelected();

        // Validation
        if (name.isEmpty()) {
            showError("Doctor name is required.");
            return;
        }
        if (specialization.isEmpty()) {
            showError("Specialization is required.");
            return;
        }

        try {
            if (existingDoctor == null) {
                Doctor newDoctor = new Doctor(0, name, specialization, phone, available);
                doctorDAO.addDoctor(newDoctor);
            } else {
                existingDoctor.setName(name);
                existingDoctor.setSpecialization(specialization);
                existingDoctor.setPhone(phone);
                existingDoctor.setAvailable(available);
                doctorDAO.updateDoctor(existingDoctor);
            }
            closeDialog();
        } catch (Exception e) {
            showError("Failed to save: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() { closeDialog(); }

    private void closeDialog() {
        ((Stage) nameField.getScene().getWindow()).close();
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}