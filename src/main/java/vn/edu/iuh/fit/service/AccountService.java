package vn.edu.iuh.fit.service;import jakarta.servlet.http.HttpServletResponse;import vn.edu.iuh.fit.dto.AccountDTO;import vn.edu.iuh.fit.dto.UserDTO;import org.springframework.data.domain.Page;import vn.edu.iuh.fit.dto.UserInfoDTO;import vn.edu.iuh.fit.dto.UserRegistrationDTO;import vn.edu.iuh.fit.dto.respone.LoginResponseDTO;import vn.edu.iuh.fit.entity.Account;import vn.edu.iuh.fit.entity.User;import vn.edu.iuh.fit.enums.AccountRole;import java.util.List;public interface AccountService extends CrudService<Account, Long>{  //    User register(String username, String password, User user);  User register(UserRegistrationDTO registrationDTO);//  UserInfoDTO login(String username, String password);//LoginResponseDTO login(String username, String password); LoginResponseDTO login(String username, String password, HttpServletResponse respons);  void resetPassword(String username, String newPassword);  boolean existsByUsername(String username);  String getEmailByUsername(String username);  Page<Account> getPageList(int page, int size, String sortBy, String sortDirection);  //   Page<AccountDTO> getAllAccounts(int page, int size);  Page<AccountDTO> getAllAccounts(int page, int size, String role, String status);  void changePassword(long userId, String oldPassword, String newPassword);  AccountDTO getAccountById(long accountId);  AccountDTO updateAccount(long accountId, AccountDTO accountDTO);  void unlockAccount(long userId);  void lockAccount(long userId);  void upgradeAccount(long userId, AccountRole newRole);  void adminResetPassword(Long[] userIds);  UserInfoDTO loginWithEmail(String email);}