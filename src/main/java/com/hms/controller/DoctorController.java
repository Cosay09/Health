package com.hms.controller;

import com.hms.dao.DoctorDAO;
import com.hms.model.Doctor;
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
import java.util.Optional;
import java.util.ResourceBundle;

public class DoctorController implements Initializable {

    @FXML private TableView<Doctor>            doctorTable;
    @FXML private TableColumn<Doctor, Integer> colId;
    @FXML private TableColumn<Doctor, String>  colName;
    @FXML private TableColumn<Doctor, String>  colSpecialization;
    @FXML private TableColumn<Doctor, String>  colPhone;
    @FXML private TableColumn<Doctor, Boolean> colAvailable;
    @FXML private TableColumn<Doctor, Void>    colActions;
    @FXML private TextField                    searchField;

    private final ObservableList<Doctor> doctorList = FXCollections.observableArrayList();
    private final DoctorDAO doctorDAO = new DoctorDAO();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        loadAllDoctors();
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("doctorId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colSpecialization.setCellValueFactory(new PropertyValueFactory<>("specialization"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));

        // Render available as a coloured "Yes" / "No" badge instead of true/false
        colAvailable.setCellValueFactory(new PropertyValueFactory<>("available"));
        colAvailable.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean available, boolean empty) {
                super.updateItem(available, empty);
                if (empty || available == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(available ? "Yes" : "No");
                    badge.getStyleClass().add(
                            available ? "badge-available" : "badge-unavailable"
                    );
                    setGraphic(badge);
                }
            }
        });

        // Actions column — Edit and Delete buttons per row
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn   = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox   box       = new HBox(6, editBtn, deleteBtn);

            {
                editBtn.getStyleClass().add("btn-edit");
                deleteBtn.getStyleClass().add("btn-delete");

                editBtn.setOnAction(e -> {
                    Doctor d = getTableView().getItems().get(getIndex());
                    handleEdit(d);
                });
                deleteBtn.setOnAction(e -> {
                    Doctor d = getTableView().getItems().get(getIndex());
                    handleDelete(d);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        doctorTable.setItems(doctorList);
    }

    private void loadAllDoctors() {
        try {
            doctorList.setAll(doctorDAO.getAllDoctors());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Failed to load doctors: " + e.getMessage());
        }
    }

    @FXML
    private void handleSearch() {
        String keyword = searchField.getText().trim();
        try {
            doctorList.setAll(
                    keyword.isEmpty()
                            ? doctorDAO.getAllDoctors()
                            : doctorDAO.search(keyword)
            );
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Search failed: " + e.getMessage());
        }
    }

    @FXML
    private void handleAdd() { openDoctorForm(null); }

    private void handleEdit(Doctor doctor) { openDoctorForm(doctor); }

    private void handleDelete(Doctor doctor) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete Dr. " + doctor.getName() + "?");
        confirm.setContentText("This cannot be undone.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                doctorDAO.deleteDoctor(doctor.getDoctorId());
                doctorList.remove(doctor);
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Delete failed: " + e.getMessage());
            }
        }
    }

    private void openDoctorForm(Doctor doctor) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/hms/view/DoctorFormView.fxml")
            );
            Stage dialog = new Stage();
            dialog.setTitle(doctor == null ? "Add Doctor" : "Edit Doctor");
            dialog.setScene(new Scene(loader.load()));
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setResizable(false);

            DoctorFormController formController = loader.getController();
            formController.setDoctor(doctor);

            dialog.showAndWait();
            loadAllDoctors();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String message) {
        new Alert(type, message).showAndWait();
    }
}