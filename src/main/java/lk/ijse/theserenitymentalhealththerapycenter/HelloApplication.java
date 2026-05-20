package lk.ijse.theserenitymentalhealththerapycenter;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lk.ijse.theserenitymentalhealththerapycenter.config.FactoryConfiguration;

import java.io.IOException;

public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        try {
            FactoryConfiguration.getInstance();
        } catch (Exception e) {
            System.err.println("Critical System Boot Failure: Could not build database communication pipelines.");
            e.printStackTrace();
        }

        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("/lk/ijse/theserenitymentalhealththerapycenter/view/Login.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1280, 720);
        stage.setTitle("The Serenity Mental Health & Therapy Center - Secure Portal");
        stage.setScene(scene);
        stage.setResizable(false);

        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}