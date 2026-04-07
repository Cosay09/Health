package com.hms.controller;

import com.hms.dao.PrescriptionDAO;
import com.hms.model.Prescription;
import com.hms.model.PrescriptionItem;
import com.hms.util.ReportGenerator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class PrescriptionController implements Initializable {

    @FXML private TableView<Prescription>            prescriptionTable;
    @FXML private TableColumn<Prescription, Integer> colId;
    @FXML private TableColumn<Prescription, Integer> colApptId;
    @FXML private TableColumn<Prescription, String>  colPatient;
    @FXML private TableColumn<Prescription, String>  colDoctor;
    @FXML private TableColumn<Prescription, String>  colDate;
    @FXML private TableColumn<Prescription, Void>    colActions;
    @FXML private TextField                          searchField;

    private final ObservableList<Prescription> prescriptionList = FXCollections.observableArrayList();
    private final PrescriptionDAO prescriptionDAO = new PrescriptionDAO();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupColumns();
        loadAll();
    }

    private void setupColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("prescriptionId"));
        colApptId.setCellValueFactory(new PropertyValueFactory<>("appointmentId"));
        colPatient.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        colDoctor.setCellValueFactory(new PropertyValueFactory<>("doctorName"));
        colDate.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getDate().toString()
                )
        );

        // Actions: View/Print + Delete
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button printBtn  = new Button("Print");
            private final Button deleteBtn = new Button("Delete");
            private final HBox   box       = new HBox(6, printBtn, deleteBtn);

            {
                printBtn.getStyleClass().add("btn-complete");
                deleteBtn.getStyleClass().add("btn-delete");

                printBtn.setOnAction(e -> {
                    Prescription p = getTableView().getItems().get(getIndex());
                    handlePrint(p);
                });
                deleteBtn.setOnAction(e -> {
                    Prescription p = getTableView().getItems().get(getIndex());
                    handleDelete(p);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        prescriptionTable.setItems(prescriptionList);
    }

    private void loadAll() {
        try {
            prescriptionList.setAll(prescriptionDAO.getAllPrescriptions());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Failed to load: " + e.getMessage());
        }
    }

    @FXML
    private void handleSearch() {
        String kw = searchField.getText().trim();
        try {
            prescriptionList.setAll(
                    kw.isEmpty() ? prescriptionDAO.getAllPrescriptions() : prescriptionDAO.search(kw)
            );
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Search failed: " + e.getMessage());
        }
    }

    @FXML private void handleAdd() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/hms/view/PrescriptionFormView.fxml")
            );
            Stage dialog = new Stage();
            dialog.setTitle("New Prescription");
            dialog.setScene(new Scene(loader.load()));
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setResizable(false);
            dialog.showAndWait();
            loadAll();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handlePrint(Prescription prescription) {
        try {
            // Load items for this prescription before printing
            List<PrescriptionItem> items =
                    prescriptionDAO.getItems(prescription.getPrescriptionId());
            prescription.setItems(items);

            // Ask user where to save the PDF
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Prescription PDF");
            fileChooser.setInitialFileName(
                    "Prescription_" + prescription.getPrescriptionId() + ".pdf"
            );
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
            );

            File file = fileChooser.showSaveDialog(
                    prescriptionTable.getScene().getWindow()
            );

            if (file != null) {
                ReportGenerator.generatePrescriptionPDF(prescription, file.getAbsolutePath());
                showAlert(Alert.AlertType.INFORMATION,
                        "PDF saved to: " + file.getAbsolutePath());
            }

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Failed to generate PDF: " + e.getMessage());
        }
    }

    private void handleDelete(Prescription p) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete prescription #" + p.getPrescriptionId() + "?");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                prescriptionDAO.deletePrescription(p.getPrescriptionId());
                prescriptionList.remove(p);
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Delete failed: " + e.getMessage());
            }
        }
    }

    private void showAlert(Alert.AlertType type, String msg) {
        new Alert(type, msg).showAndWait();
    }
}