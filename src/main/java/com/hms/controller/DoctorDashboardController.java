package com.hms.controller;

import com.hms.dao.*;
import com.hms.model.*;
import com.hms.util.SessionManager;
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
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class DoctorDashboardController implements Initializable {

    @FXML private Label welcomeLabel;
    @FXML private Label specializationLabel;

    // Appointments tab
    @FXML private TableView<Appointment>            appointmentTable;
    @FXML private TableColumn<Appointment, Integer> colApptId;
    @FXML private TableColumn<Appointment, String>  colApptPatient;
    @FXML private TableColumn<Appointment, String>  colApptDate;
    @FXML private TableColumn<Appointment, String>  colApptTime;
    @FXML private TableColumn<Appointment, String>  colCondition;
    @FXML private TableColumn<Appointment, String>  colApptStatus;
    @FXML private TableColumn<Appointment, Void>    colApptActions;
    @FXML private TextField                         apptSearchField;
    @FXML private ComboBox<String>                  apptStatusFilter;

    // Patients tab
    @FXML private TableView<Patient>            patientTable;
    @FXML private TableColumn<Patient, Integer> colPatId;
    @FXML private TableColumn<Patient, String>  colPatName;
    @FXML private TableColumn<Patient, Integer> colPatAge;
    @FXML private TableColumn<Patient, String>  colPatGender;
    @FXML private TableColumn<Patient, String>  colPatPhone;
    @FXML private TableColumn<Patient, Void>    colPatActions;
    @FXML private TextField                     patientSearchField;

    // Prescriptions tab
    @FXML private TableView<Prescription>            prescriptionTable;
    @FXML private TableColumn<Prescription, Integer> colPresId;
    @FXML private TableColumn<Prescription, Integer> colPresApptId;
    @FXML private TableColumn<Prescription, String>  colPresPatient;
    @FXML private TableColumn<Prescription, String>  colPresDate;
    @FXML private TableColumn<Prescription, String>  colPresNote;
    @FXML private TableColumn<Prescription, Void>    colPresActions;
    @FXML private TextField                          presSearchField;

    private final ObservableList<Appointment>  apptList  = FXCollections.observableArrayList();
    private final ObservableList<Patient>      patList   = FXCollections.observableArrayList();
    private final ObservableList<Prescription> presList  = FXCollections.observableArrayList();

    // All loaded data — filtered locally to avoid extra DB calls
    private List<Appointment>  allAppointments;
    private List<Patient>      allPatients;
    private List<Prescription> allPrescriptions;

    private Doctor currentDoctor;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Look up this doctor's record from their user_id
        try {
            int userId = SessionManager.getCurrentUser().getUserId();
            currentDoctor = new DoctorDAO().getDoctorByUserId(userId);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Could not load doctor profile: " + e.getMessage());
            return;
        }

        if (currentDoctor == null) {
            showAlert(Alert.AlertType.WARNING,
                    "No doctor profile linked to this account. Contact admin.");
            return;
        }

        welcomeLabel.setText("Dr. " + currentDoctor.getName());
        specializationLabel.setText(currentDoctor.getSpecialization());

        apptStatusFilter.setItems(FXCollections.observableArrayList(
                "All", "Scheduled", "Completed", "Cancelled"
        ));
        apptStatusFilter.setValue("All");

        setupAppointmentColumns();
        setupPatientColumns();
        setupPrescriptionColumns();
        loadAllData();
    }

    // ── COLUMN SETUP ─────────────────────────────────────────

    private void setupAppointmentColumns() {
        colApptId.setCellValueFactory(new PropertyValueFactory<>("appointmentId"));
        colApptPatient.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        colApptDate.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getDate().toString()));
        colApptTime.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getTime().toString()));
        colCondition.setCellValueFactory(new PropertyValueFactory<>("conditionNote"));

        // Status badge
        colApptStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colApptStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) { setGraphic(null); return; }
                Label badge = new Label(status);
                badge.getStyleClass().add(switch (status) {
                    case "Completed" -> "badge-completed";
                    case "Cancelled" -> "badge-cancelled";
                    default          -> "badge-scheduled";
                });
                setGraphic(badge);
            }
        });

        // Actions: View Patient History button
        colApptActions.setCellFactory(col -> new TableCell<>() {
            private final Button historyBtn = new Button("Patient");
            {
                historyBtn.getStyleClass().add("btn-edit");
                historyBtn.setOnAction(e -> {
                    Appointment a = getTableView().getItems().get(getIndex());
                    openPatientHistoryForDoctor(a.getPatientId());
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : historyBtn);
            }
        });

        appointmentTable.setItems(apptList);
    }

    private void setupPatientColumns() {
        colPatId.setCellValueFactory(new PropertyValueFactory<>("patientId"));
        colPatName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colPatAge.setCellValueFactory(data ->
                new javafx.beans.property.SimpleIntegerProperty(
                        data.getValue().getAge()).asObject());
        colPatGender.setCellValueFactory(new PropertyValueFactory<>("gender"));
        colPatPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));

        colPatActions.setCellFactory(col -> new TableCell<>() {
            private final Button historyBtn     = new Button("History");
            private final Button prescribeBtn   = new Button("Prescribe");
            private final HBox   box            = new HBox(6, historyBtn, prescribeBtn);
            {
                historyBtn.getStyleClass().add("btn-edit");
                prescribeBtn.getStyleClass().add("btn-complete");

                historyBtn.setOnAction(e -> {
                    Patient p = getTableView().getItems().get(getIndex());
                    openPatientHistoryForDoctor(p.getPatientId());
                });
                prescribeBtn.setOnAction(e -> {
                    Patient p = getTableView().getItems().get(getIndex());
                    openPrescriptionFormForPatient(p);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        patientTable.setItems(patList);
    }

    private void setupPrescriptionColumns() {
        colPresId.setCellValueFactory(new PropertyValueFactory<>("prescriptionId"));
        colPresApptId.setCellValueFactory(new PropertyValueFactory<>("appointmentId"));
        colPresPatient.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        colPresDate.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getDate().toString()));
        colPresNote.setCellValueFactory(new PropertyValueFactory<>("note"));

        colPresActions.setCellFactory(col -> new TableCell<>() {
            private final Button printBtn = new Button("Print");
            {
                printBtn.getStyleClass().add("btn-complete");
                printBtn.setOnAction(e -> {
                    Prescription p = getTableView().getItems().get(getIndex());
                    handlePrintPrescription(p);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : printBtn);
            }
        });

        prescriptionTable.setItems(presList);
    }

    // ── DATA LOADING ─────────────────────────────────────────

    private void loadAllData() {
        try {
            allAppointments  = new AppointmentDAO().getByDoctorId(currentDoctor.getDoctorId());
            allPatients      = new PatientDAO().getPatientsByDoctorId(currentDoctor.getDoctorId());
            allPrescriptions = new PrescriptionDAO().getByDoctorId(currentDoctor.getDoctorId());

            apptList.setAll(allAppointments);
            patList.setAll(allPatients);
            presList.setAll(allPrescriptions);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Failed to load data: " + e.getMessage());
        }
    }

    // ── SEARCH & FILTER ───────────────────────────────────────

    @FXML
    private void handleApptSearch() {
        applyApptFilter();
    }

    @FXML
    private void handleStatusFilter() {
        applyApptFilter();
    }

    private void applyApptFilter() {
        String keyword = apptSearchField.getText().trim().toLowerCase();
        String status  = apptStatusFilter.getValue();

        List<Appointment> filtered = allAppointments.stream()
                .filter(a -> keyword.isEmpty() ||
                        a.getPatientName().toLowerCase().contains(keyword))
                .filter(a -> status == null || status.equals("All") ||
                        a.getStatus().equals(status))
                .collect(Collectors.toList());

        apptList.setAll(filtered);
    }

    @FXML
    private void handlePatientSearch() {
        String keyword = patientSearchField.getText().trim().toLowerCase();
        List<Patient> filtered = allPatients.stream()
                .filter(p -> keyword.isEmpty() ||
                        p.getName().toLowerCase().contains(keyword))
                .collect(Collectors.toList());
        patList.setAll(filtered);
    }

    @FXML
    private void handlePresSearch() {
        String keyword = presSearchField.getText().trim().toLowerCase();
        List<Prescription> filtered = allPrescriptions.stream()
                .filter(p -> keyword.isEmpty() ||
                        p.getPatientName().toLowerCase().contains(keyword))
                .collect(Collectors.toList());
        presList.setAll(filtered);
    }

    // ── ACTIONS ───────────────────────────────────────────────

    // Opens patient history but only shows data under this doctor
    private void openPatientHistoryForDoctor(int patientId) {
        try {
            Patient patient = new PatientDAO().getAllPatients().stream()
                    .filter(p -> p.getPatientId() == patientId)
                    .findFirst().orElse(null);
            if (patient == null) return;

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/hms/view/DoctorPatientHistoryView.fxml")
            );
            Stage stage = new Stage();
            stage.setTitle("Patient: " + patient.getName());
            stage.setScene(new Scene(loader.load()));
            stage.initModality(Modality.APPLICATION_MODAL);

            DoctorPatientHistoryController ctrl = loader.getController();
            ctrl.setData(patient, currentDoctor);

            stage.setWidth(800);
            stage.setHeight(600);
            stage.show();
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }

    // Opens prescription form pre-filled with this patient
    private void openPrescriptionFormForPatient(Patient patient) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/hms/view/PrescriptionFormView.fxml")
            );
            Stage dialog = new Stage();
            dialog.setTitle("New Prescription — " + patient.getName());
            dialog.setScene(new Scene(loader.load()));
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setResizable(false);

            PrescriptionFormController fc = loader.getController();
            fc.setPreselectedPatientAndDoctor(patient, currentDoctor);

            dialog.showAndWait();
            loadAllData();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleNewPrescription() {
        openPrescriptionFormForPatient(null);
    }

    private void handlePrintPrescription(Prescription prescription) {
        try {
            java.util.List<com.hms.model.PrescriptionItem> items =
                    new PrescriptionDAO().getItems(prescription.getPrescriptionId());
            prescription.setItems(items);

            javafx.stage.FileChooser fc = new javafx.stage.FileChooser();
            fc.setTitle("Save Prescription PDF");
            fc.setInitialFileName("Prescription_" + prescription.getPrescriptionId() + ".pdf");
            fc.getExtensionFilters().add(
                    new javafx.stage.FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

            java.io.File file = fc.showSaveDialog(prescriptionTable.getScene().getWindow());
            if (file != null) {
                com.hms.util.ReportGenerator.generatePrescriptionPDF(
                        prescription, file.getAbsolutePath());
                showAlert(Alert.AlertType.INFORMATION,
                        "Saved to: " + file.getAbsolutePath());
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Failed to generate PDF: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String msg) {
        new Alert(type, msg).showAndWait();
    }
}