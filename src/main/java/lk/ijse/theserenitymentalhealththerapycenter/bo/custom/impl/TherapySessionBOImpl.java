package lk.ijse.theserenitymentalhealththerapycenter.bo.custom.impl;

import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.TherapySessionBO;
import lk.ijse.theserenitymentalhealththerapycenter.config.FactoryConfiguration;
import lk.ijse.theserenitymentalhealththerapycenter.dao.DAOFactory;
import lk.ijse.theserenitymentalhealththerapycenter.dao.DAOType;
import lk.ijse.theserenitymentalhealththerapycenter.dao.custom.PatientDAO;
import lk.ijse.theserenitymentalhealththerapycenter.dao.custom.TherapistDAO;
import lk.ijse.theserenitymentalhealththerapycenter.dao.custom.TherapySessionDAO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.PatientDTO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.TherapySessionDTO;
import lk.ijse.theserenitymentalhealththerapycenter.entity.Patient;
import lk.ijse.theserenitymentalhealththerapycenter.entity.TherapySession;
import lk.ijse.theserenitymentalhealththerapycenter.exception.SessionScheduleException;
import lk.ijse.theserenitymentalhealththerapycenter.util.MappingUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.ArrayList;
import java.util.List;

public class TherapySessionBOImpl implements TherapySessionBO {
    private final TherapySessionDAO sessionDAO = (TherapySessionDAO) DAOFactory.getInstance().getDAO(DAOType.THERAPY_SESSION);
    private final TherapistDAO therapistDAO = (TherapistDAO) DAOFactory.getInstance().getDAO(DAOType.THERAPIST);
    private final PatientDAO patientDAO = (PatientDAO) DAOFactory.getInstance().getDAO(DAOType.PATIENT);

    @Override
    public boolean bookSession(TherapySessionDTO dto) throws Exception {
        if (dto.getPatientId() == null || dto.getTherapistId() == null ||
                dto.getProgramId() == null || dto.getSessionDateTime() == null) {
            throw new SessionScheduleException("Booking Failed: Complete session scheduling details must be supplied.");
        }

        Session session = FactoryConfiguration.getInstance().getSession();
        Transaction transaction = null;

        try {
            transaction = session.beginTransaction();

            boolean isAvailable = therapistDAO.isTherapistAvailable(dto.getTherapistId(), dto.getSessionDateTime());
            // ✅ FIXED: Using specialized SessionScheduleException for resource Clashes
            if (!isAvailable) {
                throw new SessionScheduleException("Booking Failed: Selected Therapist is already booked for this specific time slot.");
            }

            TherapySession therapySession = MappingUtil.toTherapySessionEntity(dto);
            therapySession.setStatus(TherapySession.Status.SCHEDULED);

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
        if (dto.getId() == null || dto.getTherapistId() == null || dto.getSessionDateTime() == null) {
            throw new SessionScheduleException("Reschedule Failed: Missing identification parameters.");
        }

        Session session = FactoryConfiguration.getInstance().getSession();
        Transaction transaction = null;

        try {
            transaction = session.beginTransaction();

            TherapySession activeSession = sessionDAO.findById(dto.getId());
            if (activeSession == null || activeSession.getStatus() == TherapySession.Status.CANCELLED) {
                throw new SessionScheduleException("Reschedule Failed: Active appointment record not found.");
            }

            boolean isAvailable = therapistDAO.isTherapistAvailable(dto.getTherapistId(), dto.getSessionDateTime());
            if (!isAvailable) {
                throw new SessionScheduleException("Reschedule Failed: The selected practitioner is unavailable at the new requested time.");
            }

            activeSession.setSessionDateTime(dto.getSessionDateTime());

            boolean isUpdated = sessionDAO.update(activeSession);
            transaction.commit();
            return isUpdated;

        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
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