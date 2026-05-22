package lk.ijse.theserenitymentalhealththerapycenter.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lk.ijse.theserenitymentalhealththerapycenter.bo.BOFactory;
import lk.ijse.theserenitymentalhealththerapycenter.bo.BOType;
import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.TherapistBO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.TherapistDTO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.enums.CommonStatus;
import lk.ijse.theserenitymentalhealththerapycenter.util.AlertUtil;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class TherapistFormController {

    @FXML private Button btnClear;
    @FXML private Button btnDelete;
    @FXML private Button btnSave;
    @FXML private ComboBox<CommonStatus> cmbStatus;

    @FXML private TableColumn<TherapistDTO, String> colEmail;
    @FXML private TableColumn<TherapistDTO, Long> colId;
    @FXML private TableColumn<TherapistDTO, String> colName;
    @FXML private TableColumn<TherapistDTO, String> colPhone;
    @FXML private TableColumn<TherapistDTO, String> colSpecialization;
    @FXML private TableColumn<TherapistDTO, CommonStatus> colStatus;
    @FXML private TableView<TherapistDTO> tblTherapist;

    @FXML private TextField txtEmail;
    @FXML private TextField txtName;
    @FXML private TextField txtPhone;
    @FXML private TextField txtSearch;
    @FXML private TextField txtSpecialization;

    private final TherapistBO therapistBO = (TherapistBO) BOFactory.getInstance().getBO(BOType.THERAPIST);
    private final ObservableList<TherapistDTO> therapistList = FXCollections.observableArrayList();
    private Long selectedTherapistId = null;

    @FXML
    public void initialize() {
        initializeTableColumns();
        initializeComboBox();
        loadAllActiveTherapists();
        btnDelete.setDisable(true);
        btnSave.setText("Save Intake"); // Aligned with Patient layout
    }

    private void initializeTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colSpecialization.setCellValueFactory(new PropertyValueFactory<>("specialization"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    private void initializeComboBox() {
        cmbStatus.setItems(FXCollections.observableArrayList(CommonStatus.values()));
        cmbStatus.setValue(CommonStatus.ACTIVE);
    }

    private void loadAllActiveTherapists() {
        try {
            therapistList.clear();
            List<TherapistDTO> allTherapists = therapistBO.getAllActiveTherapists();
            therapistList.addAll(allTherapists);
            tblTherapist.setItems(therapistList);
        } catch (Exception e) {
            AlertUtil.showError("Database Error", "Registry Load Failure", e.getMessage());
        }
    }

    @FXML
    void btnSaveOnAction(ActionEvent event) {
        if (selectedTherapistId == null) {
            handleSaveTherapist();
        } else {
            handleUpdateTherapist();
        }
    }

    private void handleSaveTherapist() {
        try {
            TherapistDTO dto = new TherapistDTO(
                    null,
                    txtName.getText().trim(),
                    txtSpecialization.getText().trim(),
                    txtEmail.getText().trim(),
                    txtPhone.getText().trim(),
                    cmbStatus.getValue()
            );

            if (therapistBO.saveTherapist(dto)) {
                AlertUtil.showSuccess("Registration Success", "Intake Completed", "New practitioner profile successfully added.");
                loadAllActiveTherapists();
                clearFormFields();
            }
        } catch (Exception e) {
            AlertUtil.showWarning("Validation Error", "Invalid Fields", e.getMessage());
        }
    }

    private void handleUpdateTherapist() {
        try {
            TherapistDTO dto = new TherapistDTO(
                    selectedTherapistId,
                    txtName.getText().trim(),
                    txtSpecialization.getText().trim(),
                    txtEmail.getText().trim(),
                    txtPhone.getText().trim(),
                    cmbStatus.getValue()
            );

            if (therapistBO.updateTherapist(dto)) {
                AlertUtil.showSuccess("Profile Modified", "Update Successful", "Therapist information updated successfully.");
                loadAllActiveTherapists();
                clearFormFields();
            }
        } catch (Exception e) {
            AlertUtil.showWarning("Update Failure", "Modification Dropped", e.getMessage());
        }
    }

    @FXML
    void btnDeleteOnAction(ActionEvent event) {
        if (selectedTherapistId == null) {
            AlertUtil.showWarning("Selection Error", "No Target Selected", "Please select a therapist from the table.");
            return;
        }

        boolean confirmation = AlertUtil.showConfirmation("Confirm Action", "Profile Deactivation Pending", "Soft-delete this practitioner profile?");
        if (!confirmation) return;

        try {
            if (therapistBO.softDeleteTherapist(selectedTherapistId)) {
                AlertUtil.showSuccess("Profile Purged", "Deactivation Complete", "Therapist profile set to INACTIVE.");
                loadAllActiveTherapists();
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
    void tblTherapistOnMouseClicked(MouseEvent event) {
        TherapistDTO selectedTherapist = tblTherapist.getSelectionModel().getSelectedItem();
        if (selectedTherapist != null) {
            selectedTherapistId = selectedTherapist.getId();
            txtName.setText(selectedTherapist.getName());
            txtSpecialization.setText(selectedTherapist.getSpecialization());
            txtEmail.setText(selectedTherapist.getEmail());
            txtPhone.setText(selectedTherapist.getPhone());
            cmbStatus.setValue(selectedTherapist.getStatus());

            btnSave.setText("Update Profile"); // Changed label text to match profile update context exactly
            btnDelete.setDisable(false);

            if (event.getClickCount() == 2) {
                openTherapistDetailCard(selectedTherapist);
            }
        }
    }

    @FXML
    void txtSearchOnKeyReleased(KeyEvent event) {
        String filterQuery = txtSearch.getText().trim();

        if (event.getCode() == KeyCode.ENTER && !tblTherapist.getItems().isEmpty()) {
            TherapistDTO topMatchedTherapist = tblTherapist.getItems().get(0);
            openTherapistDetailCard(topMatchedTherapist);
            return;
        }

        if (filterQuery.isEmpty()) {
            loadAllActiveTherapists();
            return;
        }

        ObservableList<TherapistDTO> filteredList = therapistList.stream()
                .filter(t -> t.getName().toLowerCase().contains(filterQuery.toLowerCase()) ||
                        t.getSpecialization().toLowerCase().contains(filterQuery.toLowerCase()))
                .collect(Collectors.toCollection(FXCollections::observableArrayList));

        tblTherapist.setItems(filteredList);
    }

    private void openTherapistDetailCard(TherapistDTO therapist) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/lk/ijse/theserenitymentalhealththerapycenter/view/TherapistDetailCard.fxml"));
            Parent root = loader.load();

            TherapistDetailCardController controller = loader.getController();
            controller.setTherapistData(therapist);

            Stage modalStage = new Stage();
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.initStyle(StageStyle.UTILITY);
            modalStage.setTitle("Practitioner Registry Profile");
            modalStage.setScene(new Scene(root));
            modalStage.setResizable(false);
            modalStage.centerOnScreen();
            modalStage.showAndWait();

        } catch (IOException e) {
            AlertUtil.showError("System Error", "Modal View Load Failure", "Unable to launch secondary view card layout.");
            e.printStackTrace();
        }
    }

    private void clearFormFields() {
        selectedTherapistId = null;
        txtName.clear();
        txtSpecialization.clear();
        txtEmail.clear();
        txtPhone.clear();
        txtSearch.clear();
        cmbStatus.setValue(CommonStatus.ACTIVE);
        btnSave.setText("Save Intake");
        btnDelete.setDisable(true);
        tblTherapist.getSelectionModel().clearSelection();
    }
}