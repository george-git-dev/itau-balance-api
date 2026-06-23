package itau_balance_api.controller;

import itau_balance_api.dto.BalanceDTO;
import itau_balance_api.dto.BalanceResponse;
import itau_balance_api.entity.Account;
import itau_balance_api.exception.AccountNotFoundException;
import itau_balance_api.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountControllerTest {

    @Mock
    private AccountService accountService;

    @InjectMocks
    private AccountController accountController;

    private Account testAccount;

    @BeforeEach
    void setUp() {
        testAccount = new Account();
        testAccount.setId("account-123");
        testAccount.setOwner("John Doe");
        testAccount.setBalance(new BigDecimal("1000.50"));
        testAccount.setCurrency("BRL");
        testAccount.setUpdatedAt(Instant.now());
    }

    @Test
    void getBalance_ShouldReturnBalanceResponse_WhenAccountExists() {
        // Arrange
        String accountId = "account-123";
        when(accountService.findById(accountId)).thenReturn(Optional.of(testAccount));

        // Act
        BalanceResponse response = accountController.getBalance(accountId);

        // Assert
        assertNotNull(response);
        assertEquals("account-123", response.getId());
        assertEquals("John Doe", response.getOwner());
        assertEquals(new BigDecimal("1000.50"), response.getBalance().getAmount());
        assertEquals("BRL", response.getBalance().getCurrency());
        assertNotNull(response.getUpdatedAt());
    }

    @Test
    void getBalance_ShouldConvertUpdatedAtToSaoPauloTimeZone_WhenAccountExists() {
        // Arrange
        String accountId = "account-123";
        Instant updatedAt = Instant.parse("2024-01-01T12:00:00Z");
        testAccount.setUpdatedAt(updatedAt);
        when(accountService.findById(accountId)).thenReturn(Optional.of(testAccount));

        // Act
        BalanceResponse response = accountController.getBalance(accountId);

        // Assert
        OffsetDateTime expected = OffsetDateTime.ofInstant(updatedAt, ZoneId.of("America/Sao_Paulo"));
        assertEquals(expected, response.getUpdatedAt());
    }

    @Test
    void getBalance_ShouldThrowAccountNotFoundException_WhenAccountDoesNotExist() {
        // Arrange
        String accountId = "non-existent-account";
        when(accountService.findById(accountId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(AccountNotFoundException.class, () -> accountController.getBalance(accountId));
    }

    @Test
    void getBalance_ShouldMapBalanceCorrectly_WhenAccountHasZeroBalance() {
        // Arrange
        String accountId = "account-123";
        testAccount.setBalance(BigDecimal.ZERO);
        when(accountService.findById(accountId)).thenReturn(Optional.of(testAccount));

        // Act
        BalanceResponse response = accountController.getBalance(accountId);

        // Assert
        assertEquals(BigDecimal.ZERO, response.getBalance().getAmount());
    }

    @Test
    void getBalance_ShouldMapBalanceCorrectly_WhenAccountHasNegativeBalance() {
        // Arrange
        String accountId = "account-123";
        testAccount.setBalance(new BigDecimal("-500.00"));
        when(accountService.findById(accountId)).thenReturn(Optional.of(testAccount));

        // Act
        BalanceResponse response = accountController.getBalance(accountId);

        // Assert
        assertEquals(new BigDecimal("-500.00"), response.getBalance().getAmount());
    }
}
