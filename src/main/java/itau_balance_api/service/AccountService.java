package itau_balance_api.service;

import itau_balance_api.entity.Account;

import java.util.Optional;

public interface AccountService {
    void upsert(Account incomingAccount);

    Optional<Account> findById(String id);
}
