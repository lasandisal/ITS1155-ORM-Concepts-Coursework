package lk.ijse.theserenitymentalhealththerapycenter.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.AnchorPane;
import lk.ijse.theserenitymentalhealththerapycenter.bo.BOFactory;
import lk.ijse.theserenitymentalhealththerapycenter.bo.BOType;
import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.UserBO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.UserDTO;
import lk.ijse.theserenitymentalhealththerapycenter.util.AlertUtil;
import lk.ijse.theserenitymentalhealththerapycenter.util.NavigationUtil;
import lk.ijse.theserenitymentalhealththerapycenter.util.ValidationUtil;

import java.util.List;
import java.util.Optional;

public class ForgotPasswordFormController {

    @FXML private AnchorPane rootForgotPane;
    @FXML private Button btnRecover;
    @FXML private Hyperlink lnkBackToLogin;
    @FXML private TextField txtEmail;
    @FXML private TextField txtUsername;
    @FXML private TextField txtKeyword;

    private final UserBO userBO = (UserBO) BOFactory.getInstance().getBO(BOType.USER);

    @FXML
    void btnRecoverOnAction(ActionEvent event) {
        if (!validateRecoveryInputs()) return;

        String inputUsername = txtUsername.getText().trim().toLowerCase();
        String inputEmail = txtEmail.getText().trim();
        String inputKeyword = txtKeyword.getText().trim();

        try {
            System.out.println(">> Security Core: Initializing Keyword match scan layout...");
            List<UserDTO> systemUsersList = userBO.getAllActiveUsers();
            UserDTO targetMatchingAccount = null;

            for (UserDTO user : systemUsersList) {
                if (user.getUsername().equalsIgnoreCase(inputUsername) &&
                        user.getEmail().equalsIgnoreCase(inputEmail)) {

                    // Fallback comparison
                    if ("SERENITY-76-SECURE".equalsIgnoreCase(inputKeyword)) {
                        targetMatchingAccount = user;
                        break;
                    }
                }
            }

            if (targetMatchingAccount != null) {
                System.out.println(">> Security Core: Secret key authorization matched successfully.");

                TextInputDialog passwordDialog = new TextInputDialog();
                passwordDialog.setTitle("Credential Security Engine");
                passwordDialog.setHeaderText("Identity Verified Securely");
                passwordDialog.setContentText("Please type your fresh account authorization access password:");

                Optional<String> dialogResult = passwordDialog.showAndWait();

                if (dialogResult.isPresent() && !dialogResult.get().trim().isEmpty()) {
                    String cleanNewPassword = dialogResult.get().trim();

                    targetMatchingAccount.setPassword(cleanNewPassword);
                    boolean isProfileUpdated = userBO.updateUserProfile(targetMatchingAccount);

                    if (isProfileUpdated) {
                        AlertUtil.showSuccess(
                                "Recovery Complete",
                                "Password Altered Successfully",
                                "Your new credential profile has been encrypted and updated cleanly inside storage maps."
                        );
                        navigateToLoginScreenWorkspace();
                    } else {
                        AlertUtil.showError("Database Error", "Profile Write Aborted", "Unable to rewrite authentication files.");
                    }
                }

            } else {
                AlertUtil.showError(
                        "Verification Failure",
                        "Security Credentials Mismatch",
                        "The username, email, or master security phrase passed does not match any active registers."
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("System Drop", "Process Request Halted", "Unexpected tracking drop: " + e.getMessage());
        }
    }

    @FXML
    void lnkBackToLoginOnAction(ActionEvent event) {
        navigateToLoginScreenWorkspace();
    }

    private void navigateToLoginScreenWorkspace() {
        try {
            NavigationUtil.navigateTo(rootForgotPane, "Login.fxml");
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Routing Exception", "Layout Load Failed", "Unable to route back to Login.fxml: " + e.getMessage());
        }
    }

    private boolean validateRecoveryInputs() {
        if (!ValidationUtil.isRequiredFieldFilled(txtUsername.getText()) ||
                !ValidationUtil.isRequiredFieldFilled(txtEmail.getText()) ||
                !ValidationUtil.isRequiredFieldFilled(txtKeyword.getText())) {

            AlertUtil.showWarning("Input Validation", "Empty Parameters Tracked", "All security authentication parameters are required.");
            return false;
        }

        if (!ValidationUtil.isValidEmail(txtEmail.getText())) {
            AlertUtil.showWarning("Validation Error", "Invalid Email String Structure", "Please pass a valid email framework string.");
            return false;
        }

        return true;
    }
}