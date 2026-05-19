package lk.ijse.theserenitymentalhealththerapycenter.bo.custom.impl;

import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.TherapyProgramBO;
import lk.ijse.theserenitymentalhealththerapycenter.config.FactoryConfiguration;
import lk.ijse.theserenitymentalhealththerapycenter.dao.DAOFactory;
import lk.ijse.theserenitymentalhealththerapycenter.dao.DAOType;
import lk.ijse.theserenitymentalhealththerapycenter.dao.custom.TherapyProgramDAO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.TherapyProgramDTO;
import lk.ijse.theserenitymentalhealththerapycenter.entity.TherapyProgram;
import lk.ijse.theserenitymentalhealththerapycenter.exception.RegistrationException;
import lk.ijse.theserenitymentalhealththerapycenter.util.MappingUtil;
import lk.ijse.theserenitymentalhealththerapycenter.util.ValidationUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.ArrayList;
import java.util.List;

public class TherapyProgramBOImpl implements TherapyProgramBO {
    private final TherapyProgramDAO programDAO = (TherapyProgramDAO) DAOFactory.getInstance().getDAO(DAOType.THERAPY_PROGRAM);

    @Override
    public boolean saveProgram(TherapyProgramDTO dto) throws Exception {
        if (!ValidationUtil.isRequiredFieldFilled(dto.getId()) ||
                !ValidationUtil.isRequiredFieldFilled(dto.getName()) ||
                !ValidationUtil.isRequiredFieldFilled(dto.getDuration())) {
            throw new RegistrationException("Registration Failed: Program ID, Name, and Duration fields are mandatory.");
        }

        if (dto.getFee() <= 0) {
            throw new RegistrationException("Registration Failed: Program operational cost fee must be greater than 0.");
        }

        Session session = FactoryConfiguration.getInstance().getSession();
        Transaction transaction = null;

        try {
            transaction = session.beginTransaction();

            TherapyProgram existing = programDAO.findById(dto.getId());
            if (existing != null) {
                throw new RegistrationException("Registration Failed: A program with ID '" + dto.getId() + "' already exists in the catalog.");
            }

            TherapyProgram program = MappingUtil.toTherapyProgramEntity(dto);
            program.setStatus(TherapyProgram.Status.ACTIVE);

            boolean isSaved = programDAO.save(program);

            transaction.commit();
            return isSaved;

        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        }
    }

    @Override
    public boolean updateProgram(TherapyProgramDTO dto) throws Exception {
        if (!ValidationUtil.isRequiredFieldFilled(dto.getName()) ||
                !ValidationUtil.isRequiredFieldFilled(dto.getDuration())) {
            throw new RegistrationException("Update Failed: Program parameters cannot be blank.");
        }

        if (dto.getFee() <= 0) {
            throw new RegistrationException("Update Failed: Invalid fee constraint values specified.");
        }

        Session session = FactoryConfiguration.getInstance().getSession();
        Transaction transaction = null;

        try {
            transaction = session.beginTransaction();

            TherapyProgram program = programDAO.findById(dto.getId());
            if (program == null || program.getStatus() == TherapyProgram.Status.INACTIVE) {
                throw new RegistrationException("Update Failed: Target catalog program could not be found.");
            }

            program.setName(dto.getName());
            program.setDuration(dto.getDuration());
            program.setFee(dto.getFee());

            boolean isUpdated = programDAO.update(program);

            transaction.commit();
            return isUpdated;

        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        }
    }

    @Override
    public boolean softDeleteProgram(String id) throws Exception {
        Session session = FactoryConfiguration.getInstance().getSession();
        Transaction transaction = null;

        try {
            transaction = session.beginTransaction();

            boolean isDeleted = programDAO.delete(id);

            transaction.commit();
            return isDeleted;

        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        }
    }

    @Override
    public TherapyProgramDTO getProgramById(String id) throws Exception {
        Session session = FactoryConfiguration.getInstance().getSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();

            TherapyProgram program = programDAO.findById(id);
            if (program == null || program.getStatus() == TherapyProgram.Status.INACTIVE) {
                transaction.commit();
                return null;
            }
            TherapyProgramDTO dto = MappingUtil.toTherapyProgramDTO(program);
            transaction.commit();
            return dto;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        }
    }

    @Override
    public List<TherapyProgramDTO> getAllActivePrograms() throws Exception {
        Session session = FactoryConfiguration.getInstance().getSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();

            List<TherapyProgram> programs = programDAO.findAllActive();
            List<TherapyProgramDTO> dtoList = new ArrayList<>();

            for (TherapyProgram program : programs) {
                dtoList.add(MappingUtil.toTherapyProgramDTO(program));
            }
            transaction.commit();
            return dtoList;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        }
    }
}