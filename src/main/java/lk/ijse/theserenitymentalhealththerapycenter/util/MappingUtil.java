package lk.ijse.theserenitymentalhealththerapycenter.util;

import lk.ijse.theserenitymentalhealththerapycenter.dto.*;
import lk.ijse.theserenitymentalhealththerapycenter.dto.enums.CommonStatus;
import lk.ijse.theserenitymentalhealththerapycenter.dto.enums.UserRole;
import lk.ijse.theserenitymentalhealththerapycenter.entity.*;

public class MappingUtil {

    // ==========================================
    // USER
    // ==========================================

    public static User toUserEntity(UserDTO dto) {
        if (dto == null) return null;

        User user = new User();
        user.setId(dto.getId());
        user.setUsername(dto.getUsername());
        user.setPassword(dto.getPassword());
        user.setFullName(dto.getFullName());
        user.setEmail(dto.getEmail());
        user.setRole(dto.getRole());
        user.setStatus(dto.getStatus());
        user.setRecoveryKeyword(dto.getRecoveryKeyword());

        return user;
    }

    public static UserDTO toUserDTO(User entity) {
        if (entity == null) return null;
        return new UserDTO(
                entity.getId(),
                entity.getUsername(),
                null,
                entity.getFullName(),
                entity.getEmail(),
                entity.getRole(),
                entity.getStatus(),
                entity.getRecoveryKeyword()
        );
    }

    // ==========================================
    // PATIENT
    // ==========================================

    public static Patient toPatientEntity(PatientDTO dto) {
        if (dto == null) return null;

        Patient patient = new Patient();
        patient.setId(dto.getId());
        patient.setName(dto.getName());
        patient.setEmail(dto.getEmail());
        patient.setPhone(dto.getPhone());
        patient.setMedicalHistory(dto.getMedicalHistory());
        patient.setRegistrationDate(dto.getRegistrationDate());

        if (dto.getStatus() != null) {
            patient.setStatus(Patient.Status.valueOf(dto.getStatus().name()));
        }

        return patient;
    }

    public static PatientDTO toPatientDTO(Patient entity) {
        if (entity == null) return null;

        return new PatientDTO(
                entity.getId(),
                entity.getName(),
                entity.getEmail(),
                entity.getPhone(),
                entity.getMedicalHistory(),
                entity.getRegistrationDate(),
                CommonStatus.valueOf(entity.getStatus().name())
        );
    }

    // ==========================================
    // THERAPIST
    // ==========================================

    public static Therapist toTherapistEntity(TherapistDTO dto) {
        if (dto == null) return null;

        Therapist therapist = new Therapist();
        therapist.setId(dto.getId());
        therapist.setName(dto.getName());
        therapist.setSpecialization(dto.getSpecialization());
        therapist.setEmail(dto.getEmail());
        therapist.setPhone(dto.getPhone());

        if (dto.getStatus() != null) {
            therapist.setStatus(Therapist.Status.valueOf(dto.getStatus().name()));
        }

        return therapist;
    }

    public static TherapistDTO toTherapistDTO(Therapist entity) {
        if (entity == null) return null;

        return new TherapistDTO(
                entity.getId(),
                entity.getName(),
                entity.getSpecialization(),
                entity.getEmail(),
                entity.getPhone(),
                CommonStatus.valueOf(entity.getStatus().name())
        );
    }

    // ==========================================
    // THERAPY PROGRAM
    // ==========================================

    public static TherapyProgram toTherapyProgramEntity(TherapyProgramDTO dto) {
        if (dto == null) return null;

        TherapyProgram program = new TherapyProgram();
        program.setId(dto.getId());
        program.setName(dto.getName());
        program.setDuration(dto.getDuration());
        program.setFee(dto.getFee());

        if (dto.getStatus() != null) {
            program.setStatus(TherapyProgram.Status.valueOf(dto.getStatus().name()));
        }

        return program;
    }

    public static TherapyProgramDTO toTherapyProgramDTO(TherapyProgram entity) {
        if (entity == null) return null;

        return new TherapyProgramDTO(
                entity.getId(),
                entity.getName(),
                entity.getDuration(),
                entity.getFee(),
                CommonStatus.valueOf(entity.getStatus().name())
        );
    }

