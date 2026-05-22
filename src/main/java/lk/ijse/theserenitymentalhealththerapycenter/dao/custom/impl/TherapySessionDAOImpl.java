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

    /**
     * Advanced Analytics Query: Matches patients who are registered across ALL active therapeutic frameworks.
     * Rewritten to join through the attendances bridge structure.
     */
    @Override
    public List<Patient> findPatientsEnrolledInAllPrograms() throws Exception {
        String hql = "SELECT p FROM Patient p WHERE " +
                "(SELECT COUNT(DISTINCT s.therapyProgram.id) FROM TherapySession s " +
                " JOIN s.attendances a WHERE a.patient = p) = " +
                "(SELECT COUNT(tp.id) FROM TherapyProgram tp WHERE tp.status = lk.ijse.theserenitymentalhealththerapycenter.entity.TherapyProgram.Status.ACTIVE)";
        return getSession().createQuery(hql, Patient.class).list();
    }

    @Override
    public List<TherapySession> findAllSessionsWithDetails() throws Exception {
        String hql = "SELECT DISTINCT s FROM TherapySession s " +
                "LEFT JOIN FETCH s.attendances a " +
                "LEFT JOIN FETCH a.patient " +
                "JOIN FETCH s.therapist " +
                "JOIN FETCH s.therapyProgram";
        return getSession().createQuery(hql, TherapySession.class).list();
    }


    @Override
    public boolean hasOverlappingSession(Long therapistId,
                                         Long patientId,
                                         LocalDateTime newSessionStart,
                                         Long excludeSessionId) {

        int sessionDurationMinutes = 60;
        LocalDateTime newSessionEnd = newSessionStart.plusMinutes(sessionDurationMinutes);
        LocalDateTime overlappingWindowStart = newSessionStart.minusMinutes(sessionDurationMinutes);

        String hql = "SELECT COUNT(s.id) FROM TherapySession s " +
                "LEFT JOIN s.attendances a " +
                "WHERE (s.therapist.id = :therapistId OR a.patient.id = :patientId) " +
                "AND s.sessionDateTime > :overlappingWindowStart " +
                "AND s.sessionDateTime < :newSessionEnd " +
                "AND s.status != lk.ijse.theserenitymentalhealththerapycenter.entity.TherapySession.Status.CANCELLED";

        if (excludeSessionId != null) {
            hql += " AND s.id != :excludeSessionId";
        }

        var query = getSession().createQuery(hql, Long.class)
                .setParameter("therapistId", therapistId)
                .setParameter("patientId", patientId)
                .setParameter("overlappingWindowStart", overlappingWindowStart)
                .setParameter("newSessionEnd", newSessionEnd);

        if (excludeSessionId != null) {
            query.setParameter("excludeSessionId", excludeSessionId);
        }

        Long count = query.uniqueResult();
        return count != null && count > 0;
    }
}