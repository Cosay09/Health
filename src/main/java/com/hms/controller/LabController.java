// LabController.java
package com.hms.controller;

import com.hms.dao.LabTestDAO;
import com.hms.model.LabTest;
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

public class LabController implements Initializable {

    @FXML private TableView<LabTest>            labTable;
    @FXML private TableColumn<LabTest, Integer> colId;
    @FXML private TableColumn<LabTest, Integer> colApptId;
    @FXML private TableColumn<LabTest, String>  colPatient;
    @FXML private TableColumn<LabTest, String>  colTest;
    @FXML private TableColumn<LabTest, String>  colResult;
    @FXML private TableColumn<LabTest, String>  colStatus;
    @FXML private TableColumn<LabTest, Void>    colActions;
    @FXML private TextField                     searchField;

    private final ObservableList<LabTest> labList = FXCollections.observableArrayList();
    private final LabTestDAO labDAO = new LabTestDAO();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupColumns();
        loadAll();
    }

    private void setupColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("labId"));
        colApptId.setCellValueFactory(new PropertyValueFactory<>("appointmentId"));
        colPatient.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        colTest.setCellValueFactory(new PropertyValueFactory<>("testName"));
        colResult.setCellValueFactory(new PropertyValueFactory<>("result"));

        // Status badge
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) { setGraphic(null); return; }
                Label badge = new Label(status);
                badge.getStyleClass().add(
                        status.equals("Completed") ? "badge-completed" : "badge-scheduled"
                );
                setGraphic(badge);
            }
        });

        // Actions: Enter Result + Delete
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button resultBtn = new Button("Enter Result");
            private final Button deleteBtn = new Button("Delete");
            private final HBox   box       = new HBox(6, resultBtn, deleteBtn);

            {
                resultBtn.getStyleClass().add("btn-complete");
                deleteBtn.getStyleClass().add("btn-delete");

                resultBtn.setOnAction(e -> {
                    LabTest t = getTableView().getItems().get(getIndex());
                    openResultDialog(t);
                });
                deleteBtn.setOnAction(e -> {
                    LabTest t = getTableView().getItems().get(getIndex());
                    handleDelete(t);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                LabTest t = getTableView().getItems().get(getIndex());
                resultBtn.setDisable(t.getStatus().equals("Completed"));
                setGraphic(box);
            }
        });

        labTable.setItems(labList);
    }

    private void loadAll() {
        try {
            labList.setAll(labDAO.getAllLabTests());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Failed to load lab tests: " + e.getMessage());
        }
    }

    @FXML
    private void handleSearch() {
        String kw = searchField.getText().trim();
        try {
            labList.setAll(
                    kw.isEmpty() ? labDAO.getAllLabTests() : labDAO.search(kw)
            );
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Search failed: " + e.getMessage());
        }
    }

    @FXML private void handleAdd() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/hms/view/LabFormView.fxml")
            );
            Stage dialog = new Stage();
            dialog.setTitle("Order Lab Test");
            dialog.setScene(new Scene(loader.load()));
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setResizable(false);
            dialog.showAndWait();
            loadAll();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Opens a simple text input dialog to enter the result
    private void openResultDialog(LabTest test) {
        TextInputDialog dialog = new TextInputDialog(test.getResult());
        dialog.setTitle("Enter Result");
        dialog.setHeaderText("Test: " + test.getTestName());
        dialog.setContentText("Result:");

        dialog.showAndWait().ifPresent(result -> {
            if (!result.trim().isEmpty()) {
                try {
                    labDAO.updateResult(test.getLabId(), result.trim());
                    test.setResult(result.trim());
                    test.setStatus("Completed");
                    labTable.refresh();
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Failed to save result.");
                }
            }
        });
    }

    private void handleDelete(LabTest test) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete lab test for " + test.getPatientName() + "?");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                labDAO.deleteLabTest(test.getLabId());
                labList.remove(test);
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Delete failed: " + e.getMessage());
            }
        }
    }

    private void showAlert(Alert.AlertType type, String msg) {
        new Alert(type, msg).showAndWait();
    }
}