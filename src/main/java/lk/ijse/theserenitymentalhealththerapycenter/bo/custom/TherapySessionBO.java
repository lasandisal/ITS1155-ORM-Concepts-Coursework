package lk.ijse.theserenitymentalhealththerapycenter.bo.custom;

import lk.ijse.theserenitymentalhealththerapycenter.bo.SuperBO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.PatientDTO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.TherapySessionDTO;

import java.util.List;

public interface TherapySessionBO extends SuperBO {
    boolean bookSession(TherapySessionDTO dto) throws Exception;
    boolean rescheduleSession(TherapySessionDTO dto) throws Exception;
    boolean cancelSession(Long id) throws Exception;
    List<TherapySessionDTO> getAllSessionsWithFullDetails() throws Exception;
    List<PatientDTO> getPatientsEnrolledInAllPrograms() throws Exception;
}
