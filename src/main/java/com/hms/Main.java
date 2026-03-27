package com.hms;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class Main extends Application
{
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/hms/view/LoginView.fxml")
        );

        Scene scene = new Scene(loader.load());

        scene.getStylesheets().add(
            getClass().getResource("/com/hms/styles/app.css").toExternalForm()
        );

        primaryStage.setTitle("HMS — Login");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
