package lk.ijse.theserenitymentalhealththerapycenter.dto;

import lk.ijse.theserenitymentalhealththerapycenter.dto.enums.PaymentStatus;
import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PaymentDTO {
    private Long id;
    private Long patientId;
    private String patientName;

    private String programId;
    private String programName;

    private Long userId;
    private String username;

    private double amount;
    private LocalDate paymentDate;
    private String invoiceNumber;
    private PaymentStatus status;
}