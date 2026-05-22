package lk.ijse.theserenitymentalhealththerapycenter.dao.custom.impl;

import lk.ijse.theserenitymentalhealththerapycenter.dao.BaseDAOImpl;
import lk.ijse.theserenitymentalhealththerapycenter.dao.custom.PaymentDAO;
import lk.ijse.theserenitymentalhealththerapycenter.entity.Patient;
import lk.ijse.theserenitymentalhealththerapycenter.entity.Payment;
import lk.ijse.theserenitymentalhealththerapycenter.entity.TherapySession;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.nio.file.LinkOption;
import java.time.LocalDateTime;
import java.util.List;

public class PaymentDAOImpl extends BaseDAOImpl implements PaymentDAO {
    @Override
    public boolean save(Payment entity) throws Exception {
        getSession().persist(entity);
        return true;
    }

    @Override
    public boolean update(Payment entity) throws Exception {
        getSession().merge(entity);
        return true;
    }

    @Override
    public boolean delete(Long id) throws Exception {
        Payment payment = findById(id);
        if (payment != null) {
            payment.setStatus(Payment.Status.FAILED);
            getSession().merge(payment);
            return true;
        }
        return false;
    }

    @Override
    public Payment findById(Long id) throws Exception {
        return getSession().get(Payment.class, id);
    }

    @Override
    public List<Payment> findAll() throws Exception {
        return getSession().createQuery("FROM Payment", Payment.class).list();
    }

    @Override
    public Payment findByInvoiceNumber(String invoiceNumber) throws Exception {
        Query<Payment> query = getSession().createQuery("FROM Payment WHERE invoiceNumber = :invNum", Payment.class);
        query.setParameter("invNum", invoiceNumber);
        return query.uniqueResult();
    }

    @Override
    public List<Payment> findPaymentsByStatus(String status) throws Exception {
        Query<Payment> query = getSession().createQuery("FROM Payment WHERE status = :status", Payment.class);
        query.setParameter("status", Payment.Status.valueOf(status.toUpperCase()));
        return query.list();
    }

    @Override
    public String getLastInvoiceNumber() throws Exception {
        return getSession().createQuery("SELECT p.invoiceNumber FROM Payment p ORDER BY p.id DESC", String.class)
                .setMaxResults(1)
                .uniqueResult();
    }

    @Override
    public double getTotalPaidAmountByPatient(Session session, Long patientId) throws Exception {
        String hql = "SELECT COALESCE(SUM(p.amount), 0.0) FROM Payment p " +
                "WHERE p.patient.id = :pid AND p.status = lk.ijse.theserenitymentalhealththerapycenter.entity.Payment.Status.SUCCESS";
        return session.createQuery(hql, Double.class)
                .setParameter("pid", patientId)
                .getSingleResult();
    }

    @Override
    public double getTotalBookedSessionsCostByPatient(Session session, Long patientId) throws Exception {
        String hql = "SELECT COALESCE(SUM(a.session.therapyProgram.fee), 0.0) " +
                "FROM SessionAttendance a " +
                "WHERE a.patient.id = :pid AND a.session.status != 'CANCELLED'";

        return session.createQuery(hql, Double.class)
                .setParameter("pid", patientId)
                .getSingleResult();
    }


}
