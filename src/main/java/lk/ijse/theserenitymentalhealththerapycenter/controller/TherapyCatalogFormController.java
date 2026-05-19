package lk.ijse.theserenitymentalhealththerapycenter.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import lk.ijse.theserenitymentalhealththerapycenter.bo.BOFactory;
import lk.ijse.theserenitymentalhealththerapycenter.bo.BOType;
import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.TherapyProgramBO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.TherapyProgramDTO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.enums.CommonStatus;
import lk.ijse.theserenitymentalhealththerapycenter.util.AlertUtil;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class TherapyCatalogFormController {

    @FXML private Button btnClear;
    @FXML private Button btnDelete;
    @FXML private Button btnSave; // Dynamically handles Save/Update states

    @FXML private ComboBox<CommonStatus> cmbStatus;
    @FXML private TableColumn<TherapyProgramDTO, String> colDuration;
    @FXML private TableColumn<TherapyProgramDTO, Double> colFee;
    @FXML private TableColumn<TherapyProgramDTO, String> colId;
    @FXML private TableColumn<TherapyProgramDTO, String> colName;
    @FXML private TableColumn<TherapyProgramDTO, CommonStatus> colStatus;
    @FXML private TableView<TherapyProgramDTO> tblTherapyProgram;

    @FXML private TextField txtDuration;
    @FXML private TextField txtFee;
    @FXML private TextField txtProgramId;
    @FXML private TextField txtProgramName;
    @FXML private TextField txtSearchProgram;

    private final TherapyProgramBO programBO = (TherapyProgramBO) BOFactory.getInstance().getBO(BOType.THERAPY_PROGRAM);
    private final ObservableList<TherapyProgramDTO> programList = FXCollections.observableArrayList();
    private boolean isUpdateState = false; // Flag to track dynamic button states

    @FXML
    public void initialize() {
        initializeTableColumns();
        loadComboBoxData();
        loadAllActivePrograms();
    }

    private void initializeTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colDuration.setCellValueFactory(new PropertyValueFactory<>("duration"));
        colFee.setCellValueFactory(new PropertyValueFactory<>("fee"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    private void loadComboBoxData() {
        cmbStatus.setItems(FXCollections.observableArrayList(CommonStatus.values()));
        cmbStatus.setValue(CommonStatus.ACTIVE);
    }

    private void loadAllActivePrograms() {
        try {
            programList.clear();
            List<TherapyProgramDTO> activePrograms = programBO.getAllActivePrograms();
            programList.addAll(activePrograms);
            tblTherapyProgram.setItems(programList);
        } catch (Exception e) {
            AlertUtil.showError("Database Error", "Catalog Load Failure", "Could not retrieve treatment plans: " + e.getMessage());
        }
    }

    @FXML
    void btnSaveOnAction(ActionEvent event) {
        String feeText = txtFee.getText().trim();
        if (feeText.isEmpty()) {
            AlertUtil.showWarning("Validation Error", "Missing Field", "Please enter a valid base processing fee.");
            return;
        }

        double feeValue;
        try {
            feeValue = Double.parseDouble(feeText);
        } catch (NumberFormatException e) {
            AlertUtil.showWarning("Validation Error", "Type Mismatch", "Fee must be a valid numeric value.");
            return;
        }

        TherapyProgramDTO dto = new TherapyProgramDTO();
        dto.setId(txtProgramId.getText().trim());
        dto.setName(txtProgramName.getText().trim());
        dto.setDuration(txtDuration.getText().trim());
        dto.setFee(feeValue);
        dto.setStatus(cmbStatus.getValue());

        if (!isUpdateState) {
            handleSave(dto);
        } else {
            handleUpdate(dto);
        }
    }

    private void handleSave(TherapyProgramDTO dto) {
        try {
            if (programBO.saveProgram(dto)) {
                AlertUtil.showSuccess("Catalog Added", "Registration Success", "New therapy program successfully added.");
                loadAllActivePrograms();
                clearFormFields();
            }
        } catch (Exception e) {
            AlertUtil.showWarning("Validation Error", "Save Dropped", e.getMessage());
        }
    }

    private void handleUpdate(TherapyProgramDTO dto) {
        try {
            if (programBO.updateProgram(dto)) {
                AlertUtil.showSuccess("Catalog Modified", "Update Success", "Therapy program details modified successfully.");
                loadAllActivePrograms();
                clearFormFields();
            }
        } catch (Exception e) {
            AlertUtil.showWarning("Update Error", "Modification Dropped", e.getMessage());
        }
    }

    @FXML
    void btnDeleteOnAction(ActionEvent event) {
        String targetId = txtProgramId.getText().trim();
        if (targetId.isEmpty()) {
            AlertUtil.showWarning("Selection Error", "No Target Selected", "Please select a catalog row to remove.");
            return;
        }

        boolean confirmation = AlertUtil.showConfirmation("Confirm Action", "Catalog Drop Pending", "Are you sure you want to deactivate this therapy program framework?");
        if (!confirmation) return;

        try {
            if (programBO.softDeleteProgram(targetId)) {
                AlertUtil.showSuccess("Catalog Purged", "Deactivation Complete", "Program state switched to INACTIVE status.");
                loadAllActivePrograms();
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
    void tblTherapyProgramOnMouseClicked(MouseEvent event) {
        TherapyProgramDTO selectedProgram = tblTherapyProgram.getSelectionModel().getSelectedItem();
        if (selectedProgram != null) {
            // ID field mapping set to uneditable to enforce key constraints on updates
            txtProgramId.setText(selectedProgram.getId());
            txtProgramId.setEditable(false);

            txtProgramName.setText(selectedProgram.getName());
            txtDuration.setText(selectedProgram.getDuration());
            txtFee.setText(String.valueOf(selectedProgram.getFee()));
            cmbStatus.setValue(selectedProgram.getStatus());

            isUpdateState = true; // Flips your existing structural tracking state boolean flag
            btnSave.setText("Update Program");

            // Double-click triggers the modal detail treatment specifications card
            if (event.getClickCount() == 2) {
                openTherapyProgramDetailCard(selectedProgram);
            }
        }
    }

    @FXML
    void txtSearchProgramOnKeyReleased(KeyEvent event) {
        String filterQuery = txtSearchProgram.getText().trim().toLowerCase();

        // When user types and hits ENTER, grab top record result and render separate modal card view
        if (event.getCode() == javafx.scene.input.KeyCode.ENTER && !tblTherapyProgram.getItems().isEmpty()) {
            TherapyProgramDTO topMatchedProgram = tblTherapyProgram.getItems().get(0);
            openTherapyProgramDetailCard(topMatchedProgram);
            return;
        }

        if (filterQuery.isEmpty()) {
            loadAllActivePrograms();
            return;
        }

        // Clean inline list-filtering logic stream mapping your specific catalog structure
        javafx.collections.ObservableList<TherapyProgramDTO> filteredList = programList.stream()
                .filter(p -> p.getId().toLowerCase().contains(filterQuery) ||
                        p.getName().toLowerCase().contains(filterQuery))
                .collect(java.util.stream.Collectors.toCollection(javafx.collections.FXCollections::observableArrayList));

        tblTherapyProgram.setItems(filteredList);
    }

    private void openTherapyProgramDetailCard(TherapyProgramDTO program) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/lk/ijse/theserenitymentalhealththerapycenter/view/TherapyProgramDetailCard.fxml"));
            javafx.scene.Parent root = loader.load();

            TherapyProgramDetailCardController controller = loader.getController();
            controller.setProgramData(program);

            Stage modalStage = new Stage();
            modalStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            modalStage.initStyle(javafx.stage.StageStyle.UTILITY);
            modalStage.setTitle("Therapy Program Framework Specifications");
            modalStage.setScene(new javafx.scene.Scene(root));
            modalStage.setResizable(false);
            modalStage.centerOnScreen();
            modalStage.showAndWait();

        } catch (IOException e) {
            AlertUtil.showError("System Error", "Modal View Load Failure", "Unable to launch secondary view card layout.");
            e.printStackTrace();
        }
    }

    private void clearFormFields() {
        txtProgramId.clear();
        txtProgramId.setEditable(true);
        txtProgramName.clear();
        txtDuration.clear();
        txtFee.clear();
        txtSearchProgram.clear();
        cmbStatus.setValue(CommonStatus.ACTIVE);

        isUpdateState = false;
        btnSave.setText("Save Program");
        tblTherapyProgram.getSelectionModel().clearSelection();
    }
}