package lk.ijse.theserenitymentalhealththerapycenter.bo.custom.impl;

import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.TherapySessionBO;
import lk.ijse.theserenitymentalhealththerapycenter.config.FactoryConfiguration;
import lk.ijse.theserenitymentalhealththerapycenter.dao.DAOFactory;
import lk.ijse.theserenitymentalhealththerapycenter.dao.DAOType;
import lk.ijse.theserenitymentalhealththerapycenter.dao.custom.PatientDAO;
import lk.ijse.theserenitymentalhealththerapycenter.dao.custom.TherapistDAO;
import lk.ijse.theserenitymentalhealththerapycenter.dao.custom.TherapyProgramDAO; // ✅ Added for program lookup
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
    private final TherapyProgramDAO programDAO = (TherapyProgramDAO) DAOFactory.getInstance().getDAO(DAOType.THERAPY_PROGRAM); // ✅ Connected

    private static final LocalTime OPENING_HOUR = LocalTime.of(8, 0);   // 08:00 AM
    private static final LocalTime CLOSING_HOUR = LocalTime.of(18, 0); // 06:00 PM
    private static final long SESSION_BUFFER_MINUTES = 60;             // 1-Hour Time Block Buffer

//    @Override
//    public boolean bookSession(TherapySessionDTO dto) throws Exception {
//        if (dto.getPatientId() == null || dto.getTherapistId() == null ||
//                dto.getProgramId() == null || dto.getSessionDateTime() == null) {
//            throw new SessionScheduleException("Booking Failed: Complete session scheduling details must be supplied.");
//        }
//
//        validateBusinessHours(dto.getSessionDateTime());
//
//        Session session = FactoryConfiguration.getInstance().getSession();
//        Transaction transaction = null;
//
//        try {
//            transaction = session.beginTransaction();
//
//            TherapyProgram program = programDAO.findById(dto.getProgramId());
//            if (program == null) {
//                throw new SessionScheduleException("Booking Failed: Target therapeutic program framework not found.");
//            }
//
//            LocalDateTime requestedTime = dto.getSessionDateTime();
//            LocalDateTime startWindow = requestedTime.minusMinutes(SESSION_BUFFER_MINUTES);
//            LocalDateTime endWindow = requestedTime.plusMinutes(SESSION_BUFFER_MINUTES);
//
//            boolean isGroupProgram = program.getName() != null && program.getName().toLowerCase().contains("group");
//            boolean hasOverlap;
//
//            if (isGroupProgram) {
//                // Group Session Rule: Patient can't be busy, and Therapist can't be teaching a DIFFERENT program right now
//                String hql = "SELECT COUNT(s.id) FROM TherapySession s WHERE " +
//                        "(s.patient.id = :patientId OR (s.therapist.id = :therapistId AND s.therapyProgram.id != :programId)) " +
//                        "AND s.sessionDateTime > :startWindow " +
//                        "AND s.sessionDateTime < :endWindow " +
//                        "AND s.status != lk.ijse.theserenitymentalhealththerapycenter.entity.TherapySession.Status.CANCELLED";
//
//                hasOverlap = session.createQuery(hql, Long.class)
//                        .setParameter("patientId", dto.getPatientId())
//                        .setParameter("therapistId", dto.getTherapistId())
//                        .setParameter("programId", dto.getProgramId())
//                        .setParameter("startWindow", startWindow)
//                        .setParameter("endWindow", endWindow)
//                        .uniqueResult() > 0;
//            } else {
//                hasOverlap = sessionDAO.hasOverlappingSession(
//                        dto.getTherapistId(), dto.getPatientId(), startWindow, endWindow, null
//                );
//            }
//
//            if (hasOverlap) {
//                throw new SessionScheduleException("Scheduling Conflict: The selected Therapist or Patient has an overlapping commitment within this 1-hour session block window.");
//            }
//
//            TherapySession therapySession = MappingUtil.toTherapySessionEntity(dto);
//            therapySession.setStatus(TherapySession.Status.SCHEDULED);
//
//            boolean isSaved = sessionDAO.save(therapySession);
//            transaction.commit();
//            return isSaved;
//
//        } catch (Exception e) {
//            if (transaction != null) transaction.rollback();
//            throw e;
//        }
//    }

    @Override
    public boolean bookSession(TherapySessionDTO dto) throws Exception {
        // 1. Core Validations (Check business hours, therapist availability windows)
        validateBusinessHours(dto.getSessionDateTime());

        Session session = FactoryConfiguration.getInstance().getSession();
        Transaction transaction = null;

        try {
            transaction = session.beginTransaction();

            // 2. Fetch the structural fields
            Therapist therapist = therapistDAO.findById(dto.getTherapistId());
            TherapyProgram program = programDAO.findById(dto.getProgramId());

            // 3. Instantiate exactly ONE session event row (The structural room)
            TherapySession therapySession = new TherapySession();
            therapySession.setTherapist(therapist);
            therapySession.setTherapyProgram(program);
            therapySession.setSessionDateTime(dto.getSessionDateTime());
            therapySession.setStatus(TherapySession.Status.SCHEDULED);

            // 4. Loop through selected patient IDs and link them via the bridge entity
            List<SessionAttendance> attendanceList = new ArrayList<>();
            for (Long patientId : dto.getPatientIds()) {
                Patient patient = patientDAO.findById(patientId);

                SessionAttendance attendance = new SessionAttendance();
                attendance.setSession(therapySession); // Owning side reference
                attendance.setPatient(patient);

                attendanceList.add(attendance);
            }

            // Inject the collections into the parent session object
            therapySession.setAttendances(attendanceList);

            // 5. Save the single session object (CascadeType.ALL saves the attendances automatically!)
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
        // 1. Structural Identifiers Validation
        if (dto.getId() == null || dto.getTherapistId() == null ||
                dto.getPatientIds() == null || dto.getPatientIds().isEmpty() || dto.getSessionDateTime() == null) {
            throw new SessionScheduleException("Reschedule Failed: Missing necessary identification parameters.");
        }

        validateBusinessHours(dto.getSessionDateTime());

        Session session = FactoryConfiguration.getInstance().getSession();
        Transaction transaction = null;

        try {
            transaction = session.beginTransaction();

            // 2. Fetch the existing persistent managed entity and target program
            TherapySession activeSession = sessionDAO.findById(dto.getId());
            TherapyProgram program = programDAO.findById(dto.getProgramId());

            if (activeSession == null || activeSession.getStatus() == TherapySession.Status.CANCELLED || program == null) {
                throw new SessionScheduleException("Reschedule Failed: Active appointment record tracking indices missing.");
            }

            // 3. Time Windows Setup for Clashing Checks
            LocalDateTime requestedTime = dto.getSessionDateTime();
            LocalDateTime startWindow = requestedTime.minusMinutes(SESSION_BUFFER_MINUTES);
            LocalDateTime endWindow = requestedTime.plusMinutes(SESSION_BUFFER_MINUTES);

            boolean isGroupProgram = program.getName() != null && program.getName().toLowerCase().contains("group");

            // 4. Overlap Logic Redesigned for the Junction Table
            if (isGroupProgram) {
                // Check: Are any of the incoming patients busy in another active session?
                // And is this therapist busy with a DIFFERENT program framework?
                String hql = "SELECT COUNT(s.id) FROM TherapySession s " +
                        "LEFT JOIN s.attendances a " +
                        "WHERE (a.patient.id IN (:patientIds) OR (s.therapist.id = :therapistId AND s.therapyProgram.id != :programId)) " +
                        "AND s.sessionDateTime > :startWindow " +
                        "AND s.sessionDateTime < :endWindow " +
                        "AND s.status != lk.ijse.theserenitymentalhealththerapycenter.entity.TherapySession.Status.CANCELLED " +
                        "AND s.id != :excludeSessionId";

                boolean hasOverlap = session.createQuery(hql, Long.class)
                        .setParameterList("patientIds", dto.getPatientIds())
                        .setParameter("therapistId", dto.getTherapistId())
                        .setParameter("programId", dto.getProgramId())
                        .setParameter("startWindow", startWindow)
                        .setParameter("endWindow", endWindow)
                        .setParameter("excludeSessionId", dto.getId())
                        .uniqueResult() > 0;

                if (hasOverlap) {
                    throw new SessionScheduleException("Reschedule Conflict: A selected patient or the practitioner has another active commitment within this adjusted group window.");
                }
            } else {
                // Individual Program Overlap Fallback
                Long singlePatientId = dto.getPatientIds().get(0);
                boolean hasOverlap = sessionDAO.hasOverlappingSession(
                        dto.getTherapistId(), singlePatientId, startWindow, endWindow, dto.getId()
                );
                if (hasOverlap) {
                    throw new SessionScheduleException("Reschedule Conflict: The practitioner or patient has another active commitment assigned within this adjusted 1-hour time buffer.");
                }
            }

            // 5. Update Core Structural Properties
            activeSession.setSessionDateTime(dto.getSessionDateTime());
            if (dto.getStatus() != null) {
                activeSession.setStatus(TherapySession.Status.valueOf(dto.getStatus().name()));
            }

            // Load the assigned Therapist proxy
            Therapist therapist = therapistDAO.findById(dto.getTherapistId());
            activeSession.setTherapist(therapist);
            activeSession.setTherapyProgram(program);

            // 6. Synchronize the Attendances Collection (Handle modifications to the group list)
            // Clear old child attendance references from the collection mapping array
            activeSession.getAttendances().clear();

            // Repopulate with the updated patient batch payload
            List<SessionAttendance> updatedAttendances = new ArrayList<>();
            for (Long patientId : dto.getPatientIds()) {
                Patient patient = patientDAO.findById(patientId);

                SessionAttendance attendance = new SessionAttendance();
                attendance.setSession(activeSession);
                attendance.setPatient(patient);

                updatedAttendances.add(attendance);
            }

            // Crucial: Clear orphaned links at database row execution layer if your sessionDAO clears records,
            // or let CascadeType.ALL handle deletions if orphanRemoval=true is on.
            activeSession.getAttendances().addAll(updatedAttendances);

            // 7. Flush state completely down to JDBC driver persistence contexts
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