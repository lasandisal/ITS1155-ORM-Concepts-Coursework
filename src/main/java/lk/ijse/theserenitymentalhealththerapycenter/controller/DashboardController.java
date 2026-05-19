package lk.ijse.theserenitymentalhealththerapycenter.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import lk.ijse.theserenitymentalhealththerapycenter.dto.UserDTO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.enums.UserRole;
import lk.ijse.theserenitymentalhealththerapycenter.util.AlertUtil;

import java.io.IOException;

public class DashboardController {

    @FXML private Button btnTherapistManage;
    @FXML private Button btnProgramCatalog;
    @FXML private Button btnInvoicePOS;
    @FXML private Button btnPaymentTracking;

    // FIX 1: Declared the missing content canvas container field linked from Dashboard.fxml
    @FXML private AnchorPane contentArea;

    // FIX 2: Centralized session state variable to keep track of who is logged in
    private UserDTO authenticatedUser;

    /**
     * Initializes structural menu constraints dynamically based on authentication context data structures.
     * Enforces explicit access parameters for Admins, Receptionists, and shared workspaces.
     */
    public void configureAccessPrivileges(UserDTO activeUser) {
        if (activeUser == null) return;

        // Save the profile globally so it persists safely across sidebar navigation item clicks
        this.authenticatedUser = activeUser;

        UserRole role = activeUser.getRole();

        switch (role) {
            case ADMIN:
                btnTherapistManage.setVisible(true);
                btnProgramCatalog.setVisible(true);
                btnInvoicePOS.setVisible(false);
                btnPaymentTracking.setVisible(false);
                break;

            case RECEPTIONIST:
                btnTherapistManage.setVisible(false);
                btnProgramCatalog.setVisible(false);
                btnInvoicePOS.setVisible(true);
                btnPaymentTracking.setVisible(true);
                break;

            default:
                btnTherapistManage.setVisible(false);
                btnInvoicePOS.setVisible(false);
                btnPaymentTracking.setVisible(false);
                btnProgramCatalog.setVisible(false);
                break;
        }

        // Best Practice: Default load the overview dashboard or patient intake on login success
        navigateToPatientManagement();
    }

    /**
     * Standard non-parameterized event method connected straight to your Dashboard.fxml sidebar button.
     */
    @FXML
    void btnPatientManageOnAction(ActionEvent event) {
        navigateToPatientManagement();
    }

    /**
     * Orchestrates the dynamic view swapping workflow inside the decoupled content area container.
     */
    private void navigateToPatientManagement() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/lk/ijse/theserenitymentalhealththerapycenter/view/PatientForm.fxml"));
            AnchorPane pane = loader.load();

            // Fetch the loaded controller instance
            PatientFormController controller = loader.getController();

            // Pass the globally cached active user profile safely down to the sub-view elements
            controller.setSessionUserContext(authenticatedUser);

            // Bind the new view node cleanly inside the layout canvas area
            contentArea.getChildren().setAll(pane);
            AnchorPane.setTopAnchor(pane, 0.0);
            AnchorPane.setBottomAnchor(pane, 0.0);
            AnchorPane.setLeftAnchor(pane, 0.0);
            AnchorPane.setRightAnchor(pane, 0.0);

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError(
                    "Navigation Error",
                    "Workspace Loading Failed",
                    "Could not load the Patient Management workspace canvas view file."
            );
        }
    }

    @FXML
    void btnDashboardOnAction(ActionEvent event) { /* Load Overview FXML */ }

    @FXML
    void btnTherapistManageOnAction(ActionEvent event) { /* Load Therapist FXML */ }

    @FXML
    void btnProgramCatalogOnAction(ActionEvent event) { /* Load Catalog FXML */ }

    @FXML
    void handleInvoicePOSOnAction(ActionEvent event) { /* Load POS FXML */ }

    @FXML
    void btnPaymentTrackingOnAction(ActionEvent event) { /* Load Ledger FXML */ }

    @FXML
    void handleLogOutOnAction(ActionEvent event) { /* Handle teardown and return to login stage */ }
}