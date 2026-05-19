package lk.ijse.theserenitymentalhealththerapycenter.controller;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
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

    // ✅ Track your global layout header injections
    @FXML private Label lblViewTitle;
    @FXML private Label lblSystemTime;
    @FXML private Label lblLoggedInUser;

    private UserDTO authenticatedUser;

    @FXML
    public void initialize() {
        startGlobalClock();
    }

    /**
     * Spawns a background thread-safe loop to keep the clinical clock accurate.
     */
    private void startGlobalClock() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd | hh:mm:ss a");
        Timeline clock = new Timeline(new KeyFrame(Duration.ZERO, e -> {
            lblSystemTime.setText("System Time: " + LocalDateTime.now().format(formatter));
        }), new KeyFrame(Duration.seconds(1)));
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();
    }

    /**
     * Configure security visibility profiles once upon direct session validation.
     */
    public void configureAccessPrivileges(UserDTO activeUser) {
        if (activeUser == null) return;
        this.authenticatedUser = activeUser;

        // ✅ Centralized injection setup for user role badge display
        lblLoggedInUser.setText(activeUser.getFullName() + " (" + activeUser.getRole().name() + ")");

        UserRole role = activeUser.getRole();
        switch (role) {
            case ADMIN -> {
                btnTherapistManage.setVisible(true);
                btnTherapyCatalog.setVisible(true);
                btnGenerateInvoice.setVisible(false);
                btnPaymentTracking.setVisible(false);
            }
            case RECEPTIONIST -> {
                btnTherapistManage.setVisible(false);
                btnTherapyCatalog.setVisible(false);
                btnGenerateInvoice.setVisible(true);
                btnPaymentTracking.setVisible(true);
            }
            default -> {
                btnTherapistManage.setVisible(false);
                btnTherapyCatalog.setVisible(false);
                btnGenerateInvoice.setVisible(false);
                btnPaymentTracking.setVisible(false);
            }
        }

        // Set default system entry view dashboard viewport configuration maps
        setActiveNavigation(btnPatientManage);
        lblViewTitle.setText("Patient Intake & Clinical Registry");
        loadView("PatientForm.fxml");
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
        loadView("InvoiceForm.fxml");
    }

    @FXML
    void handleLogOutOnAction(ActionEvent event) {
        AlertUtil.showInformation("Logout", null, "Logout execution sequence routing will drop context maps here.");
    }
}