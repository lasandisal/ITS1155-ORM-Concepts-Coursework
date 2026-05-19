package lk.ijse.theserenitymentalhealththerapycenter.dao.custom.impl;

import lk.ijse.theserenitymentalhealththerapycenter.dao.BaseDAOImpl;
import lk.ijse.theserenitymentalhealththerapycenter.dao.custom.TherapySessionDAO;
import lk.ijse.theserenitymentalhealththerapycenter.entity.Patient;
import lk.ijse.theserenitymentalhealththerapycenter.entity.TherapySession;

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
}
