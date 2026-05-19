package lk.ijse.theserenitymentalhealththerapycenter.dto;

import lk.ijse.theserenitymentalhealththerapycenter.dto.enums.CommonStatus;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TherapyProgramDTO {
    private String id;
    private String name;
    private String duration;
    private double fee;
    private CommonStatus status;
}
