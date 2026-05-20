package lk.ijse.theserenitymentalhealththerapycenter.controller;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
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
import lk.ijse.theserenitymentalhealththerapycenter.dto.enums.UserRole;
import lk.ijse.theserenitymentalhealththerapycenter.util.AlertUtil;
import lk.ijse.theserenitymentalhealththerapycenter.util.NavigationUtil;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.net.URL;

public class LoginController {

    private final UserBO userBO = (UserBO) BOFactory.getInstance().getBO(BOType.USER);

    @FXML private Rectangle backgroundPane;
    @FXML private Button btnTogglePassword;
    @FXML private FontIcon eyeIcon;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordTextField;
    @FXML private AnchorPane root;
    @FXML private TextField usernameField;


    @FXML private ComboBox<UserRole> roleComboBox;

    @FXML
    public void initialize() {
        roleComboBox.setItems(FXCollections.observableArrayList(UserRole.values()));
        roleComboBox.setValue(UserRole.ADMIN); // Fallback standard default choice target
    }

    @FXML
    void btnLoginOnAction(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.isVisible() ? passwordField.getText() : passwordTextField.getText();
        UserRole selectedRole = roleComboBox.getValue();

        if (selectedRole == null) {
            AlertUtil.showWarning("Validation Error", "Missing Role Assignment", "Please select your assigned workflow execution role profile option before continuing.");
            return;
        }

        try {
            // Validate username and password using BCrypt via the User BO layer
            UserDTO authenticatedUser = userBO.authenticate(username, password);

            // ✅ STRATEGIC CHECK: Verify that the credentials match the selected login role
            if (authenticatedUser.getRole() != selectedRole) {
                AlertUtil.showWarning("Access Denied", "Role Mismatch Profile Error",
                        "The credentials provided do not map to the '" + selectedRole.name() + "' profile domain.");
                return;
            }

            AlertUtil.showSuccess("Login Successful", null, "Welcome back, " + authenticatedUser.getFullName() + "!");

            // ✅ BEST PRACTICE: Forward-Injecting Session state values through the Loader instantiation pipe
            URL resource = getClass().getResource("/lk/ijse/theserenitymentalhealththerapycenter/view/Dashboard.fxml");
            if (resource == null) {
                throw new IOException("FXML layout compilation resource path entry target missing: Dashboard.fxml");
            }

            FXMLLoader loader = new FXMLLoader(resource);
            Parent dashboardRoot = loader.load();

            // Extract the active instance of the controller directly from the pipeline
            DashboardController dashboardController = loader.getController();

            // Pass user attributes and initialize sidebar visibility metrics
            dashboardController.configureAccessPrivileges(authenticatedUser);

            // Swap out primary window screen layout boundaries completely
            root.getChildren().clear();
            root.getChildren().add(dashboardRoot);

            AnchorPane.setTopAnchor(dashboardRoot, 0.0);
            AnchorPane.setBottomAnchor(dashboardRoot, 0.0);
            AnchorPane.setLeftAnchor(dashboardRoot, 0.0);
            AnchorPane.setRightAnchor(dashboardRoot, 0.0);

        } catch (IOException e) {
            AlertUtil.showError("Navigation Error", "UI Load Failure", "Could not compile layout boundaries.");
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
        try {
            // Assuming 'rootLoginPane' is your Login screen's root AnchorPane identifier variable mapping
            System.out.println(">> Routing Engine: Swapping screen state context to ForgotPasswordForm.fxml...");
            NavigationUtil.navigateTo(root, "ForgotPasswordForm.fxml");
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Navigation Drop", "View Map Error", "Could not load ForgotPasswordForm.fxml layout template file from path resources.");
        }
    }

    @FXML
    void handleRegisterLink(ActionEvent event) {
        try {
            // Assuming 'rootLoginPane' or similar is your Login screen's base container AnchorPane anchor variable link field
            System.out.println(">> Routing Engine: Mapping view stack context to registration registration...");
            NavigationUtil.navigateTo(root, "RegisterForm.fxml");
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Navigation Drop", "View Map Error", "Could not locate RegisterForm.fxml configuration.");
        }
    }
}