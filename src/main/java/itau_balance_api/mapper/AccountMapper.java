package itau_balance_api.mapper;

import itau_balance_api.dto.TransactionMessage;
import itau_balance_api.entity.Account;

public interface AccountMapper {

    Account fromMessage(TransactionMessage message);
}
