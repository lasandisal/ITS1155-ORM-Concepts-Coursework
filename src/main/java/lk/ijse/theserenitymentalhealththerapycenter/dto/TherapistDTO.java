package lk.ijse.theserenitymentalhealththerapycenter.dto;

import lk.ijse.theserenitymentalhealththerapycenter.dto.enums.CommonStatus;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TherapistDTO {
    private Long id;
    private String name;
    private String specialization;
    private String email;
    private String phone;
    private CommonStatus status;
}
