package lk.ijse.theserenitymentalhealththerapycenter.dao.custom.impl;

import lk.ijse.theserenitymentalhealththerapycenter.dao.custom.TherapyProgramDAO;
import lk.ijse.theserenitymentalhealththerapycenter.entity.TherapyProgram;

import java.util.List;

public class TherapyProgramDAOImpl implements TherapyProgramDAO {
    @Override
    public boolean save(TherapyProgram entity) throws Exception {
        return false;
    }

    @Override
    public boolean update(TherapyProgram entity) throws Exception {
        return false;
    }

    @Override
    public boolean delete(Object id) throws Exception {
        return false;
    }

    @Override
    public TherapyProgram findById(Object id) throws Exception {
        return null;
    }

    @Override
    public List<TherapyProgram> findAll() throws Exception {
        return List.of();
    }
}
