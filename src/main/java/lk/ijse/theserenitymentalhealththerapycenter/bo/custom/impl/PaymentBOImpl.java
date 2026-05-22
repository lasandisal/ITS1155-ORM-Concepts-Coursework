package lk.ijse.theserenitymentalhealththerapycenter.bo.custom.impl;

import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.PaymentBO;
import lk.ijse.theserenitymentalhealththerapycenter.config.FactoryConfiguration;
import lk.ijse.theserenitymentalhealththerapycenter.dao.DAOFactory;
import lk.ijse.theserenitymentalhealththerapycenter.dao.DAOType;
import lk.ijse.theserenitymentalhealththerapycenter.dao.custom.PatientDAO;
import lk.ijse.theserenitymentalhealththerapycenter.dao.custom.PaymentDAO;
import lk.ijse.theserenitymentalhealththerapycenter.dao.custom.TherapyProgramDAO;
import lk.ijse.theserenitymentalhealththerapycenter.dao.custom.UserDAO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.PaymentDTO;
import lk.ijse.theserenitymentalhealththerapycenter.entity.Patient;
import lk.ijse.theserenitymentalhealththerapycenter.entity.Payment;
import lk.ijse.theserenitymentalhealththerapycenter.entity.TherapyProgram;
import lk.ijse.theserenitymentalhealththerapycenter.entity.User;
import lk.ijse.theserenitymentalhealththerapycenter.exception.PaymentException;
import lk.ijse.theserenitymentalhealththerapycenter.util.MappingUtil;
import lk.ijse.theserenitymentalhealththerapycenter.util.ValidationUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PaymentBOImpl implements PaymentBO {

    private final PaymentDAO paymentDAO = (PaymentDAO) DAOFactory.getInstance().getDAO(DAOType.PAYMENT);
    private final PatientDAO patientDAO = (PatientDAO) DAOFactory.getInstance().getDAO(DAOType.PATIENT);
    private final TherapyProgramDAO programDAO = (TherapyProgramDAO) DAOFactory.getInstance().getDAO(DAOType.THERAPY_PROGRAM);
    private final UserDAO userDAO = (UserDAO) DAOFactory.getInstance().getDAO(DAOType.USER);

    @Override
    public boolean processUpfrontPayment(PaymentDTO dto) throws Exception {
        if (dto.getUserId() == null) {
            throw new PaymentException("Payment Failed: Active handling staff member session must be specified for logging.");
        }

        if (dto.getPatientId() == null || dto.getProgramId() == null) {
            throw new PaymentException("Payment Failed: Target Patient and Therapy Program criteria must be specified.");
        }

        if (dto.getAmount() <= 0) {
            throw new PaymentException("Payment Failed: The collected ledger processing amount must be greater than zero.");
        }

        Session session = FactoryConfiguration.getInstance().getSession();
        Transaction transaction = null;

        try {
            transaction = session.beginTransaction();

            Patient patient = patientDAO.findById(dto.getPatientId());
            TherapyProgram program = programDAO.findById(dto.getProgramId());
            User staffUser = userDAO.findById(dto.getUserId());

            if (patient == null) {
                throw new PaymentException("Processing Failure: Target patient profile missing from records.");
            }
            if (program == null) {
                throw new PaymentException("Processing Failure: Target therapeutic program missing from catalog.");
            }
            if (staffUser == null) {
                throw new PaymentException("Processing Failure: Active staff session account is invalid or deleted.");
            }

            String nextInvoiceNumber = generateNextInvoiceNumber();
            dto.setInvoiceNumber(nextInvoiceNumber);
            dto.setPaymentDate(LocalDate.now());

            Payment payment = MappingUtil.toPaymentEntity(dto);
            payment.setPatient(patient);
            payment.setTherapyProgram(program);
            payment.setManagedBy(staffUser);
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
            throw new PaymentException("Lookup Failed: Target validation lookup invoice parameters cannot be blank.");
        }

        Session session = FactoryConfiguration.getInstance().getSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();

            Payment payment = paymentDAO.findByInvoiceNumber(invoiceNumber);
            if (payment == null) {
                throw new PaymentException("Lookup Failed: Invoice '" + invoiceNumber + "' not found in ledger database.");
            }

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
            throw new PaymentException("Report Failed: Filtering criterion condition token cannot be empty.");
        }

        Session session = FactoryConfiguration.getInstance().getSession();
        Transaction transaction = null;
        try {
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

    @Override
    public double calculateOutstandingBalance(Long patientId) throws Exception {
        if (patientId == null) return 0.0;

        Session session = FactoryConfiguration.getInstance().getSession();
        Transaction transaction = null;

        try {
            transaction = session.beginTransaction();

            // 1. Fetch total cost of sessions booked via DAO methods
            double totalBookedCost = paymentDAO.getTotalBookedSessionsCostByPatient(session, patientId);

            // 2. Fetch total sum of successfully cleared transactions via DAO methods
            double totalPaidAmount = paymentDAO.getTotalPaidAmountByPatient(session, patientId);

            transaction.commit();

            // 3. Mathematical evaluation formula matrix
            double outstandingBalance = totalBookedCost - totalPaidAmount;

            // Prevent returning negative balances if a client pays ahead
            return outstandingBalance < 0 ? 0.0 : outstandingBalance;

        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        }
    }

    // =============================================== Helpers ===============================================

    private String generateNextInvoiceNumber() throws Exception {
        String lastInvoice = paymentDAO.getLastInvoiceNumber();
        int currentYear = LocalDate.now().getYear();

        if (lastInvoice == null || !lastInvoice.startsWith("INV-" + currentYear + "-")) {
            return String.format("INV-%d-0001", currentYear);
        }

        String[] parts = lastInvoice.split("-");
        int lastNumericId = Integer.parseInt(parts[2]);
        int nextNumericId = lastNumericId + 1;

        return String.format("INV-%d-%04d", currentYear, nextNumericId);
    }
}