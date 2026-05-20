package lk.ijse.theserenitymentalhealththerapycenter.bo.custom.impl;

import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.UserBO;
import lk.ijse.theserenitymentalhealththerapycenter.config.FactoryConfiguration;
import lk.ijse.theserenitymentalhealththerapycenter.dao.DAOFactory;
import lk.ijse.theserenitymentalhealththerapycenter.dao.DAOType;
import lk.ijse.theserenitymentalhealththerapycenter.dao.custom.UserDAO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.UserDTO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.enums.CommonStatus;
import lk.ijse.theserenitymentalhealththerapycenter.dto.enums.UserRole;
import lk.ijse.theserenitymentalhealththerapycenter.entity.User;
import lk.ijse.theserenitymentalhealththerapycenter.exception.LoginException;
import lk.ijse.theserenitymentalhealththerapycenter.exception.RegistrationException;
import lk.ijse.theserenitymentalhealththerapycenter.util.MappingUtil;
import lk.ijse.theserenitymentalhealththerapycenter.util.PasswordUtil;
import lk.ijse.theserenitymentalhealththerapycenter.util.ValidationUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.ArrayList;
import java.util.List;

public class UserBOImpl implements UserBO {

    private final UserDAO userDAO = (UserDAO) DAOFactory.getInstance().getDAO(DAOType.USER);

    @Override
    public boolean registerUser(UserDTO dto) throws Exception {
        if (!ValidationUtil.isRequiredFieldFilled(dto.getUsername()) ||
                !ValidationUtil.isRequiredFieldFilled(dto.getPassword()) ||
                !ValidationUtil.isRequiredFieldFilled(dto.getFullName())) {
            throw new RegistrationException("Registration failed: Missing required fields.");
        }

        if (!ValidationUtil.isValidEmail(dto.getEmail())) {
            throw new RegistrationException("Registration failed: Invalid email format.");
        }

        Session session = FactoryConfiguration.getInstance().getSession();
        Transaction transaction = null;

        try {
            transaction = session.beginTransaction();

            if (userDAO.existsByUsername(dto.getUsername())) {
                throw new RegistrationException("Registration failed: Username '" + dto.getUsername() + "' is already taken.");
            }

            User user = MappingUtil.toUserEntity(dto);

            String hashedPassword = PasswordUtil.hashPassword(dto.getPassword());
            user.setPassword(hashedPassword);
            user.setStatus(CommonStatus.ACTIVE);

            boolean isSaved = userDAO.save(user);

            transaction.commit();
            return isSaved;

        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        }
    }

    @Override
    public UserDTO authenticate(String username, String rawPassword) throws Exception {
        if (!ValidationUtil.isRequiredFieldFilled(username) || !ValidationUtil.isRequiredFieldFilled(rawPassword)) {
            throw new LoginException("Login failed: Fields cannot be empty.");
        }

        Session session = FactoryConfiguration.getInstance().getSession();
        Transaction transaction = null;

        try {
            transaction = session.beginTransaction();

            User user = userDAO.findByUsername(username);

            if (user == null) {
                throw new LoginException("Login failed: Invalid credentials.");
            }

            if (!PasswordUtil.checkPassword(rawPassword, user.getPassword())) {
                throw new LoginException("Login failed: Invalid credentials.");
            }

            if (user.getStatus() == CommonStatus.INACTIVE) {
                throw new LoginException("Login failed: This user account has been deactivated.");
            }

            transaction.commit();
            return MappingUtil.toUserDTO(user);

        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        }
    }

    @Override
    public boolean updateUserProfile(UserDTO dto) throws Exception {
        Session session = FactoryConfiguration.getInstance().getSession();
        Transaction transaction = null;

        try {
            transaction = session.beginTransaction();

            User user = userDAO.findById(dto.getId());
            if (user == null || user.getStatus() == CommonStatus.INACTIVE) {
                throw new LoginException("Update failed: Active user profile context not found.");
            }
            user.setFullName(dto.getFullName());
            user.setEmail(dto.getEmail());
            user.setRole(UserRole.valueOf(dto.getRole().name()));
            user.setRecoveryKeyword(dto.getRecoveryKeyword());

            if (dto.getPassword() != null && !dto.getPassword().trim().isEmpty()) {
                String encryptedPassword = PasswordUtil.hashPassword(dto.getPassword());
                user.setPassword(encryptedPassword);
                System.out.println(">> Security Core: Cryptographic hash updated for user: " + dto.getUsername());
            }

            boolean isUpdated = userDAO.update(user);
            transaction.commit();
            return isUpdated;

        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        }
    }

    @Override
    public boolean softDeleteUser(Long id) throws Exception {
        Session session = FactoryConfiguration.getInstance().getSession();
        Transaction transaction = null;

        try {
            transaction = session.beginTransaction();

            boolean isDeleted = userDAO.delete(id);

            transaction.commit();
            return isDeleted;

        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        }
    }

    @Override
    public List<UserDTO> getAllActiveUsers() throws Exception {
        Session session = FactoryConfiguration.getInstance().getSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();

            List<User> users = userDAO.findAll();
            List<UserDTO> dtoList = new ArrayList<>();

            for (User user : users) {
                if (user.getStatus() == CommonStatus.ACTIVE) {
                    dtoList.add(MappingUtil.toUserDTO(user));
                }
            }

            transaction.commit();
            return dtoList;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        }
    }
}