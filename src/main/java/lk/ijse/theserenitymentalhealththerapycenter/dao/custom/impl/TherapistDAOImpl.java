package lk.ijse.theserenitymentalhealththerapycenter.dao.custom.impl;

import lk.ijse.theserenitymentalhealththerapycenter.dao.custom.TherapistDAO;
import lk.ijse.theserenitymentalhealththerapycenter.entity.Therapist;

import java.util.List;

public class TherapistDAOImpl implements TherapistDAO {
    @Override
    public boolean save(Therapist entity) throws Exception {
        return false;
    }

    @Override
    public boolean update(Therapist entity) throws Exception {
        return false;
    }

    @Override
    public boolean delete(Object id) throws Exception {
        return false;
    }

    @Override
    public Therapist findById(Object id) throws Exception {
        return null;
    }

    @Override
    public List<Therapist> findAll() throws Exception {
        return List.of();
    }
}
