package lk.ijse.theserenitymentalhealththerapycenter.bo.custom;

import lk.ijse.theserenitymentalhealththerapycenter.bo.SuperBO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.PatientSessionStatusDTO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.PaymentDTO;

import java.util.List;

public interface PaymentBO extends SuperBO {
    boolean processUpfrontPayment(PaymentDTO dto) throws Exception;
    PaymentDTO getInvoiceDetails(String invoiceNumber) throws Exception;
    List<PaymentDTO> getFinancialReportByStatus(String status) throws Exception;
    List<PaymentDTO> getAllTransactionsLog() throws Exception;
    double calculateOutstandingBalance(Long patientId) throws Exception;
    List<PatientSessionStatusDTO> getPatientSessionPaymentStatusLog(Long patientId) throws Exception;
}
