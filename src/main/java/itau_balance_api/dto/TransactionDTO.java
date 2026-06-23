package itau_balance_api.dto;

import lombok.Data;

@Data
public class TransactionDTO {

    private String id;
    private String type;
    private Double amount;
    private String currency;
    private String status;
    private Long timestamp;

}
