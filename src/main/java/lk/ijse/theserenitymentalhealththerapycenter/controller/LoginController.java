package lk.ijse.theserenitymentalhealththerapycenter.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import org.kordamp.ikonli.javafx.FontIcon;

public class LoginController {
    @FXML
    private Rectangle backgroundPane;

    @FXML
    private Button btnTogglePassword;

    @FXML
    private FontIcon eyeIcon;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField passwordTextField;

    @FXML
    private ComboBox<?> roleComboBox;

    @FXML
    private StackPane root;

    @FXML
    private TextField usernameField;

    @FXML
    void btnLoginOnAction(ActionEvent event) {

    }

    @FXML
    void handleForgotPasswordLink(ActionEvent event) {

    }

    @FXML
    void handleRegisterLink(ActionEvent event) {

    }

    @FXML
    void togglePasswordVisibility(ActionEvent event) {

    }
}
