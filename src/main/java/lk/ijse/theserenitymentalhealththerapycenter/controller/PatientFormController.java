package lk.ijse.theserenitymentalhealththerapycenter.controller;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;
import lk.ijse.theserenitymentalhealththerapycenter.bo.BOFactory;
import lk.ijse.theserenitymentalhealththerapycenter.bo.BOType;
import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.PatientBO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.PatientDTO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.UserDTO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.enums.CommonStatus;
import lk.ijse.theserenitymentalhealththerapycenter.util.AlertUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PatientFormController {

    @FXML private Label lblSystemTime;
    @FXML private Label lblLoggedInUser;
    @FXML private TextField txtName;
    @FXML private TextField txtEmail;
    @FXML private TextField txtPhone;
    @FXML private TextArea txtMedicalHistory;
    @FXML private TextField txtSearchProgram;

    @FXML private TableView<PatientDTO> tblPatient;
    @FXML private TableColumn<PatientDTO, Long> colId;
    @FXML private TableColumn<PatientDTO, String> colName;
    @FXML private TableColumn<PatientDTO, String> colEmail;
    @FXML private TableColumn<PatientDTO, String> colPhone;
    @FXML private TableColumn<PatientDTO, LocalDate> colRegDate;
    @FXML private TableColumn<PatientDTO, String> colMedicalHistory;

    // Fetching our concrete Patient Service implementation via the BO Factory
    private final PatientBO patientBO = (PatientBO) BOFactory.getInstance().getBO(BOType.PATIENT);
    private final ObservableList<PatientDTO> patientList = FXCollections.observableArrayList();
    private Long selectedPatientId = null; // Track selected rows for update/delete actions

    @FXML
    public void initialize() {
        startLiveSystemClock();
        initializeTableColumns();
        loadAllActivePatients();
    }

    /**
     * Spawns a background timeline loop to keep the clinical clock accurate to the second.
     */
    private void startLiveSystemClock() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd | hh:mm:ss a");
        Timeline clock = new Timeline(new KeyFrame(Duration.ZERO, e -> {
            lblSystemTime.setText("System Time: " + LocalDateTime.now().format(formatter));
        }), new KeyFrame(Duration.seconds(1)));
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();
    }

    /**
     * Binds the logged-in user profile text dynamically from the dashboard login session.
     */
    public void setSessionUserContext(UserDTO user) {
        if (user != null) {
            lblLoggedInUser.setText(user.getFullName() + " (" + user.getRole().name() + ")");
        }
    }

    private void initializeTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colRegDate.setCellValueFactory(new PropertyValueFactory<>("registrationDate"));
        colMedicalHistory.setCellValueFactory(new PropertyValueFactory<>("medicalHistory"));
    }

    private void loadAllActivePatients() {
        try {
            patientList.clear();
            List<PatientDTO> allPatients = patientBO.getAllActivePatients();
            patientList.addAll(allPatients);
            tblPatient.setItems(patientList);
        } catch (Exception e) {
            AlertUtil.showError(
                    "Database Error",
                    "Registry Load Failure",
                    "Failed to retrieve active client records from the database: " + e.getMessage()
            );
        }
    }

    @FXML
    void btnSaveOnAction(ActionEvent event) {
        try {
            PatientDTO dto = new PatientDTO(
                    null,
                    txtName.getText().trim(),
                    txtEmail.getText().trim(),
                    txtPhone.getText().trim(),
                    txtMedicalHistory.getText().trim(),
                    LocalDate.now(), // Dynamic server-side intake date logging
                    CommonStatus.ACTIVE
            );

            if (patientBO.savePatient(dto)) {
                AlertUtil.showSuccess(
                        "Registration Success",
                        "Intake Completed",
                        "New patient profile has been successfully added to the clinical registry."
                );
                loadAllActivePatients();
                clearFormFields();
            }
        } catch (Exception e) {
            AlertUtil.showWarning(
                    "Validation Error",
                    "Invalid Fields Detected",
                    e.getMessage()
            );
        }
    }

    @FXML
    void btnUpdateOnAction(ActionEvent event) {
        if (selectedPatientId == null) {
            AlertUtil.showWarning(
                    "Selection Error",
                    "No Target Selected",
                    "Please pick a target row from the registry table to modify."
            );
            return;
        }

        try {
            PatientDTO dto = new PatientDTO(
                    selectedPatientId,
                    txtName.getText().trim(),
                    txtEmail.getText().trim(),
                    txtPhone.getText().trim(),
                    txtMedicalHistory.getText().trim(),
                    null, // Keep original date intact in the database layer via managed dirty checking
                    CommonStatus.ACTIVE
            );

            if (patientBO.updatePatient(dto)) {
                AlertUtil.showSuccess(
                        "Profile Modified",
                        "Update Successful",
                        "Patient information has been updated successfully in the system."
                );
                loadAllActivePatients();
                clearFormFields();
            }
        } catch (Exception e) {
            AlertUtil.showWarning(
                    "Update Failure",
                    "Database Modification Dropped",
                    e.getMessage()
            );
        }
    }

    @FXML
    void btnDeleteOnAction(ActionEvent event) {
        if (selectedPatientId == null) {
            AlertUtil.showWarning(
                    "Selection Error",
                    "No Target Selected",
                    "Please pick a target row from the registry table to remove."
            );
            return;
        }

        boolean confirmation = AlertUtil.showConfirmation(
                "Confirm Action",
                "Profile Deactivation Pending",
                "Are you sure you want to soft-delete this patient profile?"
        );
        if (!confirmation) return;

        try {
            if (patientBO.softDeletePatient(selectedPatientId)) {
                AlertUtil.showSuccess(
                        "Profile Purged",
                        "Deactivation Complete",
                        "Patient profile successfully switched to INACTIVE status."
                );
                loadAllActivePatients();
                clearFormFields();
            }
        } catch (Exception e) {
            AlertUtil.showError(
                    "Deletion Error",
                    "Database Drop Failure",
                    e.getMessage()
            );
        }
    }

    @FXML
    void txtSearchProgramOnKeyReleased(KeyEvent event) {
        String filterQuery = txtSearchProgram.getText().trim();
        if (filterQuery.isEmpty()) {
            loadAllActivePatients();
            return;
        }

        try {
            List<PatientDTO> filteredList = patientBO.searchPatientsByTherapyProgram(filterQuery);
            tblPatient.setItems(FXCollections.observableArrayList(filteredList));
        } catch (Exception e) {
            System.err.println("Live filter exception query error: " + e.getMessage());
        }
    }

    @FXML
    void tblPatientOnMouseClicked(MouseEvent event) {
        PatientDTO selectedPatient = tblPatient.getSelectionModel().getSelectedItem();
        if (selectedPatient != null) {
            selectedPatientId = selectedPatient.getId();
            txtName.setText(selectedPatient.getName());
            txtEmail.setText(selectedPatient.getEmail());
            txtPhone.setText(selectedPatient.getPhone());
            txtMedicalHistory.setText(selectedPatient.getMedicalHistory());
        }
    }

    @FXML
    void btnClearOnAction(ActionEvent event) {
        clearFormFields();
    }

    private void clearFormFields() {
        selectedPatientId = null;
        txtName.clear();
        txtEmail.clear();
        txtPhone.clear();
        txtMedicalHistory.clear();
        tblPatient.getSelectionModel().clearSelection();
    }
}