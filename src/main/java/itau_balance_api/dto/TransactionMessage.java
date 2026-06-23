package itau_balance_api.dto;

import lombok.Data;

@Data
public class TransactionMessage {

    private TransactionDTO transaction;
    private AccountDTO account;
}
