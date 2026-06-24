package itau_balance_api.adapters.input.web.handler;

import itau_balance_api.adapters.input.web.ErrorResponse;
import itau_balance_api.domain.exception.AccountNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @Test
    void handleAccountNotFound_ShouldReturnErrorResponseWithNotFoundStatus_WhenAccountNotFoundExceptionThrown() {
        // Arrange
        AccountNotFoundException exception = new AccountNotFoundException("account-123");

        // Act
        ErrorResponse response = globalExceptionHandler.handleAccountNotFound(exception);

        // Assert
        assertNotNull(response);
        assertEquals("Account not found: account-123", response.getMessage());
        assertEquals(404, response.getStatus());
    }

    @Test
    void handleAccountNotFound_ShouldReturnErrorResponseWithCorrectMessage_WhenExceptionThrown() {
        // Arrange
        String accountId = "xyz";
        AccountNotFoundException exception = new AccountNotFoundException(accountId);

        // Act
        ErrorResponse response = globalExceptionHandler.handleAccountNotFound(exception);

        // Assert
        assertEquals("Account not found: xyz", response.getMessage());
    }

    @Test
    void handleGenericException_ShouldReturnErrorResponseWithInternalServerErrorStatus_WhenGenericExceptionThrown() {
        // Arrange
        Exception exception = new Exception("Something went wrong");

        // Act
        ErrorResponse response = globalExceptionHandler.handleGenericException(exception);

        // Assert
        assertNotNull(response);
        assertEquals("Something went wrong", response.getMessage());
        assertEquals(500, response.getStatus());
    }

    @Test
    void handleGenericException_ShouldReturnErrorResponseWithCorrectMessage_WhenExceptionThrown() {
        // Arrange
        String errorMessage = "Database connection failed";
        Exception exception = new Exception(errorMessage);

        // Act
        ErrorResponse response = globalExceptionHandler.handleGenericException(exception);

        // Assert
        assertEquals(errorMessage, response.getMessage());
    }

    @Test
    void handleGenericException_ShouldHandleNullExceptionMessage_WhenExceptionThrownWithoutMessage() {
        // Arrange
        Exception exception = new Exception((String) null);

        // Act
        ErrorResponse response = globalExceptionHandler.handleGenericException(exception);

        // Assert
        assertNull(response.getMessage());
        assertEquals(500, response.getStatus());
    }
}
