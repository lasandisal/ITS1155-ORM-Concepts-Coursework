package lk.ijse.theserenitymentalhealththerapycenter.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
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
import lk.ijse.theserenitymentalhealththerapycenter.util.AlertUtil;
import lk.ijse.theserenitymentalhealththerapycenter.util.InvoicePrintUtil;
import lk.ijse.theserenitymentalhealththerapycenter.util.ValidationUtil;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.List;

public class InvoiceFormController {

    @FXML private ComboBox<String> cmbPatient;
    @FXML private ComboBox<String> cmbProgram;
    @FXML private TextField txtAmount;

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

    private final PaymentBO paymentBO = (PaymentBO) BOFactory.getInstance().getBO(BOType.PAYMENT);
    private final PatientBO patientBO = (PatientBO) BOFactory.getInstance().getBO(BOType.PATIENT);
    private final TherapyProgramBO programBO = (TherapyProgramBO) BOFactory.getInstance().getBO(BOType.THERAPY_PROGRAM);

    private List<PatientDTO> patientList;
    private List<TherapyProgramDTO> programList;
    private final ObservableList<PaymentDTO> paymentLogList = FXCollections.observableArrayList();

    private UserDTO authenticatedUser;

    public void setAuthenticatedUser(UserDTO user) {
        this.authenticatedUser = user;
    }

    @FXML
    public void initialize() {
        configureTableColumns();
        loadSelectorData();
        loadLedgerTable();

        cmbProgram.setOnAction(event -> {
            int selectedIndex = cmbProgram.getSelectionModel().getSelectedIndex();
            if (selectedIndex >= 0 && programList != null && selectedIndex < programList.size()) {
                txtAmount.setText(String.valueOf(programList.get(selectedIndex).getFee()));
                System.out.println(">> UI Engine: Standard program cost mapped: LKR " + txtAmount.getText());
            }
        });

        cmbPatient.setOnAction(event -> {
            int selectedIndex = cmbPatient.getSelectionModel().getSelectedIndex();
            if (selectedIndex >= 0 && patientList != null && selectedIndex < patientList.size()) {
                System.out.println(">> UI Engine: Patient selection detected at index [" + selectedIndex + "]. Syncing balance...");
                updateLivePatientBalanceInfo(selectedIndex);
            }
        });

        tblPayments.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                PaymentDTO selectedPayment = tblPayments.getSelectionModel().getSelectedItem();
                if (selectedPayment != null) {
                    System.out.println(">> UI Engine: Historical invoice record chosen. Restoring inputs...");
                    autoFillInputsFromHistoryRow(selectedPayment);
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
            boxBalanceContainer.getStyleClass().removeAll("balance-overdue", "balance-settled");
            txtOutstandingBalance.getStyleClass().removeAll("balance-text-overdue", "balance-text-settled");

            if (balanceDue > 0) {
                boxBalanceContainer.getStyleClass().add("balance-overdue");
                txtOutstandingBalance.getStyleClass().add("balance-text-overdue");
                iconBalanceStatus.setIconColor(javafx.scene.paint.Color.valueOf("#DC2626"));
                iconBalanceStatus.setIconLiteral("fas-exclamation-triangle");
            } else {
                boxBalanceContainer.getStyleClass().add("balance-settled");
                txtOutstandingBalance.getStyleClass().add("balance-text-settled");
                iconBalanceStatus.setIconColor(javafx.scene.paint.Color.valueOf("#16A34A"));
                iconBalanceStatus.setIconLiteral("fas-check-circle");
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(">> Balance Tracker Warning: Could not update outstanding metrics layer: " + e.getMessage());
        }
    }

    private void autoFillInputsFromHistoryRow(PaymentDTO historicalRecord) {
        if (historicalRecord == null) return;

        if (patientList != null) {
            for (int i = 0; i < patientList.size(); i++) {
                if (patientList.get(i).getId().equals(historicalRecord.getPatientId())) {
                    cmbPatient.getSelectionModel().select(i);
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

        boxBalanceContainer.getStyleClass().removeAll("balance-overdue", "balance-settled");
        txtOutstandingBalance.getStyleClass().removeAll("balance-text-overdue", "balance-text-settled");

        iconBalanceStatus.setIconColor(javafx.scene.paint.Color.valueOf("#64748B"));
        iconBalanceStatus.setIconLiteral("fas-exclamation-circle");
    }

    private void configureTableColumns() {
        colInvoiceNo.setCellValueFactory(new PropertyValueFactory<>("invoiceNumber"));
        colPatient.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        colProgram.setCellValueFactory(new PropertyValueFactory<>("programName"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("paymentDate"));
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
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
            e.printStackTrace();
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
            e.printStackTrace();
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

        // Validate manual input payment field parameters securely
        if (!ValidationUtil.isRequiredFieldFilled(txtAmount.getText())) {
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
                btnClearOnAction(null);
                loadLedgerTable();
            }
        } catch (Exception e) {
            AlertUtil.showError("Transaction Failure", "Payment Rejected", e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void btnClearOnAction(ActionEvent event) {
        cmbPatient.getSelectionModel().clearSelection();
        cmbProgram.getSelectionModel().clearSelection();
        txtAmount.clear();
        resetBalanceCardStyles();
    }
}