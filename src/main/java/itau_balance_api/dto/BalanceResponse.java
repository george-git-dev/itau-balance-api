package itau_balance_api.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class BalanceResponse {

    private String id;
    private String owner;
    private BalanceDTO balance;
    private Instant updatedAt;
}
