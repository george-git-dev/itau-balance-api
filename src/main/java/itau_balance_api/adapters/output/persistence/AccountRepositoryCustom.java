package itau_balance_api.adapters.output.persistence;

import itau_balance_api.domain.models.Account;

public interface AccountRepositoryCustom {
    void upsert(Account account);
}
