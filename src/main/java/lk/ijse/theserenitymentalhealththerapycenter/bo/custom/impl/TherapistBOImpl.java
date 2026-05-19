package lk.ijse.theserenitymentalhealththerapycenter.bo.custom.impl;

import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.TherapistBO;
import lk.ijse.theserenitymentalhealththerapycenter.config.FactoryConfiguration;
import lk.ijse.theserenitymentalhealththerapycenter.dao.DAOFactory;
import lk.ijse.theserenitymentalhealththerapycenter.dao.DAOType;
import lk.ijse.theserenitymentalhealththerapycenter.dao.custom.TherapistDAO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.TherapistDTO;
import lk.ijse.theserenitymentalhealththerapycenter.entity.Therapist;
import lk.ijse.theserenitymentalhealththerapycenter.exception.RegistrationException;
import lk.ijse.theserenitymentalhealththerapycenter.util.MappingUtil;
import lk.ijse.theserenitymentalhealththerapycenter.util.ValidationUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TherapistBOImpl implements TherapistBO {

    private final TherapistDAO therapistDAO = (TherapistDAO) DAOFactory.getInstance().getDAO(DAOType.THERAPIST);

    @Override
    public boolean saveTherapist(TherapistDTO dto) throws Exception {
        if (!ValidationUtil.isRequiredFieldFilled(dto.getName()) ||
                !ValidationUtil.isRequiredFieldFilled(dto.getSpecialization()) ||
                !ValidationUtil.isRequiredFieldFilled(dto.getEmail()) ||
                !ValidationUtil.isRequiredFieldFilled(dto.getPhone())) {
            throw new RegistrationException("Registration Failed: All therapist profile text fields are mandatory.");
        }

        if (!ValidationUtil.isValidEmail(dto.getEmail())) {
            throw new RegistrationException("Registration Failed: Provided email format is structurally invalid.");
        }

        if (!ValidationUtil.isValidSriLankanPhone(dto.getPhone())) {
            throw new RegistrationException("Registration Failed: Provided contact number is invalid for Sri Lanka.");
        }

        Session session = FactoryConfiguration.getInstance().getSession();
        Transaction transaction = null;

        try {
            transaction = session.beginTransaction();

            Therapist therapist = MappingUtil.toTherapistEntity(dto);
            therapist.setStatus(Therapist.Status.ACTIVE);

            boolean isSaved = therapistDAO.save(therapist);

            transaction.commit();
            return isSaved;

        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        } finally {
            session.close();
        }
    }

    @Override
    public boolean updateTherapist(TherapistDTO dto) throws Exception {

        if (!ValidationUtil.isRequiredFieldFilled(dto.getName()) ||
                !ValidationUtil.isRequiredFieldFilled(dto.getSpecialization()) ||
                !ValidationUtil.isRequiredFieldFilled(dto.getEmail()) ||
                !ValidationUtil.isRequiredFieldFilled(dto.getPhone())) {
            throw new RegistrationException("Update Failed: Mandated data properties cannot be left empty.");
        }

        if (!ValidationUtil.isValidEmail(dto.getEmail())) {
            throw new RegistrationException("Update Failed: Structurally incorrect email pattern string.");
        }

        if (!ValidationUtil.isValidSriLankanPhone(dto.getPhone())) {
            throw new RegistrationException("Update Failed: Contact value format exception error.");
        }

        Session session = FactoryConfiguration.getInstance().getSession();
        Transaction transaction = null;

        try {
            transaction = session.beginTransaction();

            Therapist therapist = therapistDAO.findById(dto.getId());
            if (therapist == null || therapist.getStatus() == Therapist.Status.INACTIVE) {
                throw new RegistrationException("Update Failed: Target practitioner records could not be found.");
            }

            therapist.setName(dto.getName());
            therapist.setSpecialization(dto.getSpecialization());
            therapist.setEmail(dto.getEmail());
            therapist.setPhone(dto.getPhone());

            boolean isUpdated = therapistDAO.update(therapist);

            transaction.commit();
            return isUpdated;

        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        } finally {
            session.close();
        }
    }

    @Override
    public boolean softDeleteTherapist(Long id) throws Exception {
        Session session = FactoryConfiguration.getInstance().getSession();
        Transaction transaction = null;

        try {
            transaction = session.beginTransaction();

            boolean isDeleted = therapistDAO.delete(id);

            transaction.commit();
            return isDeleted;

        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        } finally {
            session.close();
        }
    }

    @Override
    public List<TherapistDTO> getAllActiveTherapists() throws Exception {
        Session session = FactoryConfiguration.getInstance().getSession();
        try {
            List<Therapist> therapists = therapistDAO.findAllActive();
            List<TherapistDTO> dtoList = new ArrayList<>();

            for (Therapist therapist : therapists) {
                dtoList.add(MappingUtil.toTherapistDTO(therapist));
            }
            return dtoList;
        } finally {
            session.close();
        }
    }

    @Override
    public boolean checkAvailability(Long therapistId, LocalDateTime dateTime) throws Exception {
        Session session = FactoryConfiguration.getInstance().getSession();
        try {
            return therapistDAO.isTherapistAvailable(therapistId, dateTime);
        } finally {
            session.close();
        }
    }
}
