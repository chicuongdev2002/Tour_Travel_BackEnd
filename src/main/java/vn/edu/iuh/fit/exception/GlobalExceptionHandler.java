package vn.edu.iuh.fit.exception;import org.springframework.http.HttpStatus;import org.springframework.http.ResponseEntity;import org.springframework.web.bind.annotation.ControllerAdvice;import org.springframework.web.bind.annotation.ExceptionHandler;import vn.edu.iuh.fit.exception.AccountAlreadyExistsException;@ControllerAdvicepublic class GlobalExceptionHandler {    //ResponseEntity trả về khi có lỗi    @ExceptionHandler(AccountNotFoundException.class)    public ResponseEntity<String> handleAccountNotFoundException(AccountNotFoundException ex) {        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());    }    @ExceptionHandler(InvalidPasswordException.class)    public ResponseEntity<String> handleInvalidPasswordException(InvalidPasswordException ex) {        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());    }    @ExceptionHandler(AccountAlreadyExistsException.class)    public ResponseEntity<String> handleAccountAlreadyExistsException(AccountAlreadyExistsException ex) {        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);    }    @ExceptionHandler(EmailAlreadyExistsException.class)    public ResponseEntity<String> handleEmailAlreadyExists(EmailAlreadyExistsException ex) {        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());    }    @ExceptionHandler(AccountLockedException.class) // Thêm xử lý cho AccountLockedException    public ResponseEntity<String> handleAccountLockedException(AccountLockedException ex) {        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage()); // Trả về mã 403    }    @ExceptionHandler(LoginException.class)    public ResponseEntity<String> handleLoginException(LoginException ex) {        return new ResponseEntity<>(ex.getMessage(), HttpStatus.UNAUTHORIZED); // Trả về mã 401    }    @ExceptionHandler(Exception.class)    public ResponseEntity<String> handleGenericException(Exception ex) {        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Đã xảy ra lỗi hệ thống: " + ex.getMessage());    }}