package itau_balance_api.repository.impl;

import itau_balance_api.entity.Account;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountRepositoryCustomImplTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private AccountRepositoryCustomImpl accountRepositoryCustom;

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
    void upsert_ShouldExecuteUpdateFirstAndUpsert_WhenAccountProvided() {
        // Act
        accountRepositoryCustom.upsert(testAccount);

        // Assert
        verify(mongoTemplate, times(1)).updateFirst(any(Query.class), any(Update.class), eq(Account.class));
        verify(mongoTemplate, times(1)).upsert(any(Query.class), any(Update.class), eq(Account.class));
    }

    @Test
    void upsert_ShouldUseCorrectAccountIdInQueries() {
        // Act
        accountRepositoryCustom.upsert(testAccount);

        // Assert
        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate, times(1)).updateFirst(queryCaptor.capture(), any(Update.class), eq(Account.class));
        verify(mongoTemplate, times(1)).upsert(queryCaptor.capture(), any(Update.class), eq(Account.class));
        
        Query firstQuery = queryCaptor.getAllValues().get(0);
        Query secondQuery = queryCaptor.getAllValues().get(1);
        
        assertTrue(firstQuery.toString().contains("account-123"));
        assertTrue(secondQuery.toString().contains("account-123"));
    }

    @Test
    void upsert_ShouldNotThrowException_WhenMongoOperationsSucceed() {
        // Act & Assert
        assertDoesNotThrow(() -> accountRepositoryCustom.upsert(testAccount));
    }
}
