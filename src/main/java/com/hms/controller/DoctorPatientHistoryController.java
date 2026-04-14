package com.hms.controller;

import com.hms.dao.AppointmentDAO;
import com.hms.dao.LabTestDAO;
import com.hms.dao.PrescriptionDAO;
import com.hms.model.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class DoctorPatientHistoryController implements Initializable {

    @FXML private Label patientNameLabel;
    @FXML private Label patientInfoLabel;

    @FXML private TableView<Appointment>            appointmentTable;
    @FXML private TableColumn<Appointment, Integer> colApptId;
    @FXML private TableColumn<Appointment, String>  colApptDate;
    @FXML private TableColumn<Appointment, String>  colApptTime;
    @FXML private TableColumn<Appointment, String>  colCondition;
    @FXML private TableColumn<Appointment, String>  colApptStatus;

    @FXML private TableView<Prescription>            prescriptionTable;
    @FXML private TableColumn<Prescription, Integer> colPresId;
    @FXML private TableColumn<Prescription, String>  colPresDate;
    @FXML private TableColumn<Prescription, String>  colPresNote;

    @FXML private TableView<LabTest>            labTable;
    @FXML private TableColumn<LabTest, String>  colLabTest;
    @FXML private TableColumn<LabTest, String>  colLabResult;
    @FXML private TableColumn<LabTest, String>  colLabStatus;
    @FXML private TableColumn<LabTest, String>  colLabDate;

    private Patient patient;
    private Doctor  doctor;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupColumns();
    }

    public void setData(Patient patient, Doctor doctor) {
        this.patient = patient;
        this.doctor  = doctor;

        patientNameLabel.setText(patient.getName());
        patientInfoLabel.setText(
                "Age: " + patient.getAge() +
                        "  |  Gender: " + patient.getGender() +
                        "  |  Phone: " + patient.getPhone()
        );
        loadData();
    }

    private void setupColumns() {
        colApptId.setCellValueFactory(new PropertyValueFactory<>("appointmentId"));
        colApptDate.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getDate().toString()));
        colApptTime.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getTime().toString()));
        colCondition.setCellValueFactory(new PropertyValueFactory<>("conditionNote"));
        colApptStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        colPresId.setCellValueFactory(new PropertyValueFactory<>("prescriptionId"));
        colPresDate.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getDate().toString()));
        colPresNote.setCellValueFactory(new PropertyValueFactory<>("note"));

        colLabTest.setCellValueFactory(new PropertyValueFactory<>("testName"));
        colLabResult.setCellValueFactory(new PropertyValueFactory<>("result"));
        colLabStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colLabDate.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getOrderedAt() != null
                                ? data.getValue().getOrderedAt().toLocalDate().toString()
                                : "—"
                ));
    }

    private void loadData() {
        try {
            int pid = patient.getPatientId();
            int did = doctor.getDoctorId();

            // Appointments: only those under this doctor for this patient
            List<Appointment> appts = new AppointmentDAO()
                    .getByPatientId(pid).stream()
                    .filter(a -> a.getDoctorId() == did)
                    .collect(Collectors.toList());
            appointmentTable.setItems(FXCollections.observableArrayList(appts));

            // Prescriptions: only written by this doctor
            List<Prescription> pres = new PrescriptionDAO()
                    .getByPatientId(pid).stream()
                    .filter(p -> p.getDoctorId() == did)
                    .collect(Collectors.toList());
            prescriptionTable.setItems(FXCollections.observableArrayList(pres));

            // Lab tests: all for this patient (ordered by any doctor)
            List<LabTest> labs = new LabTestDAO().getByPatientId(pid);
            labTable.setItems(FXCollections.observableArrayList(labs));

        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR,
                    "Failed to load history: " + e.getMessage()).showAndWait();
        }
    }
}