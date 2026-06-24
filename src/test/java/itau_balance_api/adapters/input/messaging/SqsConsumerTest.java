package itau_balance_api.adapters.input.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import itau_balance_api.adapters.input.web.BalanceDTO;
import itau_balance_api.application.ports.input.AccountService;
import itau_balance_api.domain.models.Account;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SqsConsumerTest {

    @Mock
    private AccountService accountService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private AccountMapper accountMapper;

    @InjectMocks
    private SqsConsumer sqsConsumer;

    private String validMessage;
    private TransactionMessage transactionMessage;
    private Account account;

    @BeforeEach
    void setUp() throws Exception {
        validMessage = "{\"transaction\":{\"id\":\"txn-123\",\"type\":\"DEPOSIT\",\"amount\":100.0,\"currency\":\"BRL\",\"status\":\"COMPLETED\",\"timestamp\":1234567890123},\"account\":{\"id\":\"account-123\",\"owner\":\"John Doe\",\"created_at\":\"2024-01-01T00:00:00Z\",\"status\":\"ACTIVE\",\"balance\":{\"amount\":1000.50,\"currency\":\"BRL\"}}}";

        transactionMessage = new TransactionMessage();
        TransactionDTO transaction = new TransactionDTO();
        transaction.setId("txn-123");
        transaction.setType("DEPOSIT");
        transaction.setAmount(100.0);
        transaction.setCurrency("BRL");
        transaction.setStatus("COMPLETED");
        transaction.setTimestamp(1234567890123L);

        AccountDTO accountDTO = new AccountDTO();
        accountDTO.setId("account-123");
        accountDTO.setOwner("John Doe");
        accountDTO.setCreated_at("2024-01-01T00:00:00Z");
        accountDTO.setStatus("ACTIVE");

        BalanceDTO balance = new BalanceDTO();
        balance.setAmount(new BigDecimal("1000.50"));
        balance.setCurrency("BRL");
        accountDTO.setBalance(balance);

        transactionMessage.setTransaction(transaction);
        transactionMessage.setAccount(accountDTO);

        account = new Account();
        account.setId("account-123");
        account.setOwner("John Doe");
        account.setBalance(new BigDecimal("1000.50"));
        account.setCurrency("BRL");
    }

    @Test
    void consume_ShouldProcessMessageSuccessfully_WhenValidMessageProvided() throws Exception {
        // Arrange
        when(objectMapper.readValue(anyString(), eq(TransactionMessage.class))).thenReturn(transactionMessage);
        when(accountMapper.fromMessage(transactionMessage)).thenReturn(account);

        // Act
        sqsConsumer.consume(validMessage);

        // Assert
        verify(objectMapper, times(1)).readValue(eq(validMessage), eq(TransactionMessage.class));
        verify(accountMapper, times(1)).fromMessage(eq(transactionMessage));
        verify(accountService, times(1)).upsert(eq(account));
    }

    @Test
    void consume_ShouldThrowRuntimeException_WhenJsonParsingFails() throws Exception {
        // Arrange
        when(objectMapper.readValue(anyString(), eq(TransactionMessage.class)))
                .thenThrow(new RuntimeException("JSON parsing error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> sqsConsumer.consume(validMessage));
        verify(accountService, never()).upsert(any());
    }

    @Test
    void consume_ShouldThrowRuntimeException_WhenMappingFails() throws Exception {
        // Arrange
        when(objectMapper.readValue(anyString(), eq(TransactionMessage.class))).thenReturn(transactionMessage);
        when(accountMapper.fromMessage(transactionMessage))
                .thenThrow(new RuntimeException("Mapping error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> sqsConsumer.consume(validMessage));
        verify(accountService, never()).upsert(any());
    }

    @Test
    void consume_ShouldThrowRuntimeException_WhenServiceUpsertFails() throws Exception {
        // Arrange
        when(objectMapper.readValue(anyString(), eq(TransactionMessage.class))).thenReturn(transactionMessage);
        when(accountMapper.fromMessage(transactionMessage)).thenReturn(account);
        doThrow(new RuntimeException("Database error")).when(accountService).upsert(any());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> sqsConsumer.consume(validMessage));
    }
}
