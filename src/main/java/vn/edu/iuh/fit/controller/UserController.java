package vn.edu.iuh.fit.controller;import org.springframework.beans.factory.annotation.Autowired;import org.springframework.http.HttpStatus;import org.springframework.http.ResponseEntity;import org.springframework.web.bind.annotation.*;import vn.edu.iuh.fit.dto.UserAccountInfoDTO;import vn.edu.iuh.fit.dto.UserDTO;import vn.edu.iuh.fit.dto.UserInfoDTO;import vn.edu.iuh.fit.entity.User;import vn.edu.iuh.fit.service.UserService;import java.util.List;@RestController@RequestMapping("/api/users")@CrossOrigin(origins = "*", allowedHeaders = "*")public class UserController {    @Autowired    private UserService userService;    @GetMapping    public List<UserAccountInfoDTO> getAllUsers() {        return userService.getAllUsers();    }    @GetMapping("/exists/{email}")    public ResponseEntity<Boolean> checkAccountExists(@PathVariable String email) {        boolean exists = userService.existsByEmail(email);        return ResponseEntity.ok(exists);    }    @GetMapping("/{id}")    public UserInfoDTO getUserById(@PathVariable("id") long id) {        return userService.getUserById(id);    }    @PutMapping("/{id}")    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody UserInfoDTO userInfoDTO) {        UserInfoDTO updated = userService.updateUser(id, userInfoDTO);        if (updated !=null) {            return ResponseEntity.ok(updated);        } else {            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy người dùng");        }    }}