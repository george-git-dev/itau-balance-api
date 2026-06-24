package itau_balance_api.adapters.input.messaging;

import itau_balance_api.adapters.input.web.BalanceDTO;
import lombok.Data;

@Data
public class AccountDTO {

    private String id;
    private String owner;
    private String created_at;
    private String status;
    private BalanceDTO balance;
}