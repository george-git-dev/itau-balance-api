package itau_balance_api.adapters.input.web;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("updated_at")
    private OffsetDateTime updatedAt;
}
