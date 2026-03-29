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

    // Sidebar buttons — we need these to highlight the active one
    @FXML private Button btnDashboard;
    @FXML private Button btnPatients;
    @FXML private Button btnDoctors;
    @FXML private Button btnAppointments;
    @FXML private Button btnBilling;
    @FXML private Button btnLab;

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

    // Central loader — all nav methods call this
    private void loadView(String fxmlFile, Button clickedButton) {
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
    private void setActiveButton(Button button) {
        if (activeButton != null) {
            activeButton.getStyleClass().remove("nav-btn-active");
        }
        activeButton = button;
        activeButton.getStyleClass().add("nav-btn-active");
    }

    // Hide nav items based on role
    private void applyRoleVisibility() {
        String role = SessionManager.getCurrentRole();

        switch (role) {
            case "LabAssistant" -> {
                btnPatients.setVisible(false);
                btnDoctors.setVisible(false);
                btnBilling.setVisible(false);
            }
            case "Doctor" -> {
                btnBilling.setVisible(false);
            }
            // Admin sees everything — no changes needed
        }
    }

    @FXML
    private void handleLogout() {
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
            stage.setTitle("HMS — Login");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}