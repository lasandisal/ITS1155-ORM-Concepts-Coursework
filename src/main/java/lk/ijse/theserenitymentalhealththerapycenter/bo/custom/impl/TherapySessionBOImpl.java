package lk.ijse.theserenitymentalhealththerapycenter.bo.custom.impl;

import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.TherapySessionBO;
import lk.ijse.theserenitymentalhealththerapycenter.config.FactoryConfiguration;
import lk.ijse.theserenitymentalhealththerapycenter.dao.DAOFactory;
import lk.ijse.theserenitymentalhealththerapycenter.dao.DAOType;
import lk.ijse.theserenitymentalhealththerapycenter.dao.custom.PatientDAO;
import lk.ijse.theserenitymentalhealththerapycenter.dao.custom.TherapistDAO;
import lk.ijse.theserenitymentalhealththerapycenter.dao.custom.TherapyProgramDAO;
import lk.ijse.theserenitymentalhealththerapycenter.dao.custom.TherapySessionDAO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.PatientDTO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.TherapySessionDTO;
import lk.ijse.theserenitymentalhealththerapycenter.entity.*;
import lk.ijse.theserenitymentalhealththerapycenter.exception.SessionScheduleException;
import lk.ijse.theserenitymentalhealththerapycenter.util.MappingUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class TherapySessionBOImpl implements TherapySessionBO {
    private final TherapySessionDAO sessionDAO = (TherapySessionDAO) DAOFactory.getInstance().getDAO(DAOType.THERAPY_SESSION);
    private final TherapistDAO therapistDAO = (TherapistDAO) DAOFactory.getInstance().getDAO(DAOType.THERAPIST);
    private final PatientDAO patientDAO = (PatientDAO) DAOFactory.getInstance().getDAO(DAOType.PATIENT);
    private final TherapyProgramDAO programDAO = (TherapyProgramDAO) DAOFactory.getInstance().getDAO(DAOType.THERAPY_PROGRAM);

    private static final LocalTime OPENING_HOUR = LocalTime.of(8, 0);
    private static final LocalTime CLOSING_HOUR = LocalTime.of(18, 0);

    @Override
    public boolean bookSession(TherapySessionDTO dto) throws Exception {
        if (dto.getPatientIds() == null || dto.getPatientIds().isEmpty()) {
            throw new SessionScheduleException("Booking Failed: A session must contain at least one patient registration entry.");
        }

        validateBusinessHours(dto.getSessionDateTime());

        Session session = FactoryConfiguration.getInstance().getSession();
        Transaction transaction = null;

        try {
            transaction = session.beginTransaction();

            TherapyProgram program = programDAO.findById(dto.getProgramId());
            if (program == null) {
                throw new SessionScheduleException("Booking Failed: Target therapeutic program missing.");
            }

            for (Long patientId : dto.getPatientIds()) {
                boolean hasOverlap = sessionDAO.hasOverlappingSession(
                        dto.getTherapistId(), patientId, dto.getSessionDateTime(), null
                );
                if (hasOverlap) {
                    throw new SessionScheduleException("Scheduling Conflict: The selected practitioner or a patient has another appointment scheduled within this 1-hour time frame.");
                }
            }

            Therapist therapist = therapistDAO.findById(dto.getTherapistId());
            TherapySession therapySession = new TherapySession();
            therapySession.setTherapist(therapist);
            therapySession.setTherapyProgram(program);
            therapySession.setSessionDateTime(dto.getSessionDateTime());
            therapySession.setStatus(TherapySession.Status.SCHEDULED);

            List<SessionAttendance> attendanceList = new ArrayList<>();
            for (Long patientId : dto.getPatientIds()) {
                Patient patient = patientDAO.findById(patientId);
                SessionAttendance attendance = new SessionAttendance();
                attendance.setSession(therapySession);
                attendance.setPatient(patient);
                attendanceList.add(attendance);
            }
            therapySession.setAttendances(attendanceList);

            boolean isSaved = sessionDAO.save(therapySession);
            transaction.commit();
            return isSaved;

        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        }
    }

    @Override
    public boolean rescheduleSession(TherapySessionDTO dto) throws Exception {
        if (dto.getId() == null || dto.getTherapistId() == null ||
                dto.getPatientIds() == null || dto.getPatientIds().isEmpty() || dto.getSessionDateTime() == null) {
            throw new SessionScheduleException("Reschedule Failed: Missing necessary identification parameters.");
        }

        validateBusinessHours(dto.getSessionDateTime());

        Session session = FactoryConfiguration.getInstance().getSession();
        Transaction transaction = null;

        try {
            transaction = session.beginTransaction();
            TherapySession activeSession = sessionDAO.findById(dto.getId());
            TherapyProgram program = programDAO.findById(dto.getProgramId());

            if (activeSession == null || activeSession.getStatus() == TherapySession.Status.CANCELLED || program == null) {
                throw new SessionScheduleException("Reschedule Failed: Active appointment record missing.");
            }

            for (Long patientId : dto.getPatientIds()) {
                boolean hasOverlap = sessionDAO.hasOverlappingSession(
                        dto.getTherapistId(), patientId, dto.getSessionDateTime(), dto.getId()
                );
                if (hasOverlap) {
                    throw new SessionScheduleException("Reschedule Conflict: The practitioner or an assigned patient has another active commitment scheduled within this 1-hour interval block.");
                }
            }

            activeSession.setSessionDateTime(dto.getSessionDateTime());
            if (dto.getStatus() != null) {
                activeSession.setStatus(TherapySession.Status.valueOf(dto.getStatus().name()));
            }

            Therapist therapist = therapistDAO.findById(dto.getTherapistId());
            activeSession.setTherapist(therapist);
            activeSession.setTherapyProgram(program);

            activeSession.getAttendances().clear();
            List<SessionAttendance> updatedAttendances = new ArrayList<>();
            for (Long patientId : dto.getPatientIds()) {
                Patient patient = patientDAO.findById(patientId);
                SessionAttendance attendance = new SessionAttendance();
                attendance.setSession(activeSession);
                attendance.setPatient(patient);
                updatedAttendances.add(attendance);
            }
            activeSession.getAttendances().addAll(updatedAttendances);

            boolean isUpdated = sessionDAO.update(activeSession);
            transaction.commit();
            return isUpdated;

        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        }
    }

    private void validateBusinessHours(LocalDateTime sessionDateTime) throws SessionScheduleException {
        LocalTime sessionTime = sessionDateTime.toLocalTime();
        if (sessionDateTime.toLocalDate().isBefore(LocalDate.now())) {
            throw new SessionScheduleException("Scheduling Error: Cannot book clinical sessions retroactively in the past.");
        }
        if (sessionTime.isBefore(OPENING_HOUR) || sessionTime.isAfter(CLOSING_HOUR)) {
            throw new SessionScheduleException("Outside Business Hours: The Serenity Center is only open for appointments between 08:00 AM and 06:00 PM.");
        }
    }

    @Override
    public boolean cancelSession(Long id) throws Exception {
        Session session = FactoryConfiguration.getInstance().getSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            boolean isCancelled = sessionDAO.delete(id);
            transaction.commit();
            return isCancelled;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        }
    }

    @Override
    public List<TherapySessionDTO> getAllSessionsWithFullDetails() throws Exception {
        Session session = FactoryConfiguration.getInstance().getSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            List<TherapySession> sessions = sessionDAO.findAllSessionsWithDetails();
            List<TherapySessionDTO> dtoList = new ArrayList<>();
            for (TherapySession s : sessions) {
                dtoList.add(MappingUtil.toTherapySessionDTO(s));
            }
            transaction.commit();
            return dtoList;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        }
    }

    @Override
    public List<PatientDTO> getPatientsEnrolledInAllPrograms() throws Exception {
        Session session = FactoryConfiguration.getInstance().getSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            List<Patient> patients = sessionDAO.findPatientsEnrolledInAllPrograms();
            List<PatientDTO> dtoList = new ArrayList<>();
            for (Patient patient : patients) {
                dtoList.add(MappingUtil.toPatientDTO(patient));
            }
            transaction.commit();
            return dtoList;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        }
    }
}