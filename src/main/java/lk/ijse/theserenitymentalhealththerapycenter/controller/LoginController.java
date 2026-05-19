package lk.ijse.theserenitymentalhealththerapycenter.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Rectangle;
import lk.ijse.theserenitymentalhealththerapycenter.bo.BOFactory;
import lk.ijse.theserenitymentalhealththerapycenter.bo.BOType;
import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.UserBO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.UserDTO;
import lk.ijse.theserenitymentalhealththerapycenter.util.AlertUtil;
import lk.ijse.theserenitymentalhealththerapycenter.util.NavigationUtil;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.net.URL;

public class LoginController {

    private final UserBO userBO = (UserBO) BOFactory.getInstance().getBO(BOType.USER);

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
    private AnchorPane root;

    @FXML
    private TextField usernameField;

    @FXML
    void btnLoginOnAction(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.isVisible() ? passwordField.getText() : passwordTextField.getText();

        try {
            UserDTO authenticatedUser = userBO.authenticate(username, password);
            AlertUtil.showSuccess("Login Successful", null, "Welcome back, " + authenticatedUser.getFullName() + "!");
            NavigationUtil.navigateTo(root, "Dashboard.fxml");

        } catch (IOException e) {
            AlertUtil.showError("Navigation Error", "UI Load Failure", "Could not load DashboardForm.fxml. Please verify folder placement.");
            e.printStackTrace();
        } catch (Exception e) {
            AlertUtil.showError("Authentication Error", "Login Failed", e.getMessage());
        }
    }

    @FXML
    void togglePasswordVisibility(ActionEvent event) {

        if (passwordField.isVisible()) {
            passwordTextField.setText(passwordField.getText());
            passwordTextField.setVisible(true);
            passwordTextField.setManaged(true);
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            eyeIcon.setIconLiteral("fas-eye-slash");

        } else {

            // Show hidden password
            passwordField.setText(passwordTextField.getText());

            passwordField.setVisible(true);
            passwordField.setManaged(true);

            passwordTextField.setVisible(false);
            passwordTextField.setManaged(false);

            eyeIcon.setIconLiteral("fas-eye");
        }
    }

    @FXML
    void handleForgotPasswordLink(ActionEvent event) {
        AlertUtil.showInformation("System Feature", null, "Password recovery options will be implemented here.");
    }

    @FXML
    void handleRegisterLink(ActionEvent event) {
        AlertUtil.showInformation("System Feature", null, "User registration window transitions will navigate from here.");
    }
}