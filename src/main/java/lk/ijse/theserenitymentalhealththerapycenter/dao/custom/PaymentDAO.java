package lk.ijse.theserenitymentalhealththerapycenter.dao.custom;

import lk.ijse.theserenitymentalhealththerapycenter.dao.CrudDAO;
import lk.ijse.theserenitymentalhealththerapycenter.dao.SuperDAO;
import lk.ijse.theserenitymentalhealththerapycenter.entity.Payment;
import org.hibernate.Session;

import java.time.LocalDateTime;
import java.util.List;

public interface PaymentDAO extends CrudDAO<Payment, Long> {

    Payment findByInvoiceNumber(String invoiceNumber) throws Exception;
    List<Payment> findPaymentsByStatus(String status) throws Exception;
    String getLastInvoiceNumber() throws Exception;
    double getTotalPaidAmountByPatient(Session session, Long patientId) throws Exception;
    double getTotalBookedSessionsCostByPatient(Session session, Long patientId) throws Exception;
    public boolean isSessionPaidForPatient(Long patientId, Long sessionId);
    public List<Object[]> getPatientSessionPaymentStatusLog(Long patientId);

}
