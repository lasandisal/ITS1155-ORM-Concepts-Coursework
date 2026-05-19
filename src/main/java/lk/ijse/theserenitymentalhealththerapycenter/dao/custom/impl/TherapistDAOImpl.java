package lk.ijse.theserenitymentalhealththerapycenter.dao.custom.impl;

import lk.ijse.theserenitymentalhealththerapycenter.dao.BaseDAOImpl;
import lk.ijse.theserenitymentalhealththerapycenter.dao.custom.TherapistDAO;
import lk.ijse.theserenitymentalhealththerapycenter.entity.Therapist;
import org.hibernate.query.Query;

import java.time.LocalDateTime;
import java.util.List;

public class TherapistDAOImpl extends BaseDAOImpl implements TherapistDAO {
    @Override
    public boolean save(Therapist entity) throws Exception {
        getSession().persist(entity);
        return true;
    }

    @Override
    public boolean update(Therapist entity) throws Exception {
        getSession().merge(entity);
        return true;
    }

    @Override
    public boolean delete(Long id) throws Exception {
        Therapist therapist = findById(id);
        if (therapist != null) {
            therapist.setStatus(Therapist.Status.INACTIVE);
            getSession().merge(therapist);
            return true;
        }
        return false;
    }

    @Override
    public Therapist findById(Long id) throws Exception {
        return getSession().get(Therapist.class, id);
    }

    @Override
    public List<Therapist> findAll() throws Exception {
        return getSession().createQuery("FROM Therapist", Therapist.class).list();
    }

    @Override
    public List<Therapist> findAllActive() throws Exception {
        return getSession().createQuery("FROM Therapist WHERE status = 'ACTIVE'", Therapist.class).list();
    }

    @Override
    public boolean isTherapistAvailable(Long therapistId, LocalDateTime dateTime) throws Exception {
        String hql = "SELECT count(s.id) FROM TherapySession s WHERE s.therapist.id = :tId AND s.sessionDateTime = :dt AND s.status = 'SCHEDULED'";
        Query<Long> query = getSession().createQuery(hql, Long.class);
        query.setParameter("tId", therapistId);
        query.setParameter("dt", dateTime);
        return query.uniqueResult() == 0;
    }
}
