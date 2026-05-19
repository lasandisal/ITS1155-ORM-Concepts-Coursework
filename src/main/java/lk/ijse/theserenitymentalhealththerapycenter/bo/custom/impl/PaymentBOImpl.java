package lk.ijse.theserenitymentalhealththerapycenter.bo.custom.impl;

import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.PaymentBO;
import lk.ijse.theserenitymentalhealththerapycenter.config.FactoryConfiguration;
import lk.ijse.theserenitymentalhealththerapycenter.dao.DAOFactory;
import lk.ijse.theserenitymentalhealththerapycenter.dao.DAOType;
import lk.ijse.theserenitymentalhealththerapycenter.dao.custom.PaymentDAO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.PaymentDTO;
import lk.ijse.theserenitymentalhealththerapycenter.entity.Payment;
import lk.ijse.theserenitymentalhealththerapycenter.exception.RegistrationException;
import lk.ijse.theserenitymentalhealththerapycenter.util.MappingUtil;
import lk.ijse.theserenitymentalhealththerapycenter.util.ValidationUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.ArrayList;
import java.util.List;

public class PaymentBOImpl implements PaymentBO {

    // ✅ FIXED: Corrected the DAO type mapping from PATIENT to PAYMENT and stripped the double semicolon
    private final PaymentDAO paymentDAO = (PaymentDAO) DAOFactory.getInstance().getDAO(DAOType.PAYMENT);

    @Override
    public boolean processUpfrontPayment(PaymentDTO dto) throws Exception {
        if (dto.getPatientId() == null || dto.getProgramId() == null ||
                !ValidationUtil.isRequiredFieldFilled(dto.getInvoiceNumber()) || dto.getPaymentDate() == null) {
            throw new RegistrationException("Payment Failed: Incomplete processing parameters supplied.");
        }

        if (dto.getAmount() <= 0) {
            throw new RegistrationException("Payment Failed: The collected ledger processing amount must be greater than zero.");
        }

        Session session = FactoryConfiguration.getInstance().getSession();
        Transaction transaction = null;

        try {
            transaction = session.beginTransaction();

            Payment existingInvoice = paymentDAO.findByInvoiceNumber(dto.getInvoiceNumber());
            if (existingInvoice != null) {
                throw new RegistrationException("Payment Failed: Invoice ID tracking serial '" + dto.getInvoiceNumber() + "' is already assigned.");
            }

            Payment payment = MappingUtil.toPaymentEntity(dto);
            payment.setStatus(Payment.Status.SUCCESS);

            boolean isSaved = paymentDAO.save(payment);

            transaction.commit();
            return isSaved;

        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        }
    }

    @Override
    public PaymentDTO getInvoiceDetails(String invoiceNumber) throws Exception {
        if (!ValidationUtil.isRequiredFieldFilled(invoiceNumber)) {
            throw new RegistrationException("Lookup Failed: Target validation lookup invoice parameters cannot be blank.");
        }

        Session session = FactoryConfiguration.getInstance().getSession();
        Transaction transaction = null;
        try {
            // ✅ FIXED: Read transaction context added, legacy finally-close removed
            transaction = session.beginTransaction();

            Payment payment = paymentDAO.findByInvoiceNumber(invoiceNumber);
            PaymentDTO dto = MappingUtil.toPaymentDTO(payment);

            transaction.commit();
            return dto;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        }
    }

    @Override
    public List<PaymentDTO> getFinancialReportByStatus(String status) throws Exception {
        if (!ValidationUtil.isRequiredFieldFilled(status)) {
            throw new RegistrationException("Report Failed: Filtering criterion condition token cannot be empty.");
        }

        Session session = FactoryConfiguration.getInstance().getSession();
        Transaction transaction = null;
        try {
            // ✅ FIXED: Read transaction context added, legacy finally-close removed
            transaction = session.beginTransaction();

            List<Payment> payments = paymentDAO.findPaymentsByStatus(status);
            List<PaymentDTO> dtoList = new ArrayList<>();

            for (Payment p : payments) {
                dtoList.add(MappingUtil.toPaymentDTO(p));
            }

            transaction.commit();
            return dtoList;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        }
    }

    @Override
    public List<PaymentDTO> getAllTransactionsLog() throws Exception {
        Session session = FactoryConfiguration.getInstance().getSession();
        Transaction transaction = null;
        try {
            // ✅ FIXED: Read transaction context added, legacy finally-close removed
            transaction = session.beginTransaction();

            List<Payment> payments = paymentDAO.findAll();
            List<PaymentDTO> dtoList = new ArrayList<>();

            for (Payment p : payments) {
                dtoList.add(MappingUtil.toPaymentDTO(p));
            }

            transaction.commit();
            return dtoList;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        }
    }
}