package lk.ijse.theserenitymentalhealththerapycenter.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import lk.ijse.theserenitymentalhealththerapycenter.bo.BOFactory;
import lk.ijse.theserenitymentalhealththerapycenter.bo.BOType;
import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.UserBO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.UserDTO;
import lk.ijse.theserenitymentalhealththerapycenter.util.AlertUtil;
import lk.ijse.theserenitymentalhealththerapycenter.util.ValidationUtil;

public class ProfileFormController {

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtNewPassword;
    @FXML private PasswordField txtConfirmPassword;
    @FXML private Button btnSave;

    private final UserBO userBO = (UserBO) BOFactory.getInstance().getBO(BOType.USER);
    private UserDTO currentUser;

    @FXML
    public void initialize() {
        this.currentUser = DashboardController.getAuthenticatedUserSession();
        if (currentUser != null) {
            txtUsername.setText(currentUser.getUsername());
        }
    }

    @FXML
    void btnSaveOnAction(ActionEvent event) {
        String username = txtUsername.getText().trim();
        String password = txtNewPassword.getText();
        String confirmPassword = txtConfirmPassword.getText();

        if (!ValidationUtil.isRequiredFieldFilled(username)) {
            AlertUtil.showWarning("Validation Error", "Missing Values", "Username cannot be empty.");
            return;
        }

        if (!password.isEmpty()) {
            if (password.length() < 4) {
                AlertUtil.showWarning("Security Error", "Weak Password", "Password must be at least 4 characters.");
                return;
            }
            if (!password.equals(confirmPassword)) {
                AlertUtil.showWarning("Validation Error", "Mismatch", "Passwords do not match.");
                return;
            }
            currentUser.setPassword(password);
        }

        currentUser.setUsername(username.toLowerCase());

        try {
            boolean isUpdated = userBO.updateUserProfile(currentUser);
            if (isUpdated) {
                AlertUtil.showSuccess("Profile Updated", "Success", "Security parameters updated seamlessly.");
            } else {
                AlertUtil.showError("System Error", "Update Aborted", "Persistence layer rejected alterations.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Execution Error", "Failed", e.getMessage());
        }
    }
}