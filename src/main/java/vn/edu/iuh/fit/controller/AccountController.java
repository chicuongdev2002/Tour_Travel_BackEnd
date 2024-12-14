package vn.edu.iuh.fit.controller;import jakarta.servlet.http.Cookie;import jakarta.servlet.http.HttpServletRequest;import jakarta.servlet.http.HttpServletResponse;import org.springframework.beans.factory.annotation.Autowired;import org.springframework.data.domain.Page;import org.springframework.http.HttpStatus;import org.springframework.http.ResponseEntity;import org.springframework.web.bind.annotation.*;import vn.edu.iuh.fit.dto.*;import vn.edu.iuh.fit.dto.request.RefreshTokenRequest;import vn.edu.iuh.fit.dto.request.ResetPasswordRequest;import vn.edu.iuh.fit.dto.respone.AuthResponse;import vn.edu.iuh.fit.dto.respone.LoginResponseDTO;import vn.edu.iuh.fit.entity.Account;import vn.edu.iuh.fit.entity.Token;import vn.edu.iuh.fit.entity.User;import vn.edu.iuh.fit.enums.AccountRole;import vn.edu.iuh.fit.exception.*;import vn.edu.iuh.fit.repositories.AccountRepository;import vn.edu.iuh.fit.service.AccountService;import vn.edu.iuh.fit.service.token.TokenService;import java.util.Arrays;import java.util.List;import java.util.Optional;@RestController@RequestMapping("/api/accounts")@CrossOrigin(origins = "*", allowedHeaders = "*")public class AccountController {    @Autowired    private AccountService accountService;    @Autowired    private TokenService tokenService;    @GetMapping("/email/{username}")    public ResponseEntity<String> getEmailByUsername(@PathVariable String username) {        String email = accountService.getEmailByUsername(username);        if (email != null) {            return ResponseEntity.ok(email);        } else {            return ResponseEntity.notFound().build();        }    }    @GetMapping("/exists/{username}")    public ResponseEntity<Boolean> checkAccountExists(@PathVariable String username) {        boolean exists = accountService.existsByUsername(username);        return ResponseEntity.ok(exists);    }    @PutMapping("/change-password")    public ResponseEntity<String> changePassword(            @RequestParam long userId,            @RequestParam String oldPassword,            @RequestParam String newPassword) {        try {            accountService.changePassword(userId, oldPassword, newPassword);            return ResponseEntity.ok("Mật khẩu đã được thay đổi thành công.");        } catch (RuntimeException e) {            return ResponseEntity.badRequest().body(e.getMessage());        }    }    @PostMapping("/register")    public ResponseEntity<?> register(@RequestBody UserRegistrationDTO registrationDTO) {        try {            User newUser = accountService.register(registrationDTO);            return new ResponseEntity<>(newUser, HttpStatus.CREATED);        } catch (AccountAlreadyExistsException e) {            return ResponseEntity.status(HttpStatus.CONFLICT)                    .body(new ErrorResponse(e.getMessage()));        } catch (EmailAlreadyExistsException e) {            return ResponseEntity.status(HttpStatus.CONFLICT)                    .body(new ErrorResponse(e.getMessage()));        } catch (Exception e) {            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)                    .body(new ErrorResponse("Đã xảy ra lỗi. Vui lòng thử lại."));        }    }    @PostMapping("/login")    public ResponseEntity<?> login(@RequestParam String username, @RequestParam String password, HttpServletResponse response, HttpServletRequest request) {        try {            response.setHeader("Access-Control-Allow-Origin", "https://two2-webtour.onrender.com");            response.setHeader("Access-Control-Allow-Credentials", "true");            response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");            // Gọi service để thực hiện đăng nhập            LoginResponseDTO loginResponse = accountService.login(username, password, response);            // Nếu đăng nhập thành công, trả về phản hồi với thông tin người dùng và access token            return ResponseEntity.ok()                    .body(loginResponse);        } catch (AccountNotFoundException ex) {            // Nếu tài khoản không tồn tại, trả về 404 Not Found            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());        } catch (InvalidPasswordException ex) {            // Nếu mật khẩu không đúng, trả về 401 Unauthorized            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());        } catch (AccountLockedException ex) {            // Nếu tài khoản bị khóa, trả về 403 Forbidden            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());        } catch (Exception ex) {            // Xử lý các lỗi khác, trả về phản hồi 500 Internal Server Error            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Đã xảy ra lỗi hệ thống.");        }    }    @PostMapping("/login-with-email")    public ResponseEntity<UserInfoDTO> loginWithEmail(@RequestParam String email) {        UserInfoDTO userInfo = accountService.loginWithEmail(email);        return ResponseEntity.ok(userInfo);    }    @PostMapping("/reset-password")    public ResponseEntity<Void> resetPassword(@RequestParam String email, @RequestParam String newPassword) {        try {            accountService.resetPassword(email, newPassword);            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();        } catch (RuntimeException e) {            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);        }    }    @PostMapping("/admin-reset-password")    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequest request) {        accountService.adminResetPassword(request.getUserIds());        return ResponseEntity.ok("Mật khẩu đã được reset và gửi qua email.");    }    @GetMapping("/page")    public ResponseEntity<Page<Account>> getPage(@RequestParam(required = false, defaultValue = "0") int page, @RequestParam(required = false, defaultValue = "10") int size){        return ResponseEntity.ok(accountService.getPageList(page,size,null, null));    }    @GetMapping("/{id}")    public AccountDTO getAccountById(@PathVariable long id) {        return accountService.getAccountById(id);    }    @PutMapping()    public ResponseEntity<AccountDTO> updateAccount(@RequestBody AccountDTO accountDTO) {        AccountDTO updatedAccount = accountService.updateAccount(accountDTO.getUserId(), accountDTO);        return ResponseEntity.ok(updatedAccount);    }    @GetMapping    public Page<AccountDTO> getAllAccounts(            @RequestParam(defaultValue = "0") int page,            @RequestParam(defaultValue = "10") int size,            @RequestParam(required = false) String role,            @RequestParam(required = false) String status) {        return accountService.getAllAccounts(page, size, role, status);    }    @PostMapping("/upgrade/{userId}/{newRole}")    public ResponseEntity<String> upgradeAccount(@PathVariable long userId, @PathVariable AccountRole newRole) {        accountService.upgradeAccount(userId, newRole);        return ResponseEntity.ok("Account upgraded successfully.");    }    @PostMapping("/lock/{userId}")    public ResponseEntity<String> lockAccount(@PathVariable long userId) {        accountService.lockAccount(userId);        return ResponseEntity.ok("Account locked successfully.");    }    @PostMapping("/unlock/{userId}")    public ResponseEntity<String> unlockAccount(@PathVariable long userId) {        accountService.unlockAccount(userId);        return ResponseEntity.ok("Account unlocked successfully.");    }    @PostMapping("/refresh-token")    public ResponseEntity<?> refreshToken(@CookieValue("refreshToken") String refreshToken, HttpServletResponse response, HttpServletRequest request) {        try {            Cookie[] cookies = request.getCookies();            System.out.println("Received cookies: " + Arrays.toString(cookies));            Token newToken = tokenService.refreshToken(refreshToken);            // Lưu refresh token mới vào cookie            Cookie cookie = new Cookie("refreshToken", newToken.getRefreshToken());            cookie.setHttpOnly(true); // Ngăn chặn truy cập từ JavaScript            cookie.setSecure(true);     // Chỉ gửi cookie qua HTTPS            cookie.setPath("/"); // Áp dụng cho toàn bộ ứng dụng            cookie.setDomain("localhost");            cookie.setMaxAge(60 * 60 * 24 * 30); // Thời gian sống 30 ngày            response.setHeader("Access-Control-Allow-Origin", "https://two2-webtour.onrender.com");            response.setHeader("Access-Control-Allow-Credentials", "true");            response.addCookie(cookie); // Thêm cookie vào phản hồi            return ResponseEntity.ok(new AuthResponse(newToken.getToken()));        } catch (RuntimeException e) {            if (e.getMessage().equals("Expired refresh token")) {                return ResponseEntity.status(HttpStatus.GONE) // 410 Gone                        .body("REFRESH_TOKEN_EXPIRED");            } else {                return ResponseEntity.status(HttpStatus.UNAUTHORIZED) // 401 Unauthorized                        .body("INVALID_REFRESH_TOKEN");            }        }    }    @DeleteMapping("/logout")    public ResponseEntity<String> logout(@CookieValue("refreshToken") String refreshToken, HttpServletResponse response) {        try {            Optional<Token> tokenOpt = tokenService.findByRefreshToken(refreshToken);            if (tokenOpt.isPresent()) {                Token token = tokenOpt.get();                tokenService.revokeAllUserTokens(token.getUser());            }            Cookie cookie = new Cookie("refreshToken", null);            cookie.setHttpOnly(true);            cookie.setSecure(true);            cookie.setPath("/");            cookie.setMaxAge(0);            response.addCookie(cookie);            return ResponseEntity.ok("Đăng xuất thành công.");        } catch (Exception e) {            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)                    .body("Đã xảy ra lỗi khi đăng xuất.");        }    }}