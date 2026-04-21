package com.hms.controller;

import com.hms.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.scene.Scene;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    @FXML private BorderPane contentArea;
    @FXML private Label userInfoLabel;
    @FXML private Button btnDoctorDashboard;
    // Sidebar buttons — we need these to highlight the active one
    @FXML private Button btnDashboard;
    @FXML private Button btnPatients;
    @FXML private Button btnDoctors;
    @FXML private Button btnAppointments;
    @FXML private Button btnBilling;
    @FXML private Button btnLab;
    @FXML private Button btnReports;
    @FXML private Button btnMedicine;
    @FXML private Button btnPrescriptions;
    @FXML private Button btnPharmacy;
    private Button activeButton;

    // initialize() runs automatically after the FXML is loaded.
    // Think of it as the constructor for your controller.
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Show who is logged in
        userInfoLabel.setText(
                SessionManager.getCurrentUser().getName()
                        + "  |  "
                        + SessionManager.getCurrentRole()
        );

        // Load the home view by default
        loadHome();
        setActiveButton(btnDashboard);

        // Hide buttons the current role shouldn't see
        applyRoleVisibility();
    }

    @FXML private void loadHome()         { loadView("HomeView.fxml",        btnDashboard); }
    @FXML private void loadPatients()     { loadView("PatientView.fxml",     btnPatients); }
    @FXML private void loadDoctors()      { loadView("DoctorView.fxml",      btnDoctors); }
    @FXML private void loadAppointments() { loadView("AppointmentView.fxml", btnAppointments); }
    @FXML private void loadBilling()      { loadView("BillingView.fxml",     btnBilling); }
    @FXML private void loadLab()          { loadView("LabView.fxml",         btnLab); }
    @FXML private void loadReports()      { loadView("ReportsView.fxml", btnReports); }
    @FXML private void loadMedicine()     { loadView("MedicineView.fxml",      btnMedicine); }
    @FXML private void loadPrescriptions(){ loadView("PrescriptionView.fxml",  btnPrescriptions); }
    @FXML private void loadDoctorDashboard() { loadView("DoctorDashboardView.fxml", btnDoctorDashboard);}
    @FXML private void loadPharmacy( )    { loadView("PharmacyView.fxml", btnPharmacy);}



    // Central loader — all nav methods call this
    private void loadView(String fxmlFile, Button clickedButton)
    {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/hms/view/" + fxmlFile)
            );
            Pane view = loader.load();
            contentArea.setCenter(view);
            setActiveButton(clickedButton);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Highlights the active nav button and un-highlights the previous one
    private void setActiveButton(Button button)
    {
        if (activeButton != null) {
            activeButton.getStyleClass().remove("nav-btn-active");
        }
        activeButton = button;
        activeButton.getStyleClass().add("nav-btn-active");
    }

    // Hide nav items based on role
// Update applyRoleVisibility() — replace the existing method
    private void applyRoleVisibility() {
        String role = SessionManager.getCurrentRole();

        // Hide "My Dashboard" for everyone except Doctor
        btnDoctorDashboard.setVisible(false);
        btnDoctorDashboard.setManaged(false);

        switch (role) {
            case "Doctor" -> {
                // Doctor sees only their own dashboard, patients, appointments,
                // prescriptions, lab, medicine
                btnDoctorDashboard.setVisible(true);    btnDoctorDashboard.setManaged(true);
                btnBilling.setVisible(false);           btnBilling.setManaged(false);
                btnReports.setVisible(false);           btnReports.setManaged(false);
                btnPharmacy.setVisible(false);          btnPharmacy.setManaged(false);
                btnPatients.setVisible(false);          btnPatients.setManaged(false);
                btnDoctors.setVisible(false);           btnDoctors.setManaged(false);
                btnMedicine.setVisible(false);          btnMedicine.setManaged(false);
                btnPrescriptions.setVisible(false);     btnPrescriptions.setManaged(false);
                // Auto-load their personal dashboard instead of Home
                loadDoctorDashboard();
            }
            case "LabAssistant" -> {
                btnPatients.setVisible(false);          btnPatients.setManaged(false);
                btnDoctors.setVisible(false);           btnDoctors.setManaged(false);
                btnBilling.setVisible(false);           btnBilling.setManaged(false);
                btnReports.setVisible(false);           btnReports.setManaged(false);
                btnPharmacy.setVisible(false);          btnPharmacy.setManaged(false);
                btnAppointments.setVisible(false);      btnAppointments.setManaged(false);
                btnMedicine.setVisible(false);          btnMedicine.setManaged(false);
                btnPrescriptions.setVisible(false);     btnPrescriptions.setManaged(false);
            }
            case "Pharmacist" -> {
                btnPatients.setVisible(false);          btnPatients.setManaged(false);
                btnDoctors.setVisible(false);           btnDoctors.setManaged(false);
                btnBilling.setVisible(false);           btnBilling.setManaged(false);
                btnReports.setVisible(false);           btnReports.setManaged(false);
                btnAppointments.setVisible(false);      btnAppointments.setManaged(false);
                btnLab.setVisible(false);               btnLab.setManaged(false);
            }
            case "Receptionist" -> {
                btnPharmacy.setVisible(false);          btnPharmacy.setManaged(false);
                btnReports.setVisible(false);           btnReports.setManaged(false);
                btnLab.setVisible(false);               btnLab.setManaged(false);
                btnMedicine.setVisible(false);          btnMedicine.setManaged(false);
                btnPrescriptions.setVisible(false);     btnPrescriptions.setManaged(false);
            }
            // Admin sees everything — no changes needed
        }
    }

    @FXML
    private void handleLogout()
    {
        SessionManager.logout();
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/hms/view/LoginView.fxml")
            );

            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(
                    getClass().getResource("/com/hms/styles/app.css").toExternalForm()
            );

            Stage stage = (Stage) contentArea.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("HealthPlus+");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}