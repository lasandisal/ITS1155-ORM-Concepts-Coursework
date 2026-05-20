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

    @FXML
    public void initialize() {
        configureTableColumns();
        loadSelectorData();
        loadLedgerTable();

        // Auto-update price field when a specific therapy framework item is clicked
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
            patientList = patientBO.getAllActivePatients(); // Adjust method call to match your exact PatientBO configuration naming rules
            for (PatientDTO p : patientList) {
                cmbPatient.getItems().add(p.getId() + " - " + p.getName());
            }

            cmbProgram.getItems().clear();
            programList = programBO.getAllActivePrograms(); // Adjust method call to match your exact TherapyProgramBO naming layouts
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

        try {
            // 1. Persist securely via Hibernate and generate invoice string serial index sequentials
            boolean success = paymentBO.processUpfrontPayment(paymentDTO);

            if (success) {
                AlertUtil.showInformation("Success", "Payment Approved", "Transaction logged successfully. Spawning physical invoice slip...");

                // 2. Pass DTO directly into your updated error-free Jasper print utility thread
                InvoicePrintUtil.printInvoice(paymentDTO);

                // 3. Clear interfaces and reload ledger rows
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