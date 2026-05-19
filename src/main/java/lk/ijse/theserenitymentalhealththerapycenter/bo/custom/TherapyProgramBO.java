package lk.ijse.theserenitymentalhealththerapycenter.bo.custom;

import lk.ijse.theserenitymentalhealththerapycenter.bo.SuperBO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.TherapyProgramDTO;

import java.util.List;

public interface TherapyProgramBO extends SuperBO {
    boolean saveProgram(TherapyProgramDTO dto) throws Exception;
    boolean updateProgram(TherapyProgramDTO dto) throws Exception;
    boolean softDeleteProgram(String id) throws Exception;
    TherapyProgramDTO getProgramById(String id) throws Exception;
    List<TherapyProgramDTO> getAllActivePrograms() throws Exception;
}
