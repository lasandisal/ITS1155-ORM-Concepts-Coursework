package lk.ijse.theserenitymentalhealththerapycenter.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lk.ijse.theserenitymentalhealththerapycenter.bo.BOFactory;
import lk.ijse.theserenitymentalhealththerapycenter.bo.BOType;
import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.PaymentBO;
import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.TherapyProgramBO; // ✅ Inject programmatic catalog BO
import lk.ijse.theserenitymentalhealththerapycenter.dto.PaymentDTO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.TherapyProgramDTO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.tm.PaymentDisplayTM;
import lk.ijse.theserenitymentalhealththerapycenter.entity.Payment;
import lk.ijse.theserenitymentalhealththerapycenter.util.AlertUtil;
import lk.ijse.theserenitymentalhealththerapycenter.util.MailClientUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PaymentFormController {

    @FXML private Button btnDeselectAll;
    @FXML private Button btnRefresh;
    @FXML private Button btnSelectAll;
    @FXML private Button btnSendNotification;

    // ✅ Updated component type mappings to explicitly hold Strings safely
    @FXML private ComboBox<String> cmbPaymentStatusFilter;
    @FXML private ComboBox<String> cmbProgramFilter;

    @FXML private TableView<PaymentDisplayTM> tblPayment;
    @FXML private TableColumn<PaymentDisplayTM, Boolean> colSelect;
    @FXML private TableColumn<PaymentDisplayTM, String> colInvoiceNo;
    @FXML private TableColumn<PaymentDisplayTM, String> colPatientName;
    @FXML private TableColumn<PaymentDisplayTM, String> colProgramName;
    @FXML private TableColumn<PaymentDisplayTM, String> colTherapistName;
    @FXML private TableColumn<PaymentDisplayTM, String> colUser;
    @FXML private TableColumn<PaymentDisplayTM, String> colDate;
    @FXML private TableColumn<PaymentDisplayTM, Double> colAmount;
    @FXML private TableColumn<PaymentDisplayTM, String> colStatus;

    @FXML private Label lblSelectedCount;
    @FXML private Label lblTotalRecords;
    @FXML private TextField txtSearchPayments;

    private final PaymentBO paymentBO = (PaymentBO) BOFactory.getInstance().getBO(BOType.PAYMENT);
    private final TherapyProgramBO programBO = (TherapyProgramBO) BOFactory.getInstance().getBO(BOType.THERAPY_PROGRAM); // ✅ Added BO link

    private final ObservableList<PaymentDisplayTM> masterDataList = FXCollections.observableArrayList();
    private FilteredList<PaymentDisplayTM> filteredDataList;

    @FXML
    public void initialize() {
        configureColumns();
        setupFiltersComboData(); // ✅ Dynamically loads database items now
        loadLedgerRecordsData();

        cmbPaymentStatusFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyDataFilteringPredicate());
        cmbProgramFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyDataFilteringPredicate());
    }

    private void configureColumns() {
        colSelect.setCellValueFactory(cellData -> cellData.getValue().selectedProperty());
        colSelect.setCellFactory(tc -> new CheckBoxTableCell<>());
        colSelect.setEditable(true);

        colInvoiceNo.setCellValueFactory(new PropertyValueFactory<>("invoiceNumber"));
        colPatientName.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        colProgramName.setCellValueFactory(new PropertyValueFactory<>("programName"));
        colTherapistName.setCellValueFactory(new PropertyValueFactory<>("therapistName"));
        colUser.setCellValueFactory(new PropertyValueFactory<>("username"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("paymentDate"));
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        tblPayment.setEditable(true);
    }

    // =========================================================================
    // ✅ PURE DATABASE COMBOBOX INITIALIZATION
    // =========================================================================
    private void setupFiltersComboData() {
        try {
            // 1. Populate Programs dropdown from your persistent Hibernate catalog list
            ObservableList<String> programOptions = FXCollections.observableArrayList();
            programOptions.add("All Programs"); // Baseline reset option

            List<TherapyProgramDTO> activeDatabasePrograms = programBO.getAllActivePrograms();
            if (activeDatabasePrograms != null) {
                for (TherapyProgramDTO program : activeDatabasePrograms) {
                    programOptions.add(program.getName()); // Dynamic database name insertion
                }
            }
            cmbProgramFilter.setItems(programOptions);
            cmbProgramFilter.setValue("All Programs");

            // 2. Populate Status values dynamically straight from the backend Entity Enum definition definitions
            ObservableList<String> statusOptions = FXCollections.observableArrayList();
            statusOptions.add("All Statuses");
            for (Payment.Status systemStatus : Payment.Status.values()) {
                statusOptions.add(systemStatus.name()); // Pulls SUCCESS, FAILED, PENDING right out of your entity enum profile mapping
            }
            cmbPaymentStatusFilter.setItems(statusOptions);
            cmbPaymentStatusFilter.setValue("All Statuses");

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Initialization Failure", "Filter Load Dropped", "Unable to stream dropdown catalogs: " + e.getMessage());
        }
    }

    // =========================================================================
    // ✅ DYNAMIC PURE PREDICATE FILTER COUPLING
    // =========================================================================
    private void applyDataFilteringPredicate() {
        if (filteredDataList == null) return;

        String searchKeyword = txtSearchPayments.getText().toLowerCase().trim();
        String statusFilterValue = cmbPaymentStatusFilter.getValue();
        String programFilterValue = cmbProgramFilter.getValue();

        filteredDataList.setPredicate(row -> {
            if (row == null) return false;

            // 1. Unified text filter matches across unique transaction IDs and Patient profile descriptions
            boolean matchSearch = searchKeyword.isEmpty() ||
                    row.getPatientName().toLowerCase().contains(searchKeyword) ||
                    row.getInvoiceNumber().toLowerCase().contains(searchKeyword);

            // 2. Direct string-match join logic maps against DB values
            boolean matchStatus = statusFilterValue == null || "All Statuses".equalsIgnoreCase(statusFilterValue) ||
                    statusFilterValue.equalsIgnoreCase(row.getStatus());

            // 3. Exact matching logic handles dynamic strings seamlessly
            boolean matchProgram = programFilterValue == null || "All Programs".equalsIgnoreCase(programFilterValue) ||
                    (row.getProgramName() != null && row.getProgramName().equalsIgnoreCase(programFilterValue));

            return matchSearch && matchStatus && matchProgram;
        });
        updateSelectionsCounterMetrics();
    }

    private void loadLedgerRecordsData() {
        try {
            masterDataList.clear();
            List<PaymentDTO> databaseLogs = paymentBO.getAllTransactionsLog();

            for (PaymentDTO dto : databaseLogs) {
                PaymentDisplayTM tmRow = new PaymentDisplayTM(dto);
                tmRow.selectedProperty().addListener((obs, old, newVal) -> updateSelectionsCounterMetrics());
                masterDataList.add(tmRow);
            }

            filteredDataList = new FilteredList<>(masterDataList, p -> true);
            tblPayment.setItems(filteredDataList);
            applyDataFilteringPredicate(); // Make sure initial predicates run cleanly over rows on load execution

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Database Error", "Retrieval Failed", "Unable to sync transaction history rows.");
        }
    }

    private void updateSelectionsCounterMetrics() {
        if (filteredDataList == null) return;
        long count = filteredDataList.stream().filter(PaymentDisplayTM::isSelected).count();
        lblSelectedCount.setText(count + " entries selected for communication");
        lblTotalRecords.setText("Total Records: " + filteredDataList.size());
    }

    @FXML
    void btnSelectAllOnAction(ActionEvent event) {
        if (filteredDataList != null) filteredDataList.forEach(row -> row.setSelected(true));
    }

    @FXML
    void btnDeselectAllOnAction(ActionEvent event) {
        if (filteredDataList != null) filteredDataList.forEach(row -> row.setSelected(false));
    }

    @FXML
    void btnRefreshOnAction(ActionEvent event) {
        loadLedgerRecordsData();
        txtSearchPayments.clear();
        cmbProgramFilter.setValue("All Programs");
        cmbPaymentStatusFilter.setValue("All Statuses");
    }

    @FXML
    void txtSearchPaymentsOnKeyReleased(KeyEvent event) {
        applyDataFilteringPredicate();
    }

    @FXML
    void btnSendNotificationOnAction(ActionEvent event) {
        List<PaymentDisplayTM> selectedRows = masterDataList.stream()
                .filter(PaymentDisplayTM::isSelected)
                .collect(Collectors.toList());

        if (selectedRows.isEmpty()) {
            AlertUtil.showWarning("Selection Error", "No Rows Checked", "Please check at least one target row checkbox before initiating messages.");
            return;
        }

        try {
            FXMLLoader layoutLoader = new FXMLLoader(getClass().getResource("/lk/ijse/theserenitymentalhealththerapycenter/view/CustomMessageModal.fxml"));
            Parent containerRoot = layoutLoader.load();

            CustomMessageModalController modalController = layoutLoader.getController();
            Stage modalStage = new Stage();

            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.initStyle(StageStyle.UTILITY);
            modalStage.setScene(new Scene(containerRoot));
            modalStage.setTitle("Compose Client Broadcast");
            modalStage.showAndWait();

            if (modalController.isSubmitConfirmed()) {
                List<String> collectedEmails = new ArrayList<>();
                selectedRows.forEach(row -> collectedEmails.add(row.getPatientName().toLowerCase().replace(" ", "") + "@serenity.com"));

                MailClientUtil.launchGmailBccComposer(
                        collectedEmails,
                        modalController.getSubjectText(),
                        modalController.getBodyText()
                );
            }

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("UI Error", "Popup Load Aborted", "Could not load message editor pane layout window.");
        }
    }

    @FXML
    void tblPaymentOnMouseClicked(MouseEvent event) {
        // Intercept row targets out of user interaction contexts
        PaymentDisplayTM selectedRowItem = tblPayment.getSelectionModel().getSelectedItem();

        if (selectedRowItem != null) {
            // Check for double click execution passes
            if (event.getClickCount() == 2) {
                openPaymentDetailModalCard(selectedRowItem);
            }
        }
    }

    private void openPaymentDetailModalCard(PaymentDisplayTM selectedRecord) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/lk/ijse/theserenitymentalhealththerapycenter/view/PaymentDetailCard.fxml"));
            AnchorPane modalPane = loader.load();

            // Inject the data context right into our newly designed controller layer
            PaymentDetailCardController controller = loader.getController();
            controller.setPaymentDetails(selectedRecord);

            // Establish stage and configure strict overlay modality options
            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initStyle(StageStyle.UTILITY);
            dialogStage.setTitle("Financial Audit Verification Portal");
            dialogStage.setScene(new Scene(modalPane));
            dialogStage.setResizable(false);
            dialogStage.centerOnScreen();

            // Show window and lock context loop execution thread paths safely
            dialogStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("UI Navigation Error", "View Loading Failed", "Unable to open target card layout: " + e.getMessage());
        }
    }
}