package lk.ijse.theserenitymentalhealththerapycenter.bo.custom.impl;

import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.PatientBO;
import lk.ijse.theserenitymentalhealththerapycenter.config.FactoryConfiguration;
import lk.ijse.theserenitymentalhealththerapycenter.dao.DAOFactory;
import lk.ijse.theserenitymentalhealththerapycenter.dao.DAOType;
import lk.ijse.theserenitymentalhealththerapycenter.dao.custom.PatientDAO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.PatientDTO;
import lk.ijse.theserenitymentalhealththerapycenter.entity.Patient;
import lk.ijse.theserenitymentalhealththerapycenter.exception.RegistrationException;
import lk.ijse.theserenitymentalhealththerapycenter.util.MappingUtil;
import lk.ijse.theserenitymentalhealththerapycenter.util.ValidationUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.ArrayList;
import java.util.List;

public class PatientBOImpl implements PatientBO {

    private final PatientDAO patientDAO = (PatientDAO) DAOFactory.getInstance().getDAO(DAOType.PATIENT);

    @Override
    public boolean savePatient(PatientDTO dto) throws Exception {
        if (!ValidationUtil.isRequiredFieldFilled(dto.getName()) ||
                !ValidationUtil.isRequiredFieldFilled(dto.getEmail()) ||
                !ValidationUtil.isRequiredFieldFilled(dto.getPhone())) {
            throw new RegistrationException("Registration Failed: Required descriptive fields cannot be empty.");
        }

        if (!ValidationUtil.isValidEmail(dto.getEmail())) {
            throw new RegistrationException("Registration Failed: The email layout format is invalid.");
        }

        if (!ValidationUtil.isValidSriLankanPhone(dto.getPhone())) {
            throw new RegistrationException("Registration Failed: The phone number format is invalid for Sri Lanka.");
        }

        Session session = FactoryConfiguration.getInstance().getSession();
        Transaction transaction = null;

        try {
            transaction = session.beginTransaction();

            if (patientDAO.existsByEmail(dto.getEmail())) {
                throw new RegistrationException("Registration Failed: A patient profile with email '" + dto.getEmail() + "' already exists.");
            }

            Patient patient = MappingUtil.toPatientEntity(dto);
            patient.setStatus(Patient.Status.ACTIVE);

            boolean isSaved = patientDAO.save(patient);

            transaction.commit();
            return isSaved;

        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        }
    }

    @Override
    public boolean updatePatient(PatientDTO dto) throws Exception {
        if (!ValidationUtil.isRequiredFieldFilled(dto.getName()) ||
                !ValidationUtil.isRequiredFieldFilled(dto.getEmail()) ||
                !ValidationUtil.isRequiredFieldFilled(dto.getPhone())) {
            throw new RegistrationException("Update Failed: Required input parameters cannot be left blank.");
        }

        if (!ValidationUtil.isValidEmail(dto.getEmail())) {
            throw new RegistrationException("Update Failed: Invalid target email configuration.");
        }

        if (!ValidationUtil.isValidSriLankanPhone(dto.getPhone())) {
            throw new RegistrationException("Update Failed: Invalid contact phone configuration.");
        }

        Session session = FactoryConfiguration.getInstance().getSession();
        Transaction transaction = null;

        try {
            transaction = session.beginTransaction();

            Patient patient = patientDAO.findById(dto.getId());
            if (patient == null || patient.getStatus() == Patient.Status.INACTIVE) {
                throw new RegistrationException("Update Failed: Active patient account details not found.");
            }

            patient.setName(dto.getName());
            patient.setEmail(dto.getEmail());
            patient.setPhone(dto.getPhone());
            patient.setMedicalHistory(dto.getMedicalHistory());

            boolean isUpdated = patientDAO.update(patient);
            transaction.commit();
            return isUpdated;

        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        }
    }

    @Override
    public boolean softDeletePatient(Long id) throws Exception {
        Session session = FactoryConfiguration.getInstance().getSession();
        Transaction transaction = null;

        try {
            transaction = session.beginTransaction();

            boolean isDeleted = patientDAO.delete(id);

            transaction.commit();
            return isDeleted;

        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        }
    }

    @Override
    public PatientDTO getPatientById(Long id) throws Exception {
        Session session = FactoryConfiguration.getInstance().getSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();

            Patient patient = patientDAO.findById(id);
            if (patient == null || patient.getStatus() == Patient.Status.INACTIVE) {
                transaction.commit();
                return null;
            }

            PatientDTO dto = MappingUtil.toPatientDTO(patient);
            transaction.commit();
            return dto;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        }
    }

    @Override
    public List<PatientDTO> getAllActivePatients() throws Exception {
        Session session = FactoryConfiguration.getInstance().getSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();

            List<Patient> patients = patientDAO.findAllActive();
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

    @Override
    public List<PatientDTO> searchPatientsByTherapyProgram(String programId) throws Exception {
        Session session = FactoryConfiguration.getInstance().getSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();

            List<Patient> patients = patientDAO.searchPatientsByProgram(programId);
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