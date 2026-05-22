package lk.ijse.theserenitymentalhealththerapycenter.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import lk.ijse.theserenitymentalhealththerapycenter.bo.BOFactory;
import lk.ijse.theserenitymentalhealththerapycenter.bo.BOType;
import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.PatientBO;
import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.PaymentBO;
import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.TherapyProgramBO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.PatientDTO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.PaymentDTO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.TherapyProgramDTO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.UserDTO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.PatientSessionStatusDTO;
import lk.ijse.theserenitymentalhealththerapycenter.util.AlertUtil;
import lk.ijse.theserenitymentalhealththerapycenter.util.InvoicePrintUtil;
import org.kordamp.ikonli.javafx.FontIcon;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class InvoiceFormController {

    @FXML private ComboBox<String> cmbPatient;
    @FXML private ComboBox<String> cmbProgram;
    @FXML private TextField txtAmount;
    @FXML private TextField txtSearchInvoice;

    // Interactive Balance UI Indicators
    @FXML private TextField txtOutstandingBalance;
    @FXML private HBox boxBalanceContainer;
    @FXML private FontIcon iconBalanceStatus;

    // Ledger History Table View Components
    @FXML private TableView<PaymentDTO> tblPayments;
    @FXML private TableColumn<PaymentDTO, String> colInvoiceNo;
    @FXML private TableColumn<PaymentDTO, String> colPatient;
    @FXML private TableColumn<PaymentDTO, String> colProgram;
    @FXML private TableColumn<PaymentDTO, String> colDate;
    @FXML private TableColumn<PaymentDTO, Double> colAmount;

    // NEW: Real-time Patient Appointment Status Tracking Components
    @FXML private TableView<PatientSessionStatusDTO> tblPatientSessions;
    @FXML private TableColumn<PatientSessionStatusDTO, Long> colSessId;
    @FXML private TableColumn<PatientSessionStatusDTO, LocalDateTime> colSessDate;
    @FXML private TableColumn<PatientSessionStatusDTO, String> colSessProgram;
    @FXML private TableColumn<PatientSessionStatusDTO, String> colSessStatus;

    private final PaymentBO paymentBO = (PaymentBO) BOFactory.getInstance().getBO(BOType.PAYMENT);
    private final PatientBO patientBO = (PatientBO) BOFactory.getInstance().getBO(BOType.PATIENT);
    private final TherapyProgramBO programBO = (TherapyProgramBO) BOFactory.getInstance().getBO(BOType.THERAPY_PROGRAM);

    private List<PatientDTO> patientList;
    private List<TherapyProgramDTO> programList;
    private final ObservableList<PaymentDTO> paymentLogList = FXCollections.observableArrayList();
    private final ObservableList<PatientSessionStatusDTO> patientSessionList = FXCollections.observableArrayList();

    private UserDTO authenticatedUser;

    public void setAuthenticatedUser(UserDTO user) {
        this.authenticatedUser = user;
    }

    @FXML
    public void initialize() {
        configureTableColumns();
        configureSessionTableColumns(); // Initialize new table structure mappings
        loadSelectorData();
        loadLedgerTable();

        cmbProgram.setOnAction(event -> {
            int selectedIndex = cmbProgram.getSelectionModel().getSelectedIndex();
            if (selectedIndex >= 0 && programList != null && selectedIndex < programList.size()) {
                txtAmount.setText(String.valueOf(programList.get(selectedIndex).getFee()));
            }
        });

        cmbPatient.setOnAction(event -> {
            int selectedIndex = cmbPatient.getSelectionModel().getSelectedIndex();
            if (selectedIndex >= 0 && patientList != null && selectedIndex < patientList.size()) {
                updateLivePatientBalanceInfo(selectedIndex);
                loadLivePatientSessionLogs(selectedIndex); // Automatically sync session verification sub-table rows
            }
        });

        tblPayments.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                PaymentDTO selectedPayment = tblPayments.getSelectionModel().getSelectedItem();
                if (selectedPayment != null) {
                    autoFillInputsFromHistoryRow(selectedPayment);
                }
            }
        });

        txtSearchInvoice.setOnKeyReleased(this::txtSearchInvoiceOnKeyReleased);
    }

    private void configureTableColumns() {
        colInvoiceNo.setCellValueFactory(new PropertyValueFactory<>("invoiceNumber"));
        colPatient.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        colProgram.setCellValueFactory(new PropertyValueFactory<>("programName"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("paymentDate"));
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
    }

    // NEW: Session Verification Table Map Configuration Mappings
    private void configureSessionTableColumns() {
        colSessId.setCellValueFactory(new PropertyValueFactory<>("sessionId"));
        colSessDate.setCellValueFactory(new PropertyValueFactory<>("sessionDateTime"));
        colSessProgram.setCellValueFactory(new PropertyValueFactory<>("programName"));
        colSessStatus.setCellValueFactory(new PropertyValueFactory<>("paymentStatus"));

        // Optional UI Polish: Color-code PAID vs UNPAID cells in your JavaFX grid
        colSessStatus.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.equalsIgnoreCase("PAID")) {
                        setStyle("-fx-text-fill: #16A34A; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #DC2626; -fx-font-weight: bold;");
                    }
                }
            }
        });
    }

    private void updateLivePatientBalanceInfo(int selectedIndex) {
        if (selectedIndex < 0) {
            resetBalanceCardStyles();
            return;
        }

        try {
            PatientDTO targetPatient = patientList.get(selectedIndex);
            double balanceDue = paymentBO.calculateOutstandingBalance(targetPatient.getId());

            txtOutstandingBalance.setText(String.format("LKR %,.2f", balanceDue));

            if (balanceDue > 0) {
                boxBalanceContainer.setStyle("-fx-background-color: #FEF2F2; -fx-border-color: #FCA5A5;");
                txtOutstandingBalance.setStyle("-fx-text-fill: #DC2626; -fx-font-weight: bold;");
                iconBalanceStatus.setIconColor(javafx.scene.paint.Color.valueOf("#DC2626"));
                iconBalanceStatus.setIconLiteral("fas-exclamation-triangle");
            } else {
                boxBalanceContainer.setStyle("-fx-background-color: #F0FDF4; -fx-border-color: #86EFAC;");
                txtOutstandingBalance.setStyle("-fx-text-fill: #16A34A; -fx-font-weight: bold;");
                iconBalanceStatus.setIconColor(javafx.scene.paint.Color.valueOf("#16A34A"));
                iconBalanceStatus.setIconLiteral("fas-check-circle");
            }

        } catch (Exception e) {
            System.err.println(">> Balance Sync Error: " + e.getMessage());
        }
    }

    // NEW: Database pipeline hook feeding data straight into the live session table view
    private void loadLivePatientSessionLogs(int selectedIndex) {
        try {
            patientSessionList.clear();
            if (selectedIndex >= 0 && patientList != null) {
                PatientDTO selectedPatient = patientList.get(selectedIndex);
                List<PatientSessionStatusDTO> logs = paymentBO.getPatientSessionPaymentStatusLog(selectedPatient.getId());
                patientSessionList.addAll(logs);
                tblPatientSessions.setItems(patientSessionList);
            }
        } catch (Exception e) {
            System.err.println(">> App State Warning: Session status lookups dropped: " + e.getMessage());
        }
    }

    private void autoFillInputsFromHistoryRow(PaymentDTO historicalRecord) {
        if (historicalRecord == null) return;

        if (patientList != null) {
            for (int i = 0; i < patientList.size(); i++) {
                if (patientList.get(i).getId().equals(historicalRecord.getPatientId())) {
                    cmbPatient.getSelectionModel().select(i);
                    // Force refresh metrics for loaded selection row profile instantly
                    updateLivePatientBalanceInfo(i);
                    loadLivePatientSessionLogs(i);
                    break;
                }
            }
        }

        if (programList != null) {
            for (int i = 0; i < programList.size(); i++) {
                if (programList.get(i).getId().equals(historicalRecord.getProgramId())) {
                    cmbProgram.getSelectionModel().select(i);
                    break;
                }
            }
        }

        txtAmount.setText(String.valueOf(historicalRecord.getAmount()));
    }

    private void resetBalanceCardStyles() {
        txtOutstandingBalance.setText("LKR 0.00");
        boxBalanceContainer.setStyle("-fx-background-color: #F8FAFC; -fx-border-color: #CBD5E1;");
        txtOutstandingBalance.setStyle("-fx-text-fill: #64748B; -fx-font-weight: bold;");
        iconBalanceStatus.setIconColor(javafx.scene.paint.Color.valueOf("#64748B"));
        iconBalanceStatus.setIconLiteral("fas-exclamation-circle");
        patientSessionList.clear(); // Flush the tracking subgrid clean upon general clear event actions
    }

    private void loadSelectorData() {
        try {
            cmbPatient.getItems().clear();
            patientList = patientBO.getAllActivePatients();
            for (PatientDTO p : patientList) {
                cmbPatient.getItems().add(p.getId() + " - " + p.getName());
            }

            cmbProgram.getItems().clear();
            programList = programBO.getAllActivePrograms();
            for (TherapyProgramDTO tp : programList) {
                cmbProgram.getItems().add(tp.getId() + " - " + tp.getName());
            }
        } catch (Exception e) {
            AlertUtil.showError("Data Error", "Load Aborted", "Unable to stream active dropdown lists.");
        }
    }

    private void loadLedgerTable() {
        try {
            paymentLogList.clear();
            List<PaymentDTO> allTransactions = paymentBO.getAllTransactionsLog();
            paymentLogList.addAll(allTransactions);
            tblPayments.setItems(paymentLogList);
        } catch (Exception e) {
            AlertUtil.showError("Database Error", "Sync Failed", "Could not populate historical ledger logs table.");
        }
    }

    @FXML
    void btnPayPrintOnAction(ActionEvent event) {
        if (authenticatedUser == null) {
            AlertUtil.showError("Security Error", "Session Missing", "Cannot process payment. No active staff session detected.");
            return;
        }

        int patientIndex = cmbPatient.getSelectionModel().getSelectedIndex();
        int programIndex = cmbProgram.getSelectionModel().getSelectedIndex();

        if (patientIndex < 0 || programIndex < 0) {
            AlertUtil.showWarning("Validation Error", "Fields Incomplete", "Please pick a valid Patient profile and Therapy Program framework first.");
            return;
        }

        if (txtAmount.getText() == null || txtAmount.getText().trim().isEmpty()) {
            AlertUtil.showWarning("Validation Error", "Amount Field Empty", "Please enter a transaction value amount before printing.");
            return;
        }

        double processedInputAmount = 0.0;
        try {
            processedInputAmount = Double.parseDouble(txtAmount.getText().trim());
            if (processedInputAmount <= 0) {
                AlertUtil.showWarning("Input Error", "Invalid Value Range", "The payment input amount must be greater than zero.");
                return;
            }
        } catch (NumberFormatException e) {
            AlertUtil.showWarning("Input Error", "Formatting Exception", "Please pass a valid clean numeric decimal number inside your payment input box.");
            return;
        }

        PatientDTO selectedPatient = patientList.get(patientIndex);
        TherapyProgramDTO selectedProgram = programList.get(programIndex);

        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setPatientId(selectedPatient.getId());
        paymentDTO.setPatientName(selectedPatient.getName());
        paymentDTO.setProgramId(selectedProgram.getId());
        paymentDTO.setProgramName(selectedProgram.getName());
        paymentDTO.setAmount(processedInputAmount);
        paymentDTO.setUserId(authenticatedUser.getId());
        paymentDTO.setUsername(authenticatedUser.getUsername());

        try {
            boolean success = paymentBO.processUpfrontPayment(paymentDTO);
            if (success) {
                AlertUtil.showInformation("Success", "Payment Approved", "Transaction logged successfully. Spawning physical invoice slip...");
                InvoicePrintUtil.printInvoice(paymentDTO);

                // Keep the patient selected so the updated paid status list renders right away
                int currentPatientIndexBackup = patientIndex;

                btnClearOnAction(null);
                loadLedgerTable();

                // Re-select and trigger data refresh for immediate confirmation response
                cmbPatient.getSelectionModel().select(currentPatientIndexBackup);
            }
        } catch (Exception e) {
            AlertUtil.showError("Transaction Failure", "Payment Rejected", e.getMessage());
        }
    }

    @FXML
    void btnClearOnAction(ActionEvent event) {
        cmbPatient.getSelectionModel().clearSelection();
        cmbProgram.getSelectionModel().clearSelection();
        txtAmount.clear();
        resetBalanceCardStyles();
    }

    private void txtSearchInvoiceOnKeyReleased(KeyEvent event) {
        String query = txtSearchInvoice.getText().trim().toLowerCase();
        if (query.isEmpty()) {
            tblPayments.setItems(paymentLogList);
            return;
        }

        ObservableList<PaymentDTO> filtered = paymentLogList.stream()
                .filter(p -> p.getInvoiceNumber().toLowerCase().contains(query) ||
                        p.getPatientName().toLowerCase().contains(query))
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
        tblPayments.setItems(filtered);
    }
}