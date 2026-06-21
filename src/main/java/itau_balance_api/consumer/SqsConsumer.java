package itau_balance_api.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.sqs.annotation.SqsListener;
import itau_balance_api.dto.TransactionMessage;
import itau_balance_api.entity.Account;
import itau_balance_api.service.AccountService;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class SqsConsumer {

    private final AccountService service;
    private final ObjectMapper mapper;

    public SqsConsumer(AccountService service, ObjectMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @SqsListener(value = "${aws.sqs.queue-name}")
    public void consume(String message) {
        try {

            TransactionMessage payload = mapper.readValue(message, TransactionMessage.class);

            var accountData = payload.getAccount();
            var transaction = payload.getTransaction();

            Account account = new Account();
            account.setId(accountData.getId());
            account.setOwner(accountData.getOwner());
            account.setBalance(accountData.getBalance().getAmount());
            account.setCurrency(accountData.getBalance().getCurrency());

            account.setUpdatedAt(Instant.ofEpochMilli(transaction.getTimestamp() / 1000));

            service.upsert(account);

            System.out.println("Processed via listener: " + account.getId());

        } catch (Exception e) {
            System.err.println("Error processing message: " + message);
            e.printStackTrace();
        }
    }
}
