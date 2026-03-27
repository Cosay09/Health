package com.hms.controller;

import com.hms.dao.UserDAO;
import com.hms.model.User;
import com.hms.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    // @FXML tells Java: "this variable is wired to an fx:id in the FXML file"
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private final UserDAO userDAO = new UserDAO();

    // This method is called when the Login button is clicked
    // (or Enter is pressed, because defaultButton="true")
    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        // Basic validation — never trust empty input
        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both username and password.");
            return;
        }

        try {
            User user = userDAO.findByUsernameAndPassword(username, password);

            if (user != null) {
                // Save logged-in user globally so other screens can access it
                SessionManager.setCurrentUser(user);
                loadDashboard();
            } else {
                showError("Invalid username or password.");
                passwordField.clear();
            }

        } catch (Exception e) {
            showError("Connection error. Please try again.");
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private void loadDashboard() throws Exception {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/hms/view/DashboardView.fxml")
        );
        Stage stage = (Stage) usernameField.getScene().getWindow();
        stage.setScene(new Scene(loader.load()));
        stage.setTitle("HMS — Dashboard");
    }
}