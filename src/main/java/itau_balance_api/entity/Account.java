package itau_balance_api.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Document(collection = "accounts")
public class Account {

    @Id
    private String id;
    private String owner;
    private Double balance;
    private String currency;
    private Instant updatedAt;

}
