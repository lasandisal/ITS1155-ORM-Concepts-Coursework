package lk.ijse.theserenitymentalhealththerapycenter.dto;

import lk.ijse.theserenitymentalhealththerapycenter.dto.enums.SessionStatus;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TherapySessionDTO {
    private Long id;
    private Long patientId;
    private String patientName;

    private Long therapistId;
    private String therapistName;

    private String programId;
    private String programName;

    private LocalDateTime sessionDateTime;
    private SessionStatus status;
}