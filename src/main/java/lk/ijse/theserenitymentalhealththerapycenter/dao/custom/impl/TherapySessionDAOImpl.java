package lk.ijse.theserenitymentalhealththerapycenter.dao.custom.impl;

import lk.ijse.theserenitymentalhealththerapycenter.dao.custom.TherapySessionDAO;
import lk.ijse.theserenitymentalhealththerapycenter.entity.TherapySession;

import java.util.List;

public class TherapySessionDAOImpl implements TherapySessionDAO {
    @Override
    public boolean save(TherapySession entity) throws Exception {
        return false;
    }

    @Override
    public boolean update(TherapySession entity) throws Exception {
        return false;
    }

    @Override
    public boolean delete(Object id) throws Exception {
        return false;
    }

    @Override
    public TherapySession findById(Object id) throws Exception {
        return null;
    }

    @Override
    public List<TherapySession> findAll() throws Exception {
        return List.of();
    }
}
