package itau_balance_api.mapper.impl;

import itau_balance_api.dto.AccountDTO;
import itau_balance_api.dto.BalanceDTO;
import itau_balance_api.dto.TransactionDTO;
import itau_balance_api.dto.TransactionMessage;
import itau_balance_api.entity.Account;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class AccountMapperImplTest {

    private AccountMapperImpl accountMapper;

    @BeforeEach
    void setUp() {
        accountMapper = new AccountMapperImpl();
    }

    @Test
    void fromMessage_ShouldMapTransactionMessageToAccount_WhenValidMessage() {
        // Arrange
        TransactionMessage message = createValidTransactionMessage();

        // Act
        Account account = accountMapper.fromMessage(message);

        // Assert
        assertNotNull(account);
        assertEquals("account-123", account.getId());
        assertEquals("John Doe", account.getOwner());
        assertEquals(new BigDecimal("1000.50"), account.getBalance());
        assertEquals("BRL", account.getCurrency());
        // 1234567890123L microseconds = 1234567 seconds + 890123000 nanoseconds
        assertEquals(Instant.ofEpochSecond(1234567, 890123000), account.getUpdatedAt());
    }

    @Test
    void fromMessage_ShouldThrowIllegalArgumentException_WhenTimestampIsNull() {
        // Arrange
        TransactionMessage message = createValidTransactionMessage();
        message.getTransaction().setTimestamp(null);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> accountMapper.fromMessage(message)
        );
        assertTrue(exception.getMessage().contains("Invalid timestamp"));
    }

    @Test
    void fromMessage_ShouldThrowIllegalArgumentException_WhenTimestampIsZero() {
        // Arrange
        TransactionMessage message = createValidTransactionMessage();
        message.getTransaction().setTimestamp(0L);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> accountMapper.fromMessage(message)
        );
        assertTrue(exception.getMessage().contains("Invalid timestamp"));
    }

    @Test
    void fromMessage_ShouldThrowIllegalArgumentException_WhenTimestampIsNegative() {
        // Arrange
        TransactionMessage message = createValidTransactionMessage();
        message.getTransaction().setTimestamp(-1L);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> accountMapper.fromMessage(message)
        );
        assertTrue(exception.getMessage().contains("Invalid timestamp"));
    }

    @Test
    void fromMessage_ShouldConvertMicrosecondsToInstant_WhenTimestampIsInMicroseconds() {
        // Arrange
        TransactionMessage message = createValidTransactionMessage();
        message.getTransaction().setTimestamp(1234567890123456L); // microseconds

        // Act
        Account account = accountMapper.fromMessage(message);

        // Assert
        assertEquals(Instant.ofEpochSecond(1234567890, 123456000), account.getUpdatedAt());
    }

    private TransactionMessage createValidTransactionMessage() {
        TransactionMessage message = new TransactionMessage();
        
        TransactionDTO transaction = new TransactionDTO();
        transaction.setId("txn-123");
        transaction.setType("DEPOSIT");
        transaction.setAmount(100.0);
        transaction.setCurrency("BRL");
        transaction.setStatus("COMPLETED");
        transaction.setTimestamp(1234567890123L); // microseconds
        
        AccountDTO accountDTO = new AccountDTO();
        accountDTO.setId("account-123");
        accountDTO.setOwner("John Doe");
        accountDTO.setCreated_at("2024-01-01T00:00:00Z");
        accountDTO.setStatus("ACTIVE");
        
        BalanceDTO balance = new BalanceDTO();
        balance.setAmount(new BigDecimal("1000.50"));
        balance.setCurrency("BRL");
        accountDTO.setBalance(balance);
        
        message.setTransaction(transaction);
        message.setAccount(accountDTO);
        
        return message;
    }
}
