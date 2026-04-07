package com.hms.controller;

import com.hms.dao.AppointmentDAO;
import com.hms.model.Appointment;
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
import java.util.ResourceBundle;

public class AppointmentController implements Initializable {

    @FXML private TableView<Appointment>            appointmentTable;
    @FXML private TableColumn<Appointment, Integer> colId;
    @FXML private TableColumn<Appointment, String>  colPatient;
    @FXML private TableColumn<Appointment, String>  colDoctor;
    @FXML private TableColumn<Appointment, String>  colDate;
    @FXML private TableColumn<Appointment, String>  colTime;
    @FXML private TableColumn<Appointment, String>  colStatus;
    @FXML private TableColumn<Appointment, Void>    colActions;
    @FXML private TextField                         searchField;

    private final ObservableList<Appointment> appointmentList = FXCollections.observableArrayList();
    private final AppointmentDAO appointmentDAO = new AppointmentDAO();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        loadAll();
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("appointmentId"));
        colPatient.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        colDoctor.setCellValueFactory(new PropertyValueFactory<>("doctorName"));

        // Format date and time for display
        colDate.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getDate().toString()
                )
        );
        colTime.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getTime().toString()
                )
        );

        // Status badge — colour depends on value
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) { setGraphic(null); return; }
                Label badge = new Label(status);
                badge.getStyleClass().add(switch (status) {
                    case "Completed"  -> "badge-completed";
                    case "Cancelled"  -> "badge-cancelled";
                    default           -> "badge-scheduled";
                });
                setGraphic(badge);
            }
        });

        // Actions: Edit + Complete + Cancel
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn     = new Button("Edit");
            private final Button completeBtn = new Button("Complete");
            private final Button cancelBtn   = new Button("Cancel");
            private final HBox   box         = new HBox(4, editBtn, completeBtn, cancelBtn);

            {
                editBtn.getStyleClass().add("btn-edit");
                completeBtn.getStyleClass().add("btn-complete");
                cancelBtn.getStyleClass().add("btn-cancel-appt");

                editBtn.setOnAction(e -> {
                    Appointment a = getTableView().getItems().get(getIndex());
                    openForm(a);
                });
                completeBtn.setOnAction(e -> {
                    Appointment a = getTableView().getItems().get(getIndex());
                    updateStatus(a, "Completed");
                });
                cancelBtn.setOnAction(e -> {
                    Appointment a = getTableView().getItems().get(getIndex());
                    updateStatus(a, "Cancelled");
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }

                // Disable status buttons if already resolved
                Appointment a = getTableView().getItems().get(getIndex());
                boolean resolved = !a.getStatus().equals("Scheduled");
                completeBtn.setDisable(resolved);
                cancelBtn.setDisable(resolved);

                setGraphic(box);
            }
        });

        appointmentTable.setItems(appointmentList);
    }

    private void loadAll() {
        try {
            appointmentList.setAll(appointmentDAO.getAllAppointments());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Failed to load appointments: " + e.getMessage());
        }
    }

    @FXML
    private void handleSearch() {
        String kw = searchField.getText().trim();
        try {
            appointmentList.setAll(
                    kw.isEmpty() ? appointmentDAO.getAllAppointments() : appointmentDAO.search(kw)
            );
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Search failed: " + e.getMessage());
        }
    }

    @FXML private void handleAdd() { openForm(null); }

    private void updateStatus(Appointment a, String newStatus) {
        try {
            appointmentDAO.updateStatus(a.getAppointmentId(), newStatus);
            a.setStatus(newStatus);
            appointmentTable.refresh(); // refresh needed after direct property mutation
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Update failed: " + e.getMessage());
        }
    }

    private void openForm(Appointment appointment) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/hms/view/AppointmentFormView.fxml")
            );
            Stage dialog = new Stage();
            dialog.setTitle(appointment == null ? "New Appointment" : "Edit Appointment");
            dialog.setScene(new Scene(loader.load()));
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setResizable(false);

            AppointmentFormController fc = loader.getController();
            fc.setAppointment(appointment);

            dialog.showAndWait();
            loadAll();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String msg) {
        new Alert(type, msg).showAndWait();
    }
}