package itau_balance_api.mapper.impl;

import itau_balance_api.dto.TransactionMessage;
import itau_balance_api.entity.Account;
import itau_balance_api.mapper.AccountMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component
public class AccountMapperImpl implements AccountMapper {

    @Override
    public Account fromMessage(TransactionMessage payload) {

        var accountData = payload.getAccount();
        var transaction = payload.getTransaction();

        Long rawTimestamp = transaction.getTimestamp();

        if (rawTimestamp == null || rawTimestamp <= 0) {
            throw new IllegalArgumentException("Invalid timestamp in transaction: " + transaction.getId());
        }

        long micros = rawTimestamp;

        Account account = new Account();
        account.setId(accountData.getId());
        account.setOwner(accountData.getOwner());
        account.setBalance(accountData.getBalance().getAmount());
        account.setCurrency(accountData.getBalance().getCurrency());
        account.setUpdatedAt(Instant.ofEpochSecond(
                micros / 1_000_000L,
                (micros % 1_000_000L) * 1000L
        ));

        return account;
    }
}
