package com.hms.controller;

import com.hms.dao.PatientDAO;
import com.hms.model.Patient;
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
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class PatientController implements Initializable {

    @FXML private TableView<Patient>            patientTable;
    @FXML private TableColumn<Patient, Integer> colId;
    @FXML private TableColumn<Patient, String>  colName;
    @FXML private TableColumn<Patient, Integer> colAge;
    @FXML private TableColumn<Patient, String>  colGender;
    @FXML private TableColumn<Patient, String>  colPhone;
    @FXML private TableColumn<Patient, Void>    colActions;
    @FXML private TextField                     searchField;

    // ObservableList is the live data source for the table
    private final ObservableList<Patient> patientList = FXCollections.observableArrayList();
    private final PatientDAO patientDAO = new PatientDAO();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        loadAllPatients();
    }

    // ── COLUMN SETUP ─────────────────────────────────────────
    private void setupTableColumns() {

        // PropertyValueFactory links a column to a property name in your model.
        // "patientId" → calls patientIdProperty() on each Patient object
        colId.setCellValueFactory(new PropertyValueFactory<>("patientId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colGender.setCellValueFactory(new PropertyValueFactory<>("gender"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));

        // Age is calculated, not a stored property — use a custom cell factory
        colAge.setCellValueFactory(data ->
                new javafx.beans.property.SimpleIntegerProperty(
                        data.getValue().getAge()
                ).asObject()
        );

        // Actions column — buttons can't be done via PropertyValueFactory,
        // so we build them manually for each row
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn   = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox   box       = new HBox(6, editBtn, deleteBtn);

            {
                // This block runs once when the cell is created
                editBtn.getStyleClass().add("btn-edit");
                deleteBtn.getStyleClass().add("btn-delete");

                editBtn.setOnAction(e -> {
                    Patient p = getTableView().getItems().get(getIndex());
                    handleEdit(p);
                });

                deleteBtn.setOnAction(e -> {
                    Patient p = getTableView().getItems().get(getIndex());
                    handleDelete(p);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                // Only show buttons on real rows, not empty ones
                setGraphic(empty ? null : box);
            }
        });

        // Give the table its data source
        patientTable.setItems(patientList);
    }

    // ── LOAD DATA ────────────────────────────────────────────
    private void loadAllPatients() {
        try {
            List<Patient> patients = patientDAO.getAllPatients();
            patientList.setAll(patients); // replaces list contents, table auto-refreshes
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Failed to load patients: " + e.getMessage());
        }
    }

    // ── SEARCH ───────────────────────────────────────────────
    @FXML
    private void handleSearch() {
        String keyword = searchField.getText().trim();
        try {
            List<Patient> results = keyword.isEmpty()
                    ? patientDAO.getAllPatients()
                    : patientDAO.searchByName(keyword);
            patientList.setAll(results);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Search failed: " + e.getMessage());
        }
    }

    // ── ADD ──────────────────────────────────────────────────
    @FXML
    private void handleAdd() {
        // Open the form dialog with no patient (means "add mode")
        openPatientForm(null);
    }

    // ── EDIT ─────────────────────────────────────────────────
    private void handleEdit(Patient patient) {
        // Open the form dialog with an existing patient (means "edit mode")
        openPatientForm(patient);
    }

    // ── DELETE ───────────────────────────────────────────────
    private void handleDelete(Patient patient) {
        // Always confirm before deleting
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete " + patient.getName() + "?");
        confirm.setContentText("This cannot be undone.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                patientDAO.deletePatient(patient.getPatientId());
                patientList.remove(patient); // removes from list → table updates instantly
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Delete failed: " + e.getMessage());
            }
        }
    }

    // ── OPEN FORM DIALOG ─────────────────────────────────────
    private void openPatientForm(Patient patient) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/hms/view/PatientFormView.fxml")
            );

            Stage dialog = new Stage();
            dialog.setTitle(patient == null ? "Add Patient" : "Edit Patient");
            dialog.setScene(new Scene(loader.load()));

            // Modality.APPLICATION_MODAL blocks the main window until dialog closes
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setResizable(false);

            // Pass the patient (or null) to the form controller
            PatientFormController formController = loader.getController();
            formController.setPatient(patient);

            // Wait here until the dialog is closed
            dialog.showAndWait();

            // After dialog closes, refresh the table
            loadAllPatients();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ── HELPER ───────────────────────────────────────────────
    private void showAlert(Alert.AlertType type, String message) {
        new Alert(type, message).showAndWait();
    }
}