package lk.ijse.theserenitymentalhealththerapycenter.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import lk.ijse.theserenitymentalhealththerapycenter.bo.BOFactory;
import lk.ijse.theserenitymentalhealththerapycenter.bo.BOType;
import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.PatientBO;
import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.PaymentBO;
import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.TherapyProgramBO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.PatientDTO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.PaymentDTO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.TherapyProgramDTO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.UserDTO; // ✅ Imported for session tracking
import lk.ijse.theserenitymentalhealththerapycenter.util.AlertUtil;
import lk.ijse.theserenitymentalhealththerapycenter.util.InvoicePrintUtil;

import java.util.List;

public class InvoiceFormController {

    @FXML private ComboBox<String> cmbPatient;
    @FXML private ComboBox<String> cmbProgram;
    @FXML private TextField txtAmount;
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

    // =========================================================================
    // ✅ ACTIVE SESSION USER STORAGE FIELD
    // =========================================================================
    private UserDTO authenticatedUser;

    /**
     * Setter method invoked by your navigation center/DashboardController
     * to bind the logged-in employee context dynamically.
     */
    public void setAuthenticatedUser(UserDTO user) {
        this.authenticatedUser = user;
    }
    // =========================================================================

    @FXML
    public void initialize() {
        configureTableColumns();
        loadSelectorData();
        loadLedgerTable();

        cmbProgram.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.intValue() >= 0) {
                txtAmount.setText(String.valueOf(programList.get(newValue.intValue()).getFee()));
            }
        });
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
        }
    }

    @FXML
    void btnPayPrintOnAction(ActionEvent event) {
        // ✅ SAFETY GUARD: Prevent processing if navigation injection was completely skipped
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

        PatientDTO selectedPatient = patientList.get(patientIndex);
        TherapyProgramDTO selectedProgram = programList.get(programIndex);

        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setPatientId(selectedPatient.getId());
        paymentDTO.setPatientName(selectedPatient.getName());
        paymentDTO.setProgramId(selectedProgram.getId());
        paymentDTO.setProgramName(selectedProgram.getName());
        paymentDTO.setAmount(selectedProgram.getFee());

        // =========================================================================
        // ✅ CRITICAL AUDIT LINK INJECTION
        // =========================================================================
        // Securely binds the active user's ID to the business payload container
        paymentDTO.setUserId(authenticatedUser.getId());
        paymentDTO.setUsername(authenticatedUser.getUsername());
        // =========================================================================

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
    }
}