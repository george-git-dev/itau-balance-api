package itau_balance_api.mapper.impl;

import itau_balance_api.dto.TransactionMessage;
import itau_balance_api.entity.Account;
import itau_balance_api.mapper.AccountMapper;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class AccountMapperImpl implements AccountMapper {

    @Override
    public Account fromMessage(TransactionMessage payload) {

        var accountData = payload.getAccount();
        var transaction = payload.getTransaction();

        Account account = new Account();
        account.setId(accountData.getId());
        account.setOwner(accountData.getOwner());
        account.setBalance(accountData.getBalance().getAmount());
        account.setCurrency(accountData.getBalance().getCurrency());

        account.setUpdatedAt(Instant.ofEpochMilli(transaction.getTimestamp() / 1000));

        return account;
    }
}
