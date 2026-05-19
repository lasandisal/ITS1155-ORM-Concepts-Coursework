package lk.ijse.theserenitymentalhealththerapycenter.dao.custom.impl;

import lk.ijse.theserenitymentalhealththerapycenter.dao.BaseDAOImpl;
import lk.ijse.theserenitymentalhealththerapycenter.dao.custom.TherapyProgramDAO;
import lk.ijse.theserenitymentalhealththerapycenter.entity.TherapyProgram;

import java.util.List;

public class TherapyProgramDAOImpl extends BaseDAOImpl implements TherapyProgramDAO {
    @Override
    public boolean save(TherapyProgram entity) throws Exception {
        getSession().persist(entity);
        return true;
    }

    @Override
    public boolean update(TherapyProgram entity) throws Exception {
        getSession().merge(entity);
        return true;
    }

    @Override
    public boolean delete(String id) throws Exception {
        TherapyProgram program = findById(id);
        if (program != null) {
            program.setStatus(TherapyProgram.Status.INACTIVE);
            getSession().merge(program);
            return true;
        }
        return false;
    }

    @Override
    public TherapyProgram findById(String id) throws Exception {
        return getSession().get(TherapyProgram.class, id);
    }

    @Override
    public List<TherapyProgram> findAll() throws Exception {
        return getSession().createQuery("FROM TherapyProgram", TherapyProgram.class).list();
    }

    @Override
    public List<TherapyProgram> findAllActive() throws Exception {
        return getSession().createQuery("FROM TherapyProgram WHERE status = 'ACTIVE'", TherapyProgram.class).list();
    }
}
