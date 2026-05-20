package lk.ijse.theserenitymentalhealththerapycenter.dao.custom.impl;

import lk.ijse.theserenitymentalhealththerapycenter.dao.BaseDAOImpl;
import lk.ijse.theserenitymentalhealththerapycenter.dao.custom.TherapySessionDAO;
import lk.ijse.theserenitymentalhealththerapycenter.entity.Patient;
import lk.ijse.theserenitymentalhealththerapycenter.entity.TherapySession;
import org.hibernate.query.Query;

import java.time.LocalDateTime;
import java.util.List;

public class TherapySessionDAOImpl extends BaseDAOImpl implements TherapySessionDAO {
    @Override
    public boolean save(TherapySession entity) throws Exception {
        getSession().persist(entity);
        return true;
    }

    @Override
    public boolean update(TherapySession entity) throws Exception {
        getSession().merge(entity);
        return true;
    }

    @Override
    public boolean delete(Long id) throws Exception {
        TherapySession session = findById(id);
        if (session != null) {
            session.setStatus(TherapySession.Status.CANCELLED);
            getSession().merge(session);
            return true;
        }
        return false;
    }

    @Override
    public TherapySession findById(Long id) throws Exception {
        return getSession().get(TherapySession.class, id);
    }

    @Override
    public List<TherapySession> findAll() throws Exception {
        return getSession().createQuery("FROM TherapySession", TherapySession.class).list();
    }

    @Override
    public List<Patient> findPatientsEnrolledInAllPrograms() throws Exception {
        String hql = "SELECT p FROM Patient p WHERE " +
                "(SELECT count(DISTINCT s.therapyProgram.id) FROM TherapySession s WHERE s.patient = p) = " +
                "(SELECT count(tp.id) FROM TherapyProgram tp WHERE tp.status = 'ACTIVE')";
        return getSession().createQuery(hql, Patient.class).list();
    }

    @Override
    public List<TherapySession> findAllSessionsWithDetails() throws Exception {
        String hql = "SELECT DISTINCT s FROM TherapySession s " +
                "JOIN FETCH s.patient " +
                "JOIN FETCH s.therapist " +
                "JOIN FETCH s.therapyProgram";
        return getSession().createQuery(hql, TherapySession.class).list();
    }

    @Override
    public boolean hasOverlappingSession(Long therapistId,
                                         Long patientId,
                                         LocalDateTime startWindow,
                                         LocalDateTime endWindow,
                                         Long excludeSessionId) {

        String hql = "SELECT COUNT(s.id) FROM TherapySession s WHERE " +
                "(s.therapist.id = :therapistId OR s.patient.id = :patientId) " +
                "AND s.sessionDateTime > :startWindow " +
                "AND s.sessionDateTime < :endWindow " +
                "AND s.status != lk.ijse.theserenitymentalhealththerapycenter.entity.TherapySession.Status.CANCELLED";


        if (excludeSessionId != null) {
            hql += " AND s.id != :excludeSessionId";
        }

        Query<Long> query = getSession().createQuery(hql, Long.class);

        query.setParameter("therapistId", therapistId);
        query.setParameter("patientId", patientId);
        query.setParameter("startWindow", startWindow);
        query.setParameter("endWindow", endWindow);

        if (excludeSessionId != null) {
            query.setParameter("excludeSessionId", excludeSessionId);
        }

        Long count = query.uniqueResult();

        return count != null && count > 0;
    }
}