    public static TherapySession toTherapySessionEntity(TherapySessionDTO dto) {
        if (dto == null) return null;

        TherapySession session = new TherapySession();
        session.setId(dto.getId());
        session.setSessionDateTime(dto.getSessionDateTime());

        if (dto.getStatus() != null) {
            session.setStatus(TherapySession.Status.valueOf(dto.getStatus().name()));
        }

        if (dto.getPatientId() != null) {
            Patient patient = new Patient();
            patient.setId(dto.getPatientId());
            session.setPatient(patient);
        }

        if (dto.getTherapistId() != null) {
            Therapist therapist = new Therapist();
            therapist.setId(dto.getTherapistId());
            session.setTherapist(therapist);
        }

        if (dto.getProgramId() != null) {
            TherapyProgram program = new TherapyProgram();
            program.setId(dto.getProgramId());
            session.setTherapyProgram(program);
        }

        return session;
    }

    // ==========================================
    // THERAPY SESSION
    // ==========================================

    public static TherapySessionDTO toTherapySessionDTO(TherapySession entity) {
        if (entity == null) return null;

        TherapySessionDTO dto = new TherapySessionDTO();
        dto.setId(entity.getId());
        dto.setSessionDateTime(entity.getSessionDateTime());

        if (entity.getStatus() != null) {
            dto.setStatus(lk.ijse.theserenitymentalhealththerapycenter.dto.enums.SessionStatus.valueOf(entity.getStatus().name()));
        }

        if (entity.getPatient() != null) {
            dto.setPatientId(entity.getPatient().getId());
            dto.setPatientName(entity.getPatient().getName());
        }

        if (entity.getTherapist() != null) {
            dto.setTherapistId(entity.getTherapist().getId());
            dto.setTherapistName(entity.getTherapist().getName());
        }

        if (entity.getTherapyProgram() != null) {
            dto.setProgramId(entity.getTherapyProgram().getId());
            dto.setProgramName(entity.getTherapyProgram().getName());
        }

        return dto;
    }

    // ==========================================
    // PAYMENT
    // ==========================================

    public static Payment toPaymentEntity(PaymentDTO dto) {
        if (dto == null) return null;

        Payment payment = new Payment();
        payment.setId(dto.getId());
        payment.setAmount(dto.getAmount());
        payment.setPaymentDate(dto.getPaymentDate());
        payment.setInvoiceNumber(dto.getInvoiceNumber());

        if (dto.getStatus() != null) {
            payment.setStatus(Payment.Status.valueOf(dto.getStatus().name()));
        }

        if (dto.getPatientId() != null) {
            Patient patient = new Patient();
            patient.setId(dto.getPatientId());
            payment.setPatient(patient);
        }

        if (dto.getProgramId() != null) {
            TherapyProgram program = new TherapyProgram();
            program.setId(dto.getProgramId());
            payment.setTherapyProgram(program);
        }

        if (dto.getUserId() != null) {
            User user = new User();
            user.setId(dto.getUserId());
            payment.setManagedBy(user);
        }

        return payment;
    }

    public static PaymentDTO toPaymentDTO(Payment entity) {
        if (entity == null) return null;

        PaymentDTO dto = new PaymentDTO();
        dto.setId(entity.getId());
        dto.setAmount(entity.getAmount());
        dto.setPaymentDate(entity.getPaymentDate());
        dto.setInvoiceNumber(entity.getInvoiceNumber());

        if (entity.getStatus() != null) {
            dto.setStatus(lk.ijse.theserenitymentalhealththerapycenter.dto.enums.PaymentStatus.valueOf(entity.getStatus().name()));
        }

        if (entity.getPatient() != null) {
            dto.setPatientId(entity.getPatient().getId());
            dto.setPatientName(entity.getPatient().getName());
        }

        if (entity.getTherapyProgram() != null) {
            dto.setProgramId(entity.getTherapyProgram().getId());
            dto.setProgramName(entity.getTherapyProgram().getName());
        }

        if (entity.getManagedBy() != null) {
            dto.setUserId(entity.getManagedBy().getId());
            dto.setUsername(entity.getManagedBy().getUsername());
        }

        return dto;
    }



}
