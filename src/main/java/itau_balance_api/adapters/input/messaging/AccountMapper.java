package itau_balance_api.adapters.input.messaging;

import itau_balance_api.domain.models.Account;

public interface AccountMapper {

    Account fromMessage(TransactionMessage message);
}
