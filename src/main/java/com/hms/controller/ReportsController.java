package com.hms.controller;

import com.hms.util.ReportGenerator;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class ReportsController {

    @FXML private VBox root;

    @FXML private void generateAppointmentReport() { generate("Appointment_Report", "appointment"); }
    @FXML private void generateDoctorReport()      { generate("Doctor_Report",      "doctor"); }
    @FXML private void generateBillingReport()     { generate("Billing_Report",     "billing"); }
    @FXML private void generateLabReport()         { generate("Lab_Report",         "lab"); }
    @FXML private void generateFinancialReport()   { generate("Financial_Report",   "financial"); }

    private void generate(String defaultName, String type) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save Report");
        chooser.setInitialFileName(defaultName + ".pdf");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
        );

        Stage stage = (Stage) root.getScene().getWindow();
        File file = chooser.showSaveDialog(stage);

        if (file != null) {
            try {
                switch (type) {
                    case "appointment" -> ReportGenerator.generateAppointmentReport(file.getAbsolutePath());
                    case "doctor"      -> ReportGenerator.generateDoctorReport(file.getAbsolutePath());
                    case "billing"     -> ReportGenerator.generateBillingReport(file.getAbsolutePath());
                    case "lab"         -> ReportGenerator.generateLabReport(file.getAbsolutePath());
                    case "financial"   -> ReportGenerator.generateFinancialReport(file.getAbsolutePath());
                }
                new Alert(Alert.AlertType.INFORMATION,
                        "Report saved:\n" + file.getAbsolutePath()).showAndWait();
            } catch (Exception e) {
                new Alert(Alert.AlertType.ERROR,
                        "Failed to generate report:\n" + e.getMessage()).showAndWait();
            }
        }
    }
}