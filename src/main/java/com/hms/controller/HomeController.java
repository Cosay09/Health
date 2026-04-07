package com.hms.controller;

import com.hms.dao.PatientDAO;
import com.hms.dao.AppointmentDAO;
import com.hms.dao.LabTestDAO;
import com.hms.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

public class HomeController implements Initializable {

    @FXML private Label welcomeLabel;
    @FXML private Label patientCount;
    @FXML private Label appointmentCount;
    @FXML private Label labCount;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        welcomeLabel.setText(
                "Welcome, " + SessionManager.getCurrentUser().getName()
        );

        loadStats();
    }

    private void loadStats() {
        try {
            patientCount.setText(
                    String.valueOf(new PatientDAO().countAll())
            );
            appointmentCount.setText(
                    String.valueOf(new AppointmentDAO().countToday())
            );
            labCount.setText(
                    String.valueOf(new LabTestDAO().countPending())
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}