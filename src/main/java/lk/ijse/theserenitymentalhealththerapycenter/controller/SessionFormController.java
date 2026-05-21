package lk.ijse.theserenitymentalhealththerapycenter.controller;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.StringConverter;
import lk.ijse.theserenitymentalhealththerapycenter.bo.BOFactory;
import lk.ijse.theserenitymentalhealththerapycenter.bo.BOType;
import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.PatientBO;
import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.TherapistBO;
import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.TherapyProgramBO;
import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.TherapySessionBO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.PatientDTO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.TherapistDTO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.TherapyProgramDTO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.TherapySessionDTO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.enums.SessionStatus;
import lk.ijse.theserenitymentalhealththerapycenter.util.AlertUtil;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SessionFormController {

    @FXML private Button btnBook;
    @FXML private Button btnCancel;
    @FXML private Button btnClear;

    @FXML private ListView<PatientDTO> lstPatients;
    @FXML private ComboBox<TherapyProgramDTO> cmbProgramId;
    @FXML private ComboBox<SessionStatus> cmbSessionStatus;
    @FXML private ComboBox<TherapistDTO> cmbTherapistId;

    @FXML private TableColumn<TherapySessionDTO, LocalDateTime> colDateTime;
    @FXML private TableColumn<TherapySessionDTO, Long> colId;
    @FXML private TableColumn<TherapySessionDTO, String> colPatient;
    @FXML private TableColumn<TherapySessionDTO, String> colProgram;
    @FXML private TableColumn<TherapySessionDTO, SessionStatus> colStatus;
    @FXML private TableColumn<TherapySessionDTO, String> colTherapist;

    @FXML private DatePicker dtSessionDate;
    @FXML private TableView<TherapySessionDTO> tblSessions;
    @FXML private TextField txtSearchSessions;
    @FXML private TextField txtSessionTime;

    private final TherapySessionBO sessionBO = (TherapySessionBO) BOFactory.getInstance().getBO(BOType.THERAPY_SESSION);
    private final PatientBO patientBO = (PatientBO) BOFactory.getInstance().getBO(BOType.PATIENT);
    private final TherapistBO therapistBO = (TherapistBO) BOFactory.getInstance().getBO(BOType.THERAPIST);
    private final TherapyProgramBO programBO = (TherapyProgramBO) BOFactory.getInstance().getBO(BOType.THERAPY_PROGRAM);

    private final ObservableList<TherapySessionDTO> sessionList = FXCollections.observableArrayList();

    // Central state tracking map for structural checkbox selection indexes
    private final Map<Long, SimpleBooleanProperty> patientCheckedStates = new HashMap<>();

    private List<PatientDTO> activePatientsCache;
    private List<TherapistDTO> activeTherapistsCache;
    private List<TherapyProgramDTO> activeProgramsCache;

    private Long selectedSessionId = null;

    @FXML
    public void initialize() {
        initializeTableColumns();
        setupDisplayCellConverters();
        loadAllChoiceSelectors();
        loadAllScheduledSessions();
    }

    private void initializeTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colPatient.setCellValueFactory(new PropertyValueFactory<>("formattedPatientNames"));
        colTherapist.setCellValueFactory(new PropertyValueFactory<>("therapistName"));
        colProgram.setCellValueFactory(new PropertyValueFactory<>("programName"));
        colDateTime.setCellValueFactory(new PropertyValueFactory<>("sessionDateTime"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    private void setupDisplayCellConverters() {
        // Enforce a dynamic CheckBox factory architecture straight onto the ListView container
        lstPatients.setCellFactory(CheckBoxListCell.forListView(patient -> {
            if (patient == null) return null;
            // Map observation keys securely to individual database ID numbers
            if (!patientCheckedStates.containsKey(patient.getId())) {
                patientCheckedStates.put(patient.getId(), new SimpleBooleanProperty(false));
            }
            return patientCheckedStates.get(patient.getId());
        }, new StringConverter<>() {
            @Override public String toString(PatientDTO object) { return object == null ? "" : object.getId() + " - " + object.getName(); }
            @Override public PatientDTO fromString(String string) { return null; }
        }));

        // Disable standard focus tracking rows since operations are entirely checkbox-driven
        //  Add this to instantly clear selection focus and disable row highlighting:
        lstPatients.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.intValue() >= 0) {
                javafx.application.Platform.runLater(() -> lstPatients.getSelectionModel().clearSelection());
            }
        });

        cmbTherapistId.setConverter(new StringConverter<>() {
            @Override public String toString(TherapistDTO object) { return object == null ? "" : object.getId() + " - " + object.getName(); }
            @Override public TherapistDTO fromString(String string) { return null; }
        });

        cmbProgramId.setConverter(new StringConverter<>() {
            @Override public String toString(TherapyProgramDTO object) { return object == null ? "" : object.getId() + " - " + object.getName(); }
            @Override public TherapyProgramDTO fromString(String string) { return null; }
        });
    }

    private void loadAllChoiceSelectors() {
        try {
            activePatientsCache = patientBO.getAllActivePatients();

            // Clear prior states and populate tracking indicators safely
            patientCheckedStates.clear();
            for (PatientDTO patient : activePatientsCache) {
                patientCheckedStates.put(patient.getId(), new SimpleBooleanProperty(false));
            }

            lstPatients.setItems(FXCollections.observableArrayList(activePatientsCache));

            activeTherapistsCache = therapistBO.getAllActiveTherapists();
            cmbTherapistId.setItems(FXCollections.observableArrayList(activeTherapistsCache));

            activeProgramsCache = programBO.getAllActivePrograms();
            cmbProgramId.setItems(FXCollections.observableArrayList(activeProgramsCache));

            cmbSessionStatus.setItems(FXCollections.observableArrayList(SessionStatus.values()));
            cmbSessionStatus.setValue(SessionStatus.SCHEDULED);

        } catch (Exception e) {
            AlertUtil.showError("Initialization Error", "Choice Mappings Selection Failed", e.getMessage());
        }
    }

    private void loadAllScheduledSessions() {
        try {
            sessionList.clear();
            List<TherapySessionDTO> activeSessions = sessionBO.getAllSessionsWithFullDetails();
            sessionList.addAll(activeSessions);
            tblSessions.setItems(sessionList);
        } catch (Exception e) {
            AlertUtil.showError("Database Error", "Appointments Registry Load Failure", e.getMessage());
        }
    }

    @FXML
    void btnBookOnAction(ActionEvent event) {
        // Harvest all patient profiles whose checked properties are true
        List<Long> gatheredPatientIds = patientCheckedStates.entrySet().stream()
                .filter(entry -> entry.getValue().get())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        if (gatheredPatientIds.isEmpty() || cmbTherapistId.getValue() == null ||
                cmbProgramId.getValue() == null || dtSessionDate.getValue() == null || txtSessionTime.getText().trim().isEmpty()) {
            AlertUtil.showWarning("Validation Error", "Missing parameters", "All relational assignment criteria fields (including at least one selected patient) are mandatory.");
            return;
        }

        LocalDateTime resolvedDateTime;
        try {
            LocalTime parsedTime = LocalTime.parse(txtSessionTime.getText().trim(), DateTimeFormatter.ofPattern("HH:mm"));
            resolvedDateTime = LocalDateTime.of(dtSessionDate.getValue(), parsedTime);
        } catch (DateTimeParseException e) {
            AlertUtil.showWarning("Validation Error", "Invalid Time Pattern", "Please use the strict standard 24-hour time notation layout (e.g., 14:30).");
            return;
        }

        TherapySessionDTO dto = new TherapySessionDTO();
        dto.setTherapistId(cmbTherapistId.getValue().getId());
        dto.setProgramId(cmbProgramId.getValue().getId());
        dto.setSessionDateTime(resolvedDateTime);
        dto.setStatus(cmbSessionStatus.getValue());
        dto.setPatientIds(gatheredPatientIds);

        if (selectedSessionId == null) {
            handleBooking(dto);
        } else {
            dto.setId(selectedSessionId);
            handleRescheduling(dto);
        }
    }

    private void handleBooking(TherapySessionDTO dto) {
        try {
            if (sessionBO.bookSession(dto)) {
                AlertUtil.showSuccess("Booking Complete", "Appointment Fixed", "Therapy session room locked and multiple patients assigned successfully.");
                loadAllScheduledSessions();
                clearFormFields();
            }
        } catch (Exception e) {
            AlertUtil.showWarning("Booking Dropped", "Practitioner Clash", e.getMessage());
        }
    }

    private void handleRescheduling(TherapySessionDTO dto) {
        try {
            if (sessionBO.rescheduleSession(dto)) {
                AlertUtil.showSuccess("Reschedule Complete", "Timestamp Adjusted", "Therapy session entry boundaries shifted completely.");
                loadAllScheduledSessions();
                clearFormFields();
            }
        } catch (Exception e) {
            AlertUtil.showWarning("Reschedule Dropped", "Availability Exception", e.getMessage());
        }
    }

    @FXML
    void btnCancelOnAction(ActionEvent event) {
        if (selectedSessionId == null) {
            AlertUtil.showWarning("Selection Error", "No Target Selected", "Please choose a session to cancel.");
            return;
        }

        boolean confirm = AlertUtil.showConfirmation("Confirm Action", "Appointment Cancellation Pending", "Soft-delete and drop this single source of truth row entry?");
        if (!confirm) return;

        try {
            if (sessionBO.cancelSession(selectedSessionId)) {
                AlertUtil.showSuccess("Purge Complete", "Session Cancelled", "Appointment entry and corresponding records updated successfully.");
                loadAllScheduledSessions();
                clearFormFields();
            }
        } catch (Exception e) {
            AlertUtil.showError("Database Drop Failure", "Purge Execution Dropped", e.getMessage());
        }
    }

    @FXML
    void btnClearOnAction(ActionEvent event) {
        clearFormFields();
    }

    @FXML
    void tblSessionsOnMouseClicked(MouseEvent event) {
        TherapySessionDTO selectedSession = tblSessions.getSelectionModel().getSelectedItem();
        if (selectedSession != null) {
            selectedSessionId = selectedSession.getId();

            // Reverse Synchronization: Reset all checkboxes and check only those matching the active session DTO list
            patientCheckedStates.values().forEach(property -> property.set(false));

            if (selectedSession.getPatientIds() != null) {
                for (Long id : selectedSession.getPatientIds()) {
                    if (patientCheckedStates.containsKey(id)) {
                        patientCheckedStates.get(id).set(true);
                    }
                }
            }

            if (activeTherapistsCache != null) {
                activeTherapistsCache.stream()
                        .filter(t -> t.getId().equals(selectedSession.getTherapistId()))
                        .findFirst().ifPresent(cmbTherapistId::setValue);
            }

            if (activeProgramsCache != null) {
                activeProgramsCache.stream()
                        .filter(tp -> tp.getId().equals(selectedSession.getProgramId()))
                        .findFirst().ifPresent(cmbProgramId::setValue);
            }

            dtSessionDate.setValue(selectedSession.getSessionDateTime().toLocalDate());
            txtSessionTime.setText(selectedSession.getSessionDateTime().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")));
            cmbSessionStatus.setValue(selectedSession.getStatus());

            btnBook.setText("Reschedule Session");

            if (event.getClickCount() == 2) {
                openSessionDetailCard(selectedSession);
            }
        }
    }

    @FXML
    void txtSearchSessionsOnKeyReleased(KeyEvent event) {
        String query = txtSearchSessions.getText().trim().toLowerCase();

        if (event.getCode() == KeyCode.ENTER && !tblSessions.getItems().isEmpty()) {
            TherapySessionDTO topMatchedSession = tblSessions.getItems().get(0);
            openSessionDetailCard(topMatchedSession);
            return;
        }

        if (query.isEmpty()) {
            loadAllScheduledSessions();
            return;
        }

        ObservableList<TherapySessionDTO> filtered = sessionList.stream()
                .filter(s -> (s.getFormattedPatientNames() != null && s.getFormattedPatientNames().toLowerCase().contains(query)) ||
                        (s.getTherapistName() != null && s.getTherapistName().toLowerCase().contains(query)))
                .collect(Collectors.toCollection(FXCollections::observableArrayList));

        tblSessions.setItems(filtered);
    }

    private void openSessionDetailCard(TherapySessionDTO session) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/lk/ijse/theserenitymentalhealththerapycenter/view/TherapySessionDetailCard.fxml"));
            AnchorPane pane = loader.load();

            TherapySessionDetailCardController controller = loader.getController();
            controller.setSessionData(session);

            Stage modalStage = new Stage();
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.initStyle(StageStyle.UTILITY);
            modalStage.setTitle("Clinical Session Verification Card");
            modalStage.setScene(new Scene(pane));
            modalStage.setResizable(false);
            modalStage.centerOnScreen();
            modalStage.showAndWait();

        } catch (IOException e) {
            AlertUtil.showError("System Error", "Modal View Load Failure", "Unable to launch secondary view card layout.");
            e.printStackTrace();
        }
    }

    @FXML void cmbProgramOnAction(ActionEvent event) {}
    @FXML void cmbTherapistOnAction(ActionEvent event) {}

    private void clearFormFields() {
        selectedSessionId = null;

        // Wipe all checked selections out of the global map
        patientCheckedStates.values().forEach(property -> property.set(false));

        cmbTherapistId.setValue(null);
        cmbProgramId.setValue(null);
        dtSessionDate.setValue(null);
        txtSessionTime.clear();
        txtSearchSessions.clear();
        cmbSessionStatus.setValue(SessionStatus.SCHEDULED);
        btnBook.setText("Book Session");
        tblSessions.getSelectionModel().clearSelection();
    }
}