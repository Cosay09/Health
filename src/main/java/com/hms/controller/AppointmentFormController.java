package com.hms.controller;

import com.hms.dao.AppointmentDAO;
import com.hms.dao.DoctorDAO;
import com.hms.dao.PatientDAO;
import com.hms.model.Appointment;
import com.hms.model.Doctor;
import com.hms.model.Patient;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.ResourceBundle;

public class AppointmentFormController implements Initializable {

    @FXML private Label              formTitle;
    @FXML private ComboBox<Patient>  patientCombo;
    @FXML private ComboBox<Doctor>   doctorCombo;
    @FXML private DatePicker         datePicker;
    @FXML private TextField          timeField;
    @FXML private ComboBox<String>   statusCombo;
    @FXML private Label              errorLabel;
    @FXML private TextField conditionField;
    @FXML private ComboBox<String> specializationFilter;


    private final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private Appointment existingAppointment = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        statusCombo.setItems(FXCollections.observableArrayList(
                "Scheduled", "Completed", "Cancelled"
        ));
        statusCombo.setValue("Scheduled");
        loadPatients();
        loadDoctors();
        loadSpecializations();
    }

    // Populates the patient ComboBox from DB
    private void loadPatients() {
        try {
            List<Patient> patients = new PatientDAO().getAllPatients();
            patientCombo.setItems(FXCollections.observableArrayList(patients));

            // This tells the ComboBox what text to display for each Patient object
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

    // Populates the doctor ComboBox — only available doctors
    private void loadDoctors() {
        try {
            List<Doctor> doctors = new DoctorDAO().getAvailableDoctors();
            doctorCombo.setItems(FXCollections.observableArrayList(doctors));

            doctorCombo.setConverter(new javafx.util.StringConverter<>() {
                @Override public String toString(Doctor d) {
                    return d == null ? "" : "Dr. " + d.getName() + " — " + d.getSpecialization();
                }
                @Override public Doctor fromString(String s) { return null; }
            });
        } catch (Exception e) {
            showError("Could not load doctors.");
        }
    }

    public void setAppointment(Appointment appointment) {
        this.existingAppointment = appointment;

        if (appointment == null) {
            formTitle.setText("New Appointment");
            datePicker.setValue(LocalDate.now());
        } else {
            formTitle.setText("Edit Appointment");
            datePicker.setValue(appointment.getDate());
            timeField.setText(appointment.getTime().toString());
            statusCombo.setValue(appointment.getStatus());
            conditionField.setText(appointment.getConditionNote());

            // Pre-select the right patient in the ComboBox
            patientCombo.getItems().stream()
                    .filter(p -> p.getPatientId() == appointment.getPatientId())
                    .findFirst()
                    .ifPresent(patientCombo::setValue);

            // Pre-select the right doctor
            doctorCombo.getItems().stream()
                    .filter(d -> d.getDoctorId() == appointment.getDoctorId())
                    .findFirst()
                    .ifPresent(doctorCombo::setValue);
        }
    }

    private void loadSpecializations() {
        try {
            List<Doctor> all = new DoctorDAO().getAvailableDoctors();
            List<String> specs = all.stream()
                    .map(Doctor::getSpecialization)
                    .distinct()
                    .sorted()
                    .collect(java.util.stream.Collectors.toList());
            specs.add(0, "All");
            specializationFilter.setItems(FXCollections.observableArrayList(specs));
            specializationFilter.setValue("All");

            // When specialization changes, re-filter the doctor list
            specializationFilter.setOnAction(e -> filterDoctors());
        } catch (Exception e) {
            showError("Could not load specializations.");
        }
    }

    private void filterDoctors() {
        String selected = specializationFilter.getValue();
        try {
            List<Doctor> doctors = new DoctorDAO().getAvailableDoctors();
            if (selected != null && !selected.equals("All")) {
                doctors = doctors.stream()
                        .filter(d -> d.getSpecialization().equals(selected))
                        .collect(java.util.stream.Collectors.toList());
            }
            doctorCombo.setItems(FXCollections.observableArrayList(doctors));
        } catch (Exception e) {
            showError("Could not filter doctors.");
        }
    }

    @FXML
    private void handleSave() {
        Patient selectedPatient = patientCombo.getValue();
        Doctor  selectedDoctor  = doctorCombo.getValue();
        LocalDate date          = datePicker.getValue();
        String timeText         = timeField.getText().trim();
        String status           = statusCombo.getValue();

        // Validation
        if (selectedPatient == null) { showError("Please select a patient."); return; }
        if (selectedDoctor  == null) { showError("Please select a doctor.");  return; }
        if (date == null)            { showError("Please select a date.");     return; }
        if (timeText.isEmpty())      { showError("Please enter a time.");      return; }

        LocalTime time;
        try {
            time = LocalTime.parse(timeText);
        } catch (DateTimeParseException e) {
            showError("Invalid time format. Use HH:MM (e.g. 09:30)");
            return;
        }

        try {
            if (existingAppointment == null) {
                Appointment a = new Appointment(
                        0,
                        selectedPatient.getPatientId(),
                        selectedDoctor.getDoctorId(),
                        selectedPatient.getName(),
                        selectedDoctor.getName(),
                        date, time, conditionField.getText().trim(),
                        status

                );
                appointmentDAO.addAppointment(a);
            } else {
                existingAppointment.setPatientId(selectedPatient.getPatientId());
                existingAppointment.setDoctorId(selectedDoctor.getDoctorId());
                existingAppointment.setPatientName(selectedPatient.getName());
                existingAppointment.setDoctorName(selectedDoctor.getName());
                existingAppointment.setDate(date);
                existingAppointment.setTime(time);
                existingAppointment.setConditionNote(conditionField.getText().trim());
                existingAppointment.setStatus(status);
                appointmentDAO.updateAppointment(existingAppointment);
            }
            closeDialog();
        } catch (Exception e) {
            showError("Failed to save: " + e.getMessage());
        }
    }

    @FXML private void handleCancel() { closeDialog(); }

    private void closeDialog() {
        ((Stage) timeField.getScene().getWindow()).close();
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
    }
}