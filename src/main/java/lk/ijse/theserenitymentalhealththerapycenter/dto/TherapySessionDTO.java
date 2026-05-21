package lk.ijse.theserenitymentalhealththerapycenter.dto;

import lk.ijse.theserenitymentalhealththerapycenter.dto.enums.SessionStatus;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TherapySessionDTO {
    private Long id;
//    private Long patientId;
//    private String patientName;

    private List<Long> patientIds = new ArrayList<>();
    private List<String> patientNames = new ArrayList<>();

    private Long therapistId;
    private String therapistName;

    private String programId;
    private String programName;

    private LocalDateTime sessionDateTime;
    private SessionStatus status;

    public String getFormattedPatientNames() {
        if (patientNames == null || patientNames.isEmpty()) {
            return "No Patients Registered";
        }
        return String.join(", ", patientNames); // Renders as "Rusiru Salwathura, Lasandi Salwathura"
    }
}