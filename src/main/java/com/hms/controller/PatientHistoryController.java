package com.hms.controller;

import com.hms.dao.*;
import com.hms.model.*;
import com.hms.util.ReportGenerator;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class PatientHistoryController implements Initializable {

    @FXML private Label patientNameLabel;
    @FXML private Label patientInfoLabel;

    // Appointment tab
    @FXML private TableView<Appointment>            appointmentTable;
    @FXML private TableColumn<Appointment, Integer> colApptId;
    @FXML private TableColumn<Appointment, String>  colApptDate;
    @FXML private TableColumn<Appointment, String>  colApptDoctor;
    @FXML private TableColumn<Appointment, String>  colCondition;
    @FXML private TableColumn<Appointment, String>  colApptStatus;

    // Ongoing treatment tab — uses a simple string array model
    @FXML private TableView<String[]>            ongoingTable;
    @FXML private TableColumn<String[], String>  colOngoingType;
    @FXML private TableColumn<String[], String>  colOngoingDetail;
    @FXML private TableColumn<String[], String>  colOngoingDate;
    @FXML private TableColumn<String[], String>  colOngoingStatus;

    // Prescription tab
    @FXML private TableView<Prescription>            prescriptionTable;
    @FXML private TableColumn<Prescription, Integer> colPresId;
    @FXML private TableColumn<Prescription, String>  colPresDate;
    @FXML private TableColumn<Prescription, String>  colPresDoctor;
    @FXML private TableColumn<Prescription, String>  colPresNote;

    // Lab tab
    @FXML private TableView<LabTest>            labTable;
    @FXML private TableColumn<LabTest, Integer> colLabId;
    @FXML private TableColumn<LabTest, String>  colLabTest;
    @FXML private TableColumn<LabTest, String>  colLabResult;
    @FXML private TableColumn<LabTest, String>  colLabStatus;
    @FXML private TableColumn<LabTest, String>  colLabDate;

    // Billing tab
    @FXML private TableView<Billing>            billingTable;
    @FXML private TableColumn<Billing, Integer> colBillId;
    @FXML private TableColumn<Billing, Integer> colBillAppt;
    @FXML private TableColumn<Billing, String>  colBillAmount;
    @FXML private TableColumn<Billing, String>  colBillStatus;
    @FXML private TableColumn<Billing, String>  colBillDate;

    private Patient patient;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupColumns();
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
        patientNameLabel.setText(patient.getName());
        patientInfoLabel.setText(
                "Age: " + patient.getAge() +
                        "  |  Gender: " + patient.getGender() +
                        "  |  Phone: " + patient.getPhone()
        );
        loadAllData();
    }

    private void setupColumns() {
        // Appointments
        colApptId.setCellValueFactory(new PropertyValueFactory<>("appointmentId"));
        colApptDate.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getDate().toString()));
        colApptDoctor.setCellValueFactory(new PropertyValueFactory<>("doctorName"));
        colCondition.setCellValueFactory(new PropertyValueFactory<>("conditionNote"));
        colApptStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Ongoing — String[] rows: [type, detail, date, status]
        colOngoingType.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue()[0]));
        colOngoingDetail.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue()[1]));
        colOngoingDate.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue()[2]));
        colOngoingStatus.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue()[3]));

        // Prescriptions
        colPresId.setCellValueFactory(new PropertyValueFactory<>("prescriptionId"));
        colPresDate.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getDate().toString()));
        colPresDoctor.setCellValueFactory(new PropertyValueFactory<>("doctorName"));
        colPresNote.setCellValueFactory(new PropertyValueFactory<>("note"));

        // Lab tests
        colLabId.setCellValueFactory(new PropertyValueFactory<>("labId"));
        colLabTest.setCellValueFactory(new PropertyValueFactory<>("testName"));
        colLabResult.setCellValueFactory(new PropertyValueFactory<>("result"));
        colLabStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colLabDate.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getOrderedAt() != null
                                ? data.getValue().getOrderedAt().toLocalDate().toString()
                                : "—"
                ));

        // Billing
        colBillId.setCellValueFactory(new PropertyValueFactory<>("billId"));
        colBillAppt.setCellValueFactory(new PropertyValueFactory<>("appointmentId"));
        colBillAmount.setCellValueFactory(data ->
                new SimpleStringProperty(
                        String.format("৳ %.2f", data.getValue().getTotalAmount())
                ));
        colBillStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colBillDate.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getCreatedAt() != null
                                ? data.getValue().getCreatedAt().toLocalDate().toString()
                                : "—"
                ));
    }

    private void loadAllData() {
        try {
            int pid = patient.getPatientId();

            // Appointments
            List<Appointment> appointments = new AppointmentDAO().getByPatientId(pid);
            appointmentTable.setItems(FXCollections.observableArrayList(appointments));

            // Ongoing — active appointments + pending lab tests combined
            List<String[]> ongoing = new ArrayList<>();
            for (Appointment a : appointments) {
                if ("Scheduled".equals(a.getStatus())) {
                    ongoing.add(new String[]{
                            "Appointment",
                            "Dr. " + a.getDoctorName() +
                                    (a.getConditionNote() != null && !a.getConditionNote().isEmpty()
                                            ? " — " + a.getConditionNote() : ""),
                            a.getDate().toString(),
                            a.getStatus()
                    });
                }
            }
            List<LabTest> labs = new LabTestDAO().getByPatientId(pid);
            for (LabTest t : labs) {
                if ("Pending".equals(t.getStatus())) {
                    ongoing.add(new String[]{
                            "Lab Test",
                            t.getTestName(),
                            t.getOrderedAt() != null
                                    ? t.getOrderedAt().toLocalDate().toString() : "—",
                            t.getStatus()
                    });
                }
            }
            ongoingTable.setItems(FXCollections.observableArrayList(ongoing));

            // Prescriptions
            prescriptionTable.setItems(FXCollections.observableArrayList(
                    new PrescriptionDAO().getByPatientId(pid)
            ));

            // Lab tests (all, not just pending)
            labTable.setItems(FXCollections.observableArrayList(labs));

            // Billing
            billingTable.setItems(FXCollections.observableArrayList(
                    new BillingDAO().getByPatientId(pid)
            ));

        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR,
                    "Failed to load history: " + e.getMessage()).showAndWait();
        }
    }

    @FXML
    private void handleGenerateInvoice() {
        try {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Save Invoice");
            chooser.setInitialFileName("Invoice_" + patient.getName() + ".pdf");
            chooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
            );
            Stage stage = (Stage) patientNameLabel.getScene().getWindow();
            java.io.File file = chooser.showSaveDialog(stage);

            if (file != null) {
                List<Billing> bills = new BillingDAO().getByPatientId(patient.getPatientId());
                ReportGenerator.generateInvoice(patient, bills, file.getAbsolutePath());
                new Alert(Alert.AlertType.INFORMATION,
                        "Invoice saved: " + file.getAbsolutePath()).showAndWait();
            }
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR,
                    "Failed to generate invoice: " + e.getMessage()).showAndWait();
        }
    }
}