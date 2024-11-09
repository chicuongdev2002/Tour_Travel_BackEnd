package vn.edu.iuh.fit.service;import vn.edu.iuh.fit.dto.AddressDTO;import vn.edu.iuh.fit.dto.UserAccountInfoDTO;import vn.edu.iuh.fit.dto.UserDTO;import vn.edu.iuh.fit.dto.UserInfoDTO;import vn.edu.iuh.fit.entity.User;import java.util.List;import java.util.UUID;public interface UserService extends CrudService<User, Long> {    User findByUsername(String username);    boolean existsByEmail(String email);    UserInfoDTO getUserById(long id);    boolean updateUser(Long userId, UserInfoDTO userInfoDTO);    List<UserAccountInfoDTO> getAllUsers();}