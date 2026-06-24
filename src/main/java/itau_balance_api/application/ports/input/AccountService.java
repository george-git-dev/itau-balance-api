package itau_balance_api.application.ports.input;

import itau_balance_api.domain.models.Account;

import java.util.Optional;

public interface AccountService {
    void upsert(Account incomingAccount);

    Optional<Account> findById(String id);
}
