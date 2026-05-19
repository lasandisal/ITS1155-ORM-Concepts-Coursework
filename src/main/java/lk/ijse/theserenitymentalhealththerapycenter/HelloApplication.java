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
        // 1. Initialize the Hibernate Session Factory instance to kick off the auto-seeding method
        try {
            FactoryConfiguration.getInstance();
        } catch (Exception e) {
            System.err.println("Critical System Boot Failure: Could not build database communication pipelines.");
            e.printStackTrace();
        }

        // 2. Point to your clean, error-free login layout file path
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("/lk/ijse/theserenitymentalhealththerapycenter/view/Login.fxml"));

        // 3. Set a premium modern desktop window canvas dimension configuration (1280x720)
        Scene scene = new Scene(fxmlLoader.load(), 1280, 720);

        stage.setTitle("The Serenity Mental Health & Therapy Center - Secure Portal");
        stage.setScene(scene);

        // Prevent users from breaking your layout structure aspect ratios while testing
        stage.setResizable(false);

        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}