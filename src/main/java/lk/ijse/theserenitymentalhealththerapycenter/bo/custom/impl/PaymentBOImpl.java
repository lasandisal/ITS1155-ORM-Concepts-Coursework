package lk.ijse.theserenitymentalhealththerapycenter.bo.custom.impl;

import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.PaymentBO;
import lk.ijse.theserenitymentalhealththerapycenter.config.FactoryConfiguration;
import lk.ijse.theserenitymentalhealththerapycenter.dao.DAOFactory;
import lk.ijse.theserenitymentalhealththerapycenter.dao.DAOType;
import lk.ijse.theserenitymentalhealththerapycenter.dao.custom.PatientDAO;
import lk.ijse.theserenitymentalhealththerapycenter.dao.custom.PaymentDAO;
import lk.ijse.theserenitymentalhealththerapycenter.dao.custom.TherapyProgramDAO;
import lk.ijse.theserenitymentalhealththerapycenter.dao.custom.UserDAO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.PatientSessionStatusDTO;
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
import java.time.LocalDateTime;
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
            throw new PaymentException("Payment Failed: Active handling staff member session must be specified.");
        }
        if (dto.getPatientId() == null || dto.getProgramId() == null) {
            throw new PaymentException("Payment Failed: Target Patient and Therapy Program criteria must be specified.");
        }
        if (dto.getAmount() <= 0) {
            throw new PaymentException("Payment Failed: The processing amount must be greater than zero.");
        }

        Session session = FactoryConfiguration.getInstance().getSession();
        Transaction transaction = null;

        try {
            // SAFE BOUNDARY: Open transaction context safely if not initialized yet
            if (!session.getTransaction().isActive()) {
                transaction = session.beginTransaction();
            }

            Patient patient = patientDAO.findById(dto.getPatientId());
            TherapyProgram program = programDAO.findById(dto.getProgramId());
            User staffUser = userDAO.findById(dto.getUserId());

            if (patient == null || program == null || staffUser == null) {
                throw new PaymentException("Processing Failure: Associated base profiles are missing from database maps.");
            }

            dto.setInvoiceNumber(generateNextInvoiceNumber());
            dto.setPaymentDate(LocalDate.now());

            Payment payment = MappingUtil.toPaymentEntity(dto);
            payment.setPatient(patient);
            payment.setTherapyProgram(program);
            payment.setManagedBy(staffUser);
            payment.setStatus(Payment.Status.SUCCESS);

            boolean isSaved = paymentDAO.save(payment);

            if (transaction != null) transaction.commit();
            return isSaved;

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
            if (!session.getTransaction().isActive()) {
                transaction = session.beginTransaction();
            }

            List<Payment> payments = paymentDAO.findAll();
            List<PaymentDTO> dtoList = new ArrayList<>();

            for (Payment p : payments) {
                dtoList.add(MappingUtil.toPaymentDTO(p));
            }

            if (transaction != null) transaction.commit();
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
            if (!session.getTransaction().isActive()) {
                transaction = session.beginTransaction();
            }

            double totalBookedCost = paymentDAO.getTotalBookedSessionsCostByPatient(session, patientId);
            double totalPaidAmount = paymentDAO.getTotalPaidAmountByPatient(session, patientId);

            if (transaction != null) transaction.commit();

            double outstandingBalance = totalBookedCost - totalPaidAmount;
            return outstandingBalance < 0 ? 0.0 : outstandingBalance;

        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        }
    }

    @Override
    public PaymentDTO getInvoiceDetails(String invoiceNumber) throws Exception {
        Session session = FactoryConfiguration.getInstance().getSession();
        Transaction transaction = null;
        try {
            if (!session.getTransaction().isActive()) {
                transaction = session.beginTransaction();
            }
            Payment payment = paymentDAO.findByInvoiceNumber(invoiceNumber);
            PaymentDTO dto = payment != null ? MappingUtil.toPaymentDTO(payment) : null;
            if (transaction != null) transaction.commit();
            return dto;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        }
    }

    @Override
    public List<PaymentDTO> getFinancialReportByStatus(String status) throws Exception {
        Session session = FactoryConfiguration.getInstance().getSession();
        Transaction transaction = null;
        try {
            if (!session.getTransaction().isActive()) {
                transaction = session.beginTransaction();
            }
            List<Payment> payments = paymentDAO.findPaymentsByStatus(status);
            List<PaymentDTO> dtoList = new ArrayList<>();
            for (Payment p : payments) {
                dtoList.add(MappingUtil.toPaymentDTO(p));
            }
            if (transaction != null) transaction.commit();
            return dtoList;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        }
    }

    private String generateNextInvoiceNumber() throws Exception {
        String lastInvoice = paymentDAO.getLastInvoiceNumber();
        int currentYear = LocalDate.now().getYear();

        if (lastInvoice == null || !lastInvoice.startsWith("INV-" + currentYear + "-")) {
            return String.format("INV-%d-0001", currentYear);
        }

        String[] parts = lastInvoice.split("-");
        int lastNumericId = Integer.parseInt(parts[2]);
        return String.format("INV-%d-%04d", currentYear, lastNumericId + 1);
    }

    @Override
    public List<PatientSessionStatusDTO> getPatientSessionPaymentStatusLog(Long patientId) throws Exception {
        Session session = FactoryConfiguration.getInstance().getSession();
        Transaction transaction = null;
        List<PatientSessionStatusDTO> mappedList = new java.util.ArrayList<>();

        try {
            // Manage transaction boundaries safely using your established context check pattern
            if (!session.getTransaction().isActive()) {
                transaction = session.beginTransaction();
            }

            // 1. Fetch the raw object arrays from your DAO method
            // (Assuming you placed the method in paymentDAO)
            List<Object[]> rawRows = paymentDAO.getPatientSessionPaymentStatusLog(patientId);

            // 2. Map the Object[] columns cleanly into your JavaFX-friendly DTOs
            for (Object[] row : rawRows) {
                PatientSessionStatusDTO dto = new PatientSessionStatusDTO(
                        (Long) row[0],                       // ts.id
                        (LocalDateTime) row[1],              // ts.sessionDateTime
                        (String) row[2],                     // tp.name
                        (String) row[3]                      // Calculated 'PAID'/'UNPAID' label
                );
                mappedList.add(dto);
            }

            if (transaction != null) transaction.commit();
            return mappedList;

        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        }
    }
}