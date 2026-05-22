package lk.ijse.theserenitymentalhealththerapycenter.dao.custom.impl;

import lk.ijse.theserenitymentalhealththerapycenter.dao.BaseDAOImpl;
import lk.ijse.theserenitymentalhealththerapycenter.dao.custom.PatientDAO;
import lk.ijse.theserenitymentalhealththerapycenter.entity.Patient;
import org.hibernate.query.Query;

import java.util.List;

public class PatientDAOImpl extends BaseDAOImpl implements PatientDAO {
    @Override
    public boolean save(Patient entity) throws Exception {
        getSession().persist(entity);
        return true;
    }

    @Override
    public boolean update(Patient entity) throws Exception {
        getSession().merge(entity);
        return true;
    }

    @Override
    public boolean delete(Long id) throws Exception {
        Patient patient = findById(id);
        if (patient != null) {
            patient.setStatus(Patient.Status.INACTIVE);
            getSession().merge(patient);
            return true;
        }
        return false;
    }

    @Override
    public Patient findById(Long id) throws Exception {
        return getSession().get(Patient.class, id);
    }

    @Override
    public List<Patient> findAll() throws Exception {
        return getSession().createQuery("FROM Patient", Patient.class).list();
    }

    @Override
    public List<Patient> findAllActive() throws Exception {
        return getSession().createQuery("FROM Patient WHERE status = 'ACTIVE'", Patient.class).list();
    }

    @Override
    public boolean existsByEmail(String email) throws Exception {
        Query<Long> query = getSession().createQuery("SELECT count(p.id) FROM Patient p WHERE p.email = :email", Long.class);
        query.setParameter("email", email);
        return query.uniqueResult() > 0;
    }

    @Override
    public List<Patient> searchPatientsByProgram(String programId) throws Exception {
        System.out.println(">> Data Engine: Gathering unique active patient files assigned to program ID: " + programId);
        String hql = "SELECT DISTINCT a.patient FROM SessionAttendance a " +
                "WHERE a.session.therapyProgram.id = :progId " +
                "AND a.patient.status = lk.ijse.theserenitymentalhealththerapycenter.dto.enums.CommonStatus.ACTIVE";

        Query<Patient> query = getSession().createQuery(hql, Patient.class);
        query.setParameter("progId", programId);

        return query.list();
    }


}
