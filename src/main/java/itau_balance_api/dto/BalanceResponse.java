package itau_balance_api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BalanceResponse {

    private String id;
    private String owner;
    private BalanceDTO balance;
    private OffsetDateTime updated_at;
}
