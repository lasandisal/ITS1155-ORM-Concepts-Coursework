package lk.ijse.theserenitymentalhealththerapycenter.controller;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import lk.ijse.theserenitymentalhealththerapycenter.dto.UserDTO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.enums.UserRole;
import lk.ijse.theserenitymentalhealththerapycenter.util.AlertUtil;
import lk.ijse.theserenitymentalhealththerapycenter.util.NavigationUtil;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

public class DashboardController {

    @FXML private Button btnDashboard;
    @FXML private Button btnGenerateInvoice;
    @FXML private Button btnPatientManage;
    @FXML private Button btnPaymentTracking;
    @FXML private Button btnSessionSchedule;
    @FXML private Button btnTherapistManage;
    @FXML private Button btnTherapyCatalog;
    @FXML private AnchorPane contentArea;

    @FXML private Label lblViewTitle;
    @FXML private Label lblSystemTime;
    @FXML private Label lblLoggedInUser;
    @FXML private Label lblFinanceHeader;

    private static UserDTO authenticatedUser;

    @FXML
    public void initialize() {
        startGlobalClock();
    }

    private void startGlobalClock() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd | hh:mm:ss a");
        Timeline clock = new Timeline(new KeyFrame(Duration.ZERO, e -> {
            lblSystemTime.setText(LocalDateTime.now().format(formatter));
        }), new KeyFrame(Duration.seconds(1)));
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();
    }


    public void configureAccessPrivileges(UserDTO activeUser) {
        if (activeUser == null) return;
        this.authenticatedUser = activeUser;

        lblLoggedInUser.setText(activeUser.getFullName() + " (" + activeUser.getRole().name() + ")");

        UserRole role = activeUser.getRole();

        switch (role) {
            case ADMIN -> {
                // ========== VISIBILITY CONFIGURATIONS ==========
                btnDashboard.setVisible(true);
                btnSessionSchedule.setVisible(true);
                btnTherapistManage.setVisible(true);
                btnTherapyCatalog.setVisible(true);

                btnGenerateInvoice.setVisible(false);
                btnPaymentTracking.setVisible(true);
                lblFinanceHeader.setVisible(true);

                // ========== LAYOUT MANAGEMENT CONFIGURATIONS ==========
                btnDashboard.setManaged(true);
                btnSessionSchedule.setManaged(true);
                btnTherapistManage.setManaged(true);
                btnTherapyCatalog.setManaged(true);
                btnGenerateInvoice.setManaged(false);
                btnPaymentTracking.setManaged(true);
                lblFinanceHeader.setManaged(true);

                setActiveNavigation(btnDashboard);
                lblViewTitle.setText("Overview Insights Dashboard");
                loadView("OverviewForm.fxml");
            }

            case RECEPTIONIST -> {
                // ========== VISIBILITY CONFIGURATIONS ==========
                btnDashboard.setVisible(false);
                btnTherapistManage.setVisible(false);
                btnTherapyCatalog.setVisible(false);

                btnSessionSchedule.setVisible(true);
                btnGenerateInvoice.setVisible(true);
                btnPaymentTracking.setVisible(true);
                lblFinanceHeader.setVisible(true);

                // ========== LAYOUT MANAGEMENT CONFIGURATIONS ==========
                btnDashboard.setManaged(false);
                btnTherapistManage.setManaged(false);
                btnTherapyCatalog.setManaged(false);

                btnSessionSchedule.setManaged(true);
                btnGenerateInvoice.setManaged(true);
                btnPaymentTracking.setManaged(true);
                lblFinanceHeader.setManaged(true);

                setActiveNavigation(btnPatientManage);
                lblViewTitle.setText("Patient Intake & Clinical Registry");
                loadView("PatientForm.fxml");
            }

            default -> {
                // ========== ABSOLUTE LOCKDOWN FALLBACK MODE ==========
                btnDashboard.setVisible(false);
                btnSessionSchedule.setVisible(false);
                btnTherapistManage.setVisible(false);
                btnTherapyCatalog.setVisible(false);
                btnGenerateInvoice.setVisible(false);
                btnPaymentTracking.setVisible(false);
                lblFinanceHeader.setVisible(false);
                btnPatientManage.setVisible(false);

                btnDashboard.setManaged(false);
                btnSessionSchedule.setManaged(false);
                btnTherapistManage.setManaged(false);
                btnTherapyCatalog.setManaged(false);
                btnGenerateInvoice.setManaged(false);
                btnPaymentTracking.setManaged(false);
                lblFinanceHeader.setManaged(false);
                btnPatientManage.setManaged(false);

                lblViewTitle.setText("Access Denied - Restricted Profile Context");
                return;
            }
        }

        // Both verified active roles
        btnPatientManage.setVisible(true);
        btnPatientManage.setManaged(true);
    }

    private void loadView(String fxmlFile) {
        try {
            NavigationUtil.navigateTo(contentArea, fxmlFile);
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Navigation Error", "View Loading Failed", "Unable to load: " + fxmlFile);
        }
    }

    private void setActiveNavigation(Button activeButton) {
        List<Button> navigationButtons = Arrays.asList(
                btnDashboard, btnPatientManage, btnTherapistManage,
                btnTherapyCatalog, btnSessionSchedule, btnGenerateInvoice, btnPaymentTracking
        );

        for (Button button : navigationButtons) {
            button.getStyleClass().remove("active-nav-button");
        }

        if (activeButton != null) {
            activeButton.getStyleClass().add("active-nav-button");
        }
    }

    @FXML
    void btnDashboardOnAction(ActionEvent event) {
        setActiveNavigation(btnDashboard);
        lblViewTitle.setText("Overview Insights Dashboard");
        loadView("OverviewForm.fxml");
    }

    @FXML
    void btnPatientManageOnAction(ActionEvent event) {
        setActiveNavigation(btnPatientManage);
        lblViewTitle.setText("Patient Intake & Clinical Registry");
        loadView("PatientForm.fxml");
    }

    @FXML
    void btnPaymentTrackingOnAction(ActionEvent event) {
        setActiveNavigation(btnPaymentTracking);
        lblViewTitle.setText("Financial Accounts Ledger");
        loadView("PaymentForm.fxml");
    }

    @FXML
    void btnSessionScheduleOnAction(ActionEvent event) {
        setActiveNavigation(btnSessionSchedule);
        lblViewTitle.setText("Clinical Appointment Scheduler");
        loadView("SessionForm.fxml");
    }

    @FXML
    void btnTherapistManageOnAction(ActionEvent event) {
        setActiveNavigation(btnTherapistManage);
        lblViewTitle.setText("Therapist Roster Directory");
        loadView("TherapistForm.fxml");
    }

    @FXML
    void btnTherapyCatalogOnAction(ActionEvent event) {
        setActiveNavigation(btnTherapyCatalog);
        lblViewTitle.setText("Therapy Framework Catalog");
        loadView("TherapyCatalogForm.fxml");
    }

    @FXML
    void handleGenerateInvoice(ActionEvent event) {
        setActiveNavigation(btnGenerateInvoice);
        lblViewTitle.setText("Point Of Sale - Generate Invoice");

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/lk/ijse/theserenitymentalhealththerapycenter/view/InvoiceForm.fxml"));
            Parent root = loader.load();

            InvoiceFormController controller = loader.getController();
            controller.setAuthenticatedUser(this.authenticatedUser);

            contentArea.getChildren().clear();
            contentArea.getChildren().add(root);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleLogOutOnAction(ActionEvent event) {
        boolean confirmed = AlertUtil.showConfirmation(
                "Exit Session",
                "Confirm Logout",
                "Are you sure you want to end your active clinical session?"
        );

        if (!confirmed) return;

        try {
            FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/lk/ijse/theserenitymentalhealththerapycenter/view/Login.fxml")
            );
            Parent loginRoot = loader.load();

            Stage currentStage = (javafx.stage.Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();

            Scene loginScene = new javafx.scene.Scene(loginRoot);
            currentStage.setScene(loginScene);

            currentStage.centerOnScreen();
            currentStage.show();

            System.out.println(">> Security Context Terminated: User session dropped successfully.");

        } catch (IOException e) {
            AlertUtil.showError(
                    "Navigation Error",
                    "Session Termination Failed",
                    "Unable to map resource routing path back to LoginForm.fxml"
            );
            e.printStackTrace();
        }
    }

    public static UserDTO getAuthenticatedUserSession() {
        return authenticatedUser;
    }

    @FXML
    void handleOpenProfileSettings(MouseEvent event) {
        System.out.println(">> Navigation Engine: Loading user profile configuration workspace pane...");
        setActiveNavigation(null); // Clears sidebar button active style states cleanly
        lblViewTitle.setText("My Profile Management Settings");
        loadView("ProfileForm.fxml");
    }
}