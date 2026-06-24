package itau_balance_api.application.services;

import itau_balance_api.adapters.output.persistence.AccountRepository;
import itau_balance_api.domain.models.Account;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountServiceImpl accountService;

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
    void upsert_ShouldCallRepositoryUpsert_WhenAccountProvided() {
        // Act
        accountService.upsert(testAccount);

        // Assert
        verify(accountRepository, times(1)).upsert(eq(testAccount));
    }

    @Test
    void findById_ShouldReturnOptionalAccount_WhenAccountExists() {
        // Arrange
        String accountId = "account-123";
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(testAccount));

        // Act
        Optional<Account> result = accountService.findById(accountId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testAccount, result.get());
        verify(accountRepository, times(1)).findById(eq(accountId));
    }

    @Test
    void findById_ShouldReturnEmptyOptional_WhenAccountDoesNotExist() {
        // Arrange
        String accountId = "non-existent-account";
        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        // Act
        Optional<Account> result = accountService.findById(accountId);

        // Assert
        assertFalse(result.isPresent());
        verify(accountRepository, times(1)).findById(eq(accountId));
    }
}
