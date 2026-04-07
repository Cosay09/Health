package com.hms.controller;

import com.hms.dao.MedicineDAO;
import com.hms.model.Medicine;
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

public class MedicineController implements Initializable {

    @FXML private TableView<Medicine>            medicineTable;
    @FXML private TableColumn<Medicine, Integer> colId;
    @FXML private TableColumn<Medicine, String>  colName;
    @FXML private TableColumn<Medicine, Integer> colStock;
    @FXML private TableColumn<Medicine, String>  colUnit;
    @FXML private TableColumn<Medicine, Double>  colPrice;
    @FXML private TableColumn<Medicine, Void>    colActions;
    @FXML private TextField                      searchField;
    @FXML private Label                          lowStockWarning;

    private static final int LOW_STOCK_THRESHOLD = 10;

    private final ObservableList<Medicine> medicineList = FXCollections.observableArrayList();
    private final MedicineDAO medicineDAO = new MedicineDAO();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupColumns();
        loadAll();
    }

    private void setupColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("medicineId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colUnit.setCellValueFactory(new PropertyValueFactory<>("unit"));

        // Colour the stock cell red if low
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        colStock.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer stock, boolean empty) {
                super.updateItem(stock, empty);
                if (empty || stock == null) { setText(null); setStyle(""); return; }
                setText(stock.toString());
                setStyle(stock <= LOW_STOCK_THRESHOLD
                        ? "-fx-text-fill: #e74c3c; -fx-font-weight: bold;"
                        : "");
            }
        });

        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colPrice.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                setText(empty || price == null ? null : String.format("৳ %.2f", price));
            }
        });

        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn   = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox   box       = new HBox(6, editBtn, deleteBtn);

            {
                editBtn.getStyleClass().add("btn-edit");
                deleteBtn.getStyleClass().add("btn-delete");

                editBtn.setOnAction(e -> {
                    Medicine m = getTableView().getItems().get(getIndex());
                    openForm(m);
                });
                deleteBtn.setOnAction(e -> {
                    Medicine m = getTableView().getItems().get(getIndex());
                    handleDelete(m);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        medicineTable.setItems(medicineList);
    }

    private void loadAll() {
        try {
            medicineList.setAll(medicineDAO.getAllMedicines());
            checkLowStock();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Failed to load medicines: " + e.getMessage());
        }
    }

    // Shows a warning banner if any medicine is below threshold
    private void checkLowStock() {
        try {
            List<Medicine> low = medicineDAO.getLowStock(LOW_STOCK_THRESHOLD);
            if (!low.isEmpty()) {
                lowStockWarning.setText(
                        "Warning: " + low.size() + " medicine(s) low on stock"
                );
                lowStockWarning.setVisible(true);
            } else {
                lowStockWarning.setVisible(false);
            }
        } catch (Exception ignored) {}
    }

    @FXML
    private void handleSearch() {
        String kw = searchField.getText().trim();
        try {
            medicineList.setAll(
                    kw.isEmpty() ? medicineDAO.getAllMedicines() : medicineDAO.search(kw)
            );
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Search failed: " + e.getMessage());
        }
    }

    @FXML private void handleAdd() { openForm(null); }

    private void handleDelete(Medicine m) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete " + m.getName() + "?");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                medicineDAO.deleteMedicine(m.getMedicineId());
                medicineList.remove(m);
                checkLowStock();
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Delete failed: " + e.getMessage());
            }
        }
    }

    private void openForm(Medicine medicine) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/hms/view/MedicineFormView.fxml")
            );
            Stage dialog = new Stage();
            dialog.setTitle(medicine == null ? "Add Medicine" : "Edit Medicine");
            dialog.setScene(new Scene(loader.load()));
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setResizable(false);

            MedicineFormController fc = loader.getController();
            fc.setMedicine(medicine);

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