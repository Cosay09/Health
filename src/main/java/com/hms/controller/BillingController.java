package com.hms.controller;

import com.hms.dao.BillingDAO;
import com.hms.model.Billing;
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

public class BillingController implements Initializable {

    @FXML private TableView<Billing>            billingTable;
    @FXML private TableColumn<Billing, Integer> colId;
    @FXML private TableColumn<Billing, Integer> colApptId;
    @FXML private TableColumn<Billing, String>  colPatient;
    @FXML private TableColumn<Billing, Double>  colAmount;
    @FXML private TableColumn<Billing, String>  colStatus;
    @FXML private TableColumn<Billing, Void>    colActions;
    @FXML private TextField                     searchField;

    private final ObservableList<Billing> billingList = FXCollections.observableArrayList();
    private final BillingDAO billingDAO = new BillingDAO();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupColumns();
        loadAll();
    }

    private void setupColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("billId"));
        colApptId.setCellValueFactory(new PropertyValueFactory<>("appointmentId"));
        colPatient.setCellValueFactory(new PropertyValueFactory<>("patientName"));

        // Format amount with currency symbol
        colAmount.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        colAmount.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);
                setText(empty || amount == null ? null : String.format("৳ %.2f", amount));
            }
        });

        // Status badge
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) { setGraphic(null); return; }
                Label badge = new Label(status);
                badge.getStyleClass().add(switch (status) {
                    case "Paid"    -> "badge-completed";
                    case "Partial" -> "badge-scheduled";
                    default        -> "badge-cancelled";
                });
                setGraphic(badge);
            }
        });

        // Actions: Edit, Mark Paid, Delete
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn    = new Button("Edit");
            private final Button paidBtn    = new Button("Mark Paid");
            private final Button deleteBtn  = new Button("Delete");
            private final HBox   box        = new HBox(4, editBtn, paidBtn, deleteBtn);

            {
                editBtn.getStyleClass().add("btn-edit");
                paidBtn.getStyleClass().add("btn-complete");
                deleteBtn.getStyleClass().add("btn-delete");

                editBtn.setOnAction(e -> {
                    Billing b = getTableView().getItems().get(getIndex());
                    openForm(b);
                });
                paidBtn.setOnAction(e -> {
                    Billing b = getTableView().getItems().get(getIndex());
                    markPaid(b);
                });
                deleteBtn.setOnAction(e -> {
                    Billing b = getTableView().getItems().get(getIndex());
                    handleDelete(b);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                Billing b = getTableView().getItems().get(getIndex());
                paidBtn.setDisable(b.getStatus().equals("Paid"));
                setGraphic(box);
            }
        });

        billingTable.setItems(billingList);
    }

    private void loadAll() {
        try {
            billingList.setAll(billingDAO.getAllBills());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Failed to load bills: " + e.getMessage());
        }
    }

    @FXML
    private void handleSearch() {
        String kw = searchField.getText().trim();
        try {
            billingList.setAll(
                    kw.isEmpty() ? billingDAO.getAllBills() : billingDAO.search(kw)
            );
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Search failed: " + e.getMessage());
        }
    }

    @FXML private void handleAdd() { openForm(null); }

    private void markPaid(Billing b) {
        try {
            billingDAO.updateStatus(b.getBillId(), "Paid");
            b.setStatus("Paid");
            billingTable.refresh();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Failed to update: " + e.getMessage());
        }
    }

    private void handleDelete(Billing b) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete bill #" + b.getBillId() + "?");
        confirm.setContentText("This cannot be undone.");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                billingDAO.deleteBill(b.getBillId());
                billingList.remove(b);
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Delete failed: " + e.getMessage());
            }
        }
    }

    private void openForm(Billing billing) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/hms/view/BillingFormView.fxml")
            );
            Stage dialog = new Stage();
            dialog.setTitle(billing == null ? "New Bill" : "Edit Bill");
            dialog.setScene(new Scene(loader.load()));
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setResizable(false);

            BillingFormController fc = loader.getController();
            fc.setBilling(billing);

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