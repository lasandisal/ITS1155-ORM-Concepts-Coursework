package lk.ijse.theserenitymentalhealththerapycenter.controller;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import lk.ijse.theserenitymentalhealththerapycenter.bo.BOFactory;
import lk.ijse.theserenitymentalhealththerapycenter.bo.BOType;
import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.PatientBO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.PatientDTO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.UserDTO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.enums.CommonStatus;
import lk.ijse.theserenitymentalhealththerapycenter.util.AlertUtil;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PatientFormController {

    @FXML private Button btnClear;
    @FXML private Button btnDelete;
    @FXML private Button btnSave;

    @FXML private TableColumn<PatientDTO, String> colEmail;
    @FXML private TableColumn<PatientDTO, Long> colId;
    @FXML private TableColumn<PatientDTO, String> colMedicalHistory;
    @FXML private TableColumn<PatientDTO, String> colName;
    @FXML private TableColumn<PatientDTO, String> colPhone;
    @FXML private TableColumn<PatientDTO, LocalDate> colRegDate;

    @FXML private TableView<PatientDTO> tblPatient;

    @FXML private TextField txtEmail;
    @FXML private TextArea txtMedicalHistory;
    @FXML private TextField txtName;
    @FXML private TextField txtPhone;
    @FXML private TextField txtSearchProgram;

    private final PatientBO patientBO = (PatientBO) BOFactory.getInstance().getBO(BOType.PATIENT);
    private final ObservableList<PatientDTO> patientList = FXCollections.observableArrayList();
    private Long selectedPatientId = null;

    @FXML
    public void initialize() {
        initializeTableColumns();
        loadAllActivePatients();
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
            AlertUtil.showError("Database Error", "Registry Load Failure", e.getMessage());
        }
    }

    @FXML
    void btnSaveOnAction(ActionEvent event) {
        if (selectedPatientId == null) {
            handleSavePatient();
        } else {
            handleUpdatePatient();
        }
    }

    private void handleSavePatient() {
        try {
            PatientDTO dto = new PatientDTO(
                    null,
                    txtName.getText().trim(),
                    txtEmail.getText().trim(),
                    txtPhone.getText().trim(),
                    txtMedicalHistory.getText().trim(),
                    LocalDate.now(),
                    CommonStatus.ACTIVE
            );

            if (patientBO.savePatient(dto)) {
                AlertUtil.showSuccess("Registration Success", "Intake Completed", "New patient profile successfully added.");
                loadAllActivePatients();
                clearFormFields();
            }
        } catch (Exception e) {
            AlertUtil.showWarning("Validation Error", "Invalid Fields", e.getMessage());
        }
    }

    private void handleUpdatePatient() {
        try {
            PatientDTO dto = new PatientDTO(
                    selectedPatientId,
                    txtName.getText().trim(),
                    txtEmail.getText().trim(),
                    txtPhone.getText().trim(),
                    txtMedicalHistory.getText().trim(),
                    null,
                    CommonStatus.ACTIVE
            );

            if (patientBO.updatePatient(dto)) {
                AlertUtil.showSuccess("Profile Modified", "Update Successful", "Patient information updated successfully.");
                loadAllActivePatients();
                clearFormFields();
            }
        } catch (Exception e) {
            AlertUtil.showWarning("Update Failure", "Modification Dropped", e.getMessage());
        }
    }

    @FXML
    void btnDeleteOnAction(ActionEvent event) {
        if (selectedPatientId == null) {
            AlertUtil.showWarning("Selection Error", "No Target Selected", "Please select a patient from the table.");
            return;
        }

        boolean confirmation = AlertUtil.showConfirmation("Confirm Action", "Profile Deactivation Pending", "Soft-delete this patient profile?");
        if (!confirmation) return;

        try {
            if (patientBO.softDeletePatient(selectedPatientId)) {
                AlertUtil.showSuccess("Profile Purged", "Deactivation Complete", "Patient profile set to INACTIVE.");
                loadAllActivePatients();
                clearFormFields();
            }
        } catch (Exception e) {
            AlertUtil.showError("Deletion Error", "Database Drop Failure", e.getMessage());
        }
    }

    @FXML
    void btnClearOnAction(ActionEvent event) {
        clearFormFields();
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

            btnSave.setText("Update Profile");

            if (event.getClickCount() == 2) {
                openPatientDetailCard(selectedPatient);
            }
        }
    }

    @FXML
    void txtSearchProgramOnKeyReleased(KeyEvent event) {
        String filterQuery = txtSearchProgram.getText().trim();

        if (event.getCode() == KeyCode.ENTER && !tblPatient.getItems().isEmpty()) {
            PatientDTO topMatchedPatient = tblPatient.getItems().get(0);
            openPatientDetailCard(topMatchedPatient);
            return;
        }

        if (filterQuery.isEmpty()) {
            loadAllActivePatients();
            return;
        }

        try {
            List<PatientDTO> filteredList = patientBO.searchPatientsByTherapyProgram(filterQuery);
            tblPatient.setItems(FXCollections.observableArrayList(filteredList));
        } catch (Exception e) {
            System.err.println("Search filter error: " + e.getMessage());
        }
    }

    private void openPatientDetailCard(PatientDTO patient) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/lk/ijse/theserenitymentalhealththerapycenter/view/PatientDetailCard.fxml"));
            AnchorPane pane = loader.load();

            PatientDetailCardController controller = loader.getController();
            controller.setPatientData(patient);

            Stage modalStage = new Stage();
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.initStyle(StageStyle.UTILITY);
            modalStage.setTitle("Clinical Registry Summary Card");
            modalStage.setScene(new Scene(pane));
            modalStage.setResizable(false);
            modalStage.centerOnScreen();
            modalStage.showAndWait();

        } catch (IOException e) {
            AlertUtil.showError("System Error", "Modal View Load Failure", "Unable to launch secondary view card layout.");
            e.printStackTrace();
        }
    }

    private void clearFormFields() {
        selectedPatientId = null;
        txtName.clear();
        txtEmail.clear();
        txtPhone.clear();
        txtMedicalHistory.clear();
        btnSave.setText("Save Intake");
        tblPatient.getSelectionModel().clearSelection();
    }
}