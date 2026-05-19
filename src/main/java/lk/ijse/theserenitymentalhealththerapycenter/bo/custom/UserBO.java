package lk.ijse.theserenitymentalhealththerapycenter.bo.custom;

import lk.ijse.theserenitymentalhealththerapycenter.bo.SuperBO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.UserDTO;

import java.util.List;

public interface UserBO extends SuperBO {
    boolean registerUser(UserDTO dto) throws Exception;
    UserDTO authenticate(String username, String rawPassword) throws Exception;
    boolean updateUserProfile(UserDTO dto) throws Exception;
    boolean softDeleteUser(Long id) throws Exception;
    List<UserDTO> getAllActiveUsers() throws Exception;
}
