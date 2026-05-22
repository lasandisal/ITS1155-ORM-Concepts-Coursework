package lk.ijse.theserenitymentalhealththerapycenter.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientSessionStatusDTO {
    private Long sessionId;
    private LocalDateTime sessionDateTime;
    private String programName;
    private String paymentStatus;
}