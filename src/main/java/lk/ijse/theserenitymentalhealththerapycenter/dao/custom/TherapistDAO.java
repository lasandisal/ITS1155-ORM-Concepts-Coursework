package lk.ijse.theserenitymentalhealththerapycenter.dao.custom;

import lk.ijse.theserenitymentalhealththerapycenter.dao.CrudDAO;
import lk.ijse.theserenitymentalhealththerapycenter.dao.SuperDAO;
import lk.ijse.theserenitymentalhealththerapycenter.entity.Therapist;

import java.time.LocalDateTime;
import java.util.List;

public interface TherapistDAO extends CrudDAO<Therapist, Long> {

    List<Therapist> findAllActive() throws Exception;
    boolean isTherapistAvailable(Long therapistId, LocalDateTime dateTime) throws Exception;
}
