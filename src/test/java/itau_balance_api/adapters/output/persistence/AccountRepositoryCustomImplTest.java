package itau_balance_api.adapters.output.persistence;

import itau_balance_api.domain.models.Account;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

    @Test
    void upsert_ShouldIncludeUpdatedAtInUpdateQuery_ToPreventOldDataOverwrite() {
        // Act
        accountRepositoryCustom.upsert(testAccount);

        // Assert - verifica que a query do updateFirst contém o updatedAt (proteção contra dados antigos)
        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).updateFirst(queryCaptor.capture(), any(Update.class), eq(Account.class));

        String queryString = queryCaptor.getValue().toString();
        assertTrue(queryString.contains("updatedAt"),
                "updateFirst query deve filtrar por updatedAt para evitar sobrescrever dados mais recentes");
    }

    @Test
    void upsert_ShouldIncludeBalanceInUpdate_WhenAccountProvided() {
        // Act
        accountRepositoryCustom.upsert(testAccount);

        // Assert
        ArgumentCaptor<Update> updateCaptor = ArgumentCaptor.forClass(Update.class);
        verify(mongoTemplate).updateFirst(any(Query.class), updateCaptor.capture(), eq(Account.class));

        String updateString = updateCaptor.getValue().toString();
        assertTrue(updateString.contains("balance"), "Update deve conter o campo balance");
        assertTrue(updateString.contains("updatedAt"), "Update deve conter o campo updatedAt");
    }

    @Test
    void upsert_ShouldUseSetOnInsert_ForInsertQuery() {
        // Act
        accountRepositoryCustom.upsert(testAccount);

        // Assert
        ArgumentCaptor<Update> updateCaptor = ArgumentCaptor.forClass(Update.class);
        verify(mongoTemplate).upsert(any(Query.class), updateCaptor.capture(), eq(Account.class));

        String updateString = updateCaptor.getValue().toString();
        assertTrue(updateString.contains("$setOnInsert"), "Insert update deve usar setOnInsert para não sobrescrever documento existente");
    }

    @Test
    void upsert_ShouldCallUpdateFirstBeforeUpsert_ToEnsureCorrectOrder() {
        // Act
        accountRepositoryCustom.upsert(testAccount);

        // Assert
        InOrder inOrder = inOrder(mongoTemplate);
        inOrder.verify(mongoTemplate).updateFirst(any(Query.class), any(Update.class), eq(Account.class));
        inOrder.verify(mongoTemplate).upsert(any(Query.class), any(Update.class), eq(Account.class));
    }
}
