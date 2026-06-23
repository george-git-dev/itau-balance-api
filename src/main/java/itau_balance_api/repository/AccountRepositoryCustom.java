package itau_balance_api.repository;

import itau_balance_api.entity.Account;

public interface AccountRepositoryCustom {
    void upsert(Account account);
}
