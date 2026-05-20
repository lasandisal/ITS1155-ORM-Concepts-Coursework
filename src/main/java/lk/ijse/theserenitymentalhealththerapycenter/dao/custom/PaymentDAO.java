package lk.ijse.theserenitymentalhealththerapycenter.dao.custom;

import lk.ijse.theserenitymentalhealththerapycenter.dao.CrudDAO;
import lk.ijse.theserenitymentalhealththerapycenter.dao.SuperDAO;
import lk.ijse.theserenitymentalhealththerapycenter.entity.Payment;

import java.time.LocalDateTime;
import java.util.List;

public interface PaymentDAO extends CrudDAO<Payment, Long> {

    Payment findByInvoiceNumber(String invoiceNumber) throws Exception;
    List<Payment> findPaymentsByStatus(String status) throws Exception;
    String getLastInvoiceNumber() throws Exception;

}
