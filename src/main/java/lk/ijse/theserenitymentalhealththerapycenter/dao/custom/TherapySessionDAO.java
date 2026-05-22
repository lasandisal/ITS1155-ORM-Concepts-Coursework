package lk.ijse.theserenitymentalhealththerapycenter.dao.custom;

import lk.ijse.theserenitymentalhealththerapycenter.dao.CrudDAO;
import lk.ijse.theserenitymentalhealththerapycenter.dao.SuperDAO;
import lk.ijse.theserenitymentalhealththerapycenter.entity.Patient;
import lk.ijse.theserenitymentalhealththerapycenter.entity.TherapySession;

import java.time.LocalDateTime;
import java.util.List;

public interface TherapySessionDAO extends CrudDAO<TherapySession, Long> {

    List<Patient> findPatientsEnrolledInAllPrograms() throws Exception;
    List<TherapySession> findAllSessionsWithDetails() throws Exception;
    public boolean hasOverlappingSession(Long therapistId,
                                         Long patientId,
                                         LocalDateTime newSessionStart,
                                         Long excludeSessionId);
}
