package lk.ijse.theserenitymentalhealththerapycenter.bo.custom;

import lk.ijse.theserenitymentalhealththerapycenter.bo.SuperBO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.PatientDTO;

import java.util.List;

public interface PatientBO extends SuperBO {
    boolean savePatient(PatientDTO dto) throws Exception;
    boolean updatePatient(PatientDTO dto) throws Exception;
    boolean softDeletePatient(Long id) throws Exception;
    PatientDTO getPatientById(Long id) throws Exception;
    List<PatientDTO> getAllActivePatients() throws Exception;
    List<PatientDTO> searchPatientsByTherapyProgram(String programId) throws Exception;
}
