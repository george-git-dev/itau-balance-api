package itau_balance_api.domain.models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Document(collection = "accounts")
public class Account {

    @Id
    private String id;
    private String owner;
    private BigDecimal balance;
    private String currency;
    private Instant updatedAt;

}
