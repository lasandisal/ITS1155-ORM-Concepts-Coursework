package lk.ijse.theserenitymentalhealththerapycenter.dao.custom;

import lk.ijse.theserenitymentalhealththerapycenter.dao.CrudDAO;
import lk.ijse.theserenitymentalhealththerapycenter.dao.SuperDAO;
import lk.ijse.theserenitymentalhealththerapycenter.entity.Patient;

import java.util.List;

public interface PatientDAO extends CrudDAO<Patient, Long> {

    List<Patient> findAllActive() throws Exception;
    boolean existsByEmail(String email) throws Exception;
    List<Patient> searchPatientsByProgram(String programId) throws Exception;

}
