package lk.ijse.theserenitymentalhealththerapycenter.dto;

import lk.ijse.theserenitymentalhealththerapycenter.dto.enums.CommonStatus;
import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PatientDTO {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String medicalHistory;
    private LocalDate registrationDate;
    private CommonStatus status;
}