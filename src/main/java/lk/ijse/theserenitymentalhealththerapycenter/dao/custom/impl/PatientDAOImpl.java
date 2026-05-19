package lk.ijse.theserenitymentalhealththerapycenter.dao.custom.impl;

import lk.ijse.theserenitymentalhealththerapycenter.dao.custom.PatientDAO;
import lk.ijse.theserenitymentalhealththerapycenter.entity.Patient;

import java.util.List;

public class PatientDAOImpl implements PatientDAO {
    @Override
    public boolean save(Patient entity) throws Exception {
        return false;
    }

    @Override
    public boolean update(Patient entity) throws Exception {
        return false;
    }

    @Override
    public boolean delete(Object id) throws Exception {
        return false;
    }

    @Override
    public Patient findById(Object id) throws Exception {
        return null;
    }

    @Override
    public List<Patient> findAll() throws Exception {
        return List.of();
    }
}
