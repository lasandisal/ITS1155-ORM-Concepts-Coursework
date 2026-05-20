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

    private final TherapistBO therapistBO =
            (TherapistBO) BOFactory.getInstance().getBO(BOType.THERAPIST);

    private final ObservableList<TherapistDTO> therapistList =
            FXCollections.observableArrayList();

    private TherapistDTO selectedTherapist;

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

    @FXML
    public void initialize() {
        initializeTable();
        initializeComboBox();
        loadAllTherapists();
        btnDelete.setDisable(true);
    }

    private void initializeTable() {
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

    private void loadAllTherapists() {
        try {
            therapistList.clear();
            List<TherapistDTO> allTherapists = therapistBO.getAllActiveTherapists();
            therapistList.addAll(allTherapists);
            tblTherapist.setItems(therapistList);
        } catch (Exception e) {
            AlertUtil.showError("Loading Error", "Therapist Loading Failed", e.getMessage());
        }
    }

    @FXML
    void btnSaveOnAction(ActionEvent event) {
        try {
            TherapistDTO dto = new TherapistDTO();
            dto.setName(txtName.getText().trim());
            dto.setSpecialization(txtSpecialization.getText().trim());
            dto.setEmail(txtEmail.getText().trim());
            dto.setPhone(txtPhone.getText().trim());
            dto.setStatus(cmbStatus.getValue());

            boolean result;

            if (selectedTherapist == null) {
                result = therapistBO.saveTherapist(dto);
                if (result) {
                    AlertUtil.showSuccess("Success", null, "Therapist saved successfully.");
                }
            } else {
                dto.setId(selectedTherapist.getId());
                result = therapistBO.updateTherapist(dto);
                if (result) {
                    AlertUtil.showSuccess("Success", null, "Therapist updated successfully.");
                }
            }

            clearForm();
            loadAllTherapists();

        } catch (Exception e) {
            AlertUtil.showError("Operation Failed", "Therapist Save Failed", e.getMessage());
        }
    }

    @FXML
    void btnDeleteOnAction(ActionEvent event) {
        if (selectedTherapist == null) {
            AlertUtil.showWarning("Selection Required", null, "Please select a therapist first.");
            return;
        }

        boolean confirmed = AlertUtil.showConfirmation(
                "Delete Confirmation",
                "Delete Therapist",
                "Are you sure you want to remove this therapist?"
        );

        if (!confirmed) return;

        try {
            boolean result = therapistBO.softDeleteTherapist(selectedTherapist.getId());
            if (result) {
                AlertUtil.showSuccess("Deleted", null, "Therapist removed successfully.");
                clearForm();
                loadAllTherapists();
            }
        } catch (Exception e) {
            AlertUtil.showError("Delete Failed", "Therapist Delete Error", e.getMessage());
        }
    }

    @FXML
    void btnClearOnAction(ActionEvent event) {
        clearForm();
    }

    private void clearForm() {
        txtName.clear();
        txtSpecialization.clear();
        txtEmail.clear();
        txtPhone.clear();
        txtSearch.clear();

        cmbStatus.setValue(CommonStatus.ACTIVE);
        tblTherapist.getSelectionModel().clearSelection();

        selectedTherapist = null;
        btnSave.setText("Save Therapist");
        btnDelete.setDisable(true);
    }

    @FXML
    void tblTherapistOnMouseClicked(MouseEvent event) {
        // ✅ FIXED: Assigned directly to the class level 'selectedTherapist' tracking field variable
        selectedTherapist = tblTherapist.getSelectionModel().getSelectedItem();

        if (selectedTherapist != null) {
            txtName.setText(selectedTherapist.getName());
            txtSpecialization.setText(selectedTherapist.getSpecialization());
            txtEmail.setText(selectedTherapist.getEmail());
            txtPhone.setText(selectedTherapist.getPhone());
            cmbStatus.setValue(selectedTherapist.getStatus());

            btnSave.setText("Update Therapist");
            btnDelete.setDisable(false); // Activates delete availability logic block cleanly

            // Double-click triggers the modal detail profile card
            if (event.getClickCount() == 2) {
                openTherapistDetailCard(selectedTherapist);
            }
        }
    }

    @FXML
    void txtSearchOnKeyReleased(KeyEvent event) {
        String filterQuery = txtSearch.getText().trim();

        // When user types and hits ENTER, grab top record result and render separate modal card view
        if (event.getCode() == KeyCode.ENTER && !tblTherapist.getItems().isEmpty()) {
            TherapistDTO topMatchedTherapist = tblTherapist.getItems().get(0);
            openTherapistDetailCard(topMatchedTherapist);
            return;
        }

        if (filterQuery.isEmpty()) {
            // ✅ FIXED: Corrected reference method target pointer naming profile constraint
            loadAllTherapists();
            return;
        }

        // Handles dynamic type-ahead in-memory filter matching your UI requirements
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
}