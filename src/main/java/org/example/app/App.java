package org.example.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.app.fxcontrollers.MainAppController;

import java.net.URL;
import java.util.Objects;

public class App extends Application {

    private final static URL mainAppFxmlUrl = MainAppController.class.getResource("MainApp.fxml");

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(Objects.requireNonNull(mainAppFxmlUrl));
        stage.setScene(new Scene(root));
        stage.setTitle("Filterprint Matcher App");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
