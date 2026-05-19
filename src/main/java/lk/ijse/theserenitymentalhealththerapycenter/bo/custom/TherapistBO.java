package lk.ijse.theserenitymentalhealththerapycenter.bo.custom;

import lk.ijse.theserenitymentalhealththerapycenter.bo.SuperBO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.TherapistDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface TherapistBO extends SuperBO {
    boolean saveTherapist(TherapistDTO dto) throws Exception;
    boolean updateTherapist(TherapistDTO dto) throws Exception;
    boolean softDeleteTherapist(Long id) throws Exception;
    List<TherapistDTO> getAllActiveTherapists() throws Exception;
    boolean checkAvailability(Long therapistId, LocalDateTime dateTime) throws Exception;
}
