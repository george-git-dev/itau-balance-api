package itau_balance_api.dto;

import lombok.Data;

@Data
public class AccountDTO {

    private String id;
    private String owner;
    private String created_at;
    private String status;
    private BalanceDTO balance;
}