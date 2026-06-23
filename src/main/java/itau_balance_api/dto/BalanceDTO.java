package itau_balance_api.dto;

import lombok.Data;

@Data
public class BalanceDTO {

    private Double amount;
    private String currency;

}
