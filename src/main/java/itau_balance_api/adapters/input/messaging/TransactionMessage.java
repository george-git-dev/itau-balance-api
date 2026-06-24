package itau_balance_api.adapters.input.messaging;

import lombok.Data;

@Data
public class TransactionMessage {

    private TransactionDTO transaction;
    private AccountDTO account;
}
