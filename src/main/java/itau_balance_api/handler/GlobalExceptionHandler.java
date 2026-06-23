package itau_balance_api.handler;

import itau_balance_api.dto.ErrorResponse;
import itau_balance_api.exception.AccountNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public  ErrorResponse handleGenericException(Exception ex) {
        log.error("Generic exception: {}", ex.getMessage());
        return ErrorResponse.builder()
                .message(ex.getMessage())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .build();
    }


    @ExceptionHandler(AccountNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleAccountNotFound(AccountNotFoundException ex) {
        log.error("Account not found: {}", ex.getMessage());
        return ErrorResponse.builder()
                .message(ex.getMessage())
                .status(HttpStatus.NOT_FOUND.value())
                .build();
    }
}
