package lk.ijse.theserenitymentalhealththerapycenter.controller;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import lk.ijse.theserenitymentalhealththerapycenter.bo.BOFactory;
import lk.ijse.theserenitymentalhealththerapycenter.bo.BOType;
import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.UserBO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.UserDTO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.enums.UserRole;
import lk.ijse.theserenitymentalhealththerapycenter.util.AlertUtil;
import lk.ijse.theserenitymentalhealththerapycenter.util.NavigationUtil;
import lk.ijse.theserenitymentalhealththerapycenter.util.ValidationUtil;

public class RegisterFormController {

    @FXML private AnchorPane rootRegisterPane;
    @FXML private Button btnRegister;
    @FXML private Hyperlink lnkLogin;
    @FXML private ComboBox<UserRole> cmbRole;

    @FXML private TextField txtFullName;
    @FXML private TextField txtUsername;
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;

    @FXML private TextField txtKeyword;

    private final UserBO userBO = (UserBO) BOFactory.getInstance().getBO(BOType.USER);

    @FXML
    public void initialize() {
        populateAuthorityRolesComboBox();
    }

    private void populateAuthorityRolesComboBox() {
        cmbRole.setItems(FXCollections.observableArrayList(UserRole.values()));
        cmbRole.setValue(UserRole.RECEPTIONIST);
    }

    @FXML
    void btnRegisterOnAction(ActionEvent event) {
        if (!validateRegistrationInputs()) return;

        UserDTO freshUserDto = new UserDTO();
        freshUserDto.setFullName(txtFullName.getText().trim());
        freshUserDto.setUsername(txtUsername.getText().trim().toLowerCase());
        freshUserDto.setEmail(txtEmail.getText().trim());
        freshUserDto.setPassword(txtPassword.getText());
        freshUserDto.setRole(cmbRole.getValue());
        freshUserDto.setStatus(lk.ijse.theserenitymentalhealththerapycenter.dto.enums.CommonStatus.ACTIVE);
        freshUserDto.setRecoveryKeyword(txtKeyword.getText().trim());

        try {
            boolean registrationStateSuccess = userBO.registerUser(freshUserDto);
            if (registrationStateSuccess) {
                AlertUtil.showSuccess("Registration Complete", "Profile Added", "New security account initialized successfully.");
                navigateToLoginScreenWorkspace();
            } else {
                AlertUtil.showError("Registration Failed", "Persistence Rejected", "Database dropped entry safely.");
            }
        } catch (Exception e) {
            AlertUtil.showError("Security Abort", "Registration Violation", e.getMessage());
        }
    }

    @FXML
    void lnkLoginOnAction(ActionEvent event) {
        navigateToLoginScreenWorkspace();
    }

    private void navigateToLoginScreenWorkspace() {
        try {
            NavigationUtil.navigateTo(rootRegisterPane, "Login.fxml");
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Routing Error", "Layout Swapping Failed", "Unable to load Login.fxml layout: " + e.getMessage());
        }
    }

    private boolean validateRegistrationInputs() {
        if (!ValidationUtil.isRequiredFieldFilled(txtFullName.getText()) ||
                !ValidationUtil.isRequiredFieldFilled(txtUsername.getText()) ||
                !ValidationUtil.isRequiredFieldFilled(txtEmail.getText()) ||
                !ValidationUtil.isRequiredFieldFilled(txtPassword.getText()) ||
                !ValidationUtil.isRequiredFieldFilled(txtKeyword.getText())) {

            AlertUtil.showWarning("Input Validation", "Mandatory Fields Missing", "All registration parameters, including the recovery keyword, are required.");
            return false;
        }

        if (!ValidationUtil.isValidEmail(txtEmail.getText())) {
            AlertUtil.showWarning("Validation Error", "Invalid Email String Sequence", "Please pass a correctly structured clinical communication email address.");
            return false;
        }

        if (txtPassword.getText().length() < 4) {
            AlertUtil.showWarning("Security Policy", "Weak Password Entry Length", "Account access codes must be at least 4 characters long.");
            return false;
        }

        return true;
    }
}