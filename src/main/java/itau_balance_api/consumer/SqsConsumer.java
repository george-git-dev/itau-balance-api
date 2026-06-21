package itau_balance_api.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.sqs.annotation.SqsListener;
import itau_balance_api.dto.TransactionMessage;
import itau_balance_api.entity.Account;
import itau_balance_api.mapper.AccountMapper;
import itau_balance_api.service.impl.AccountServiceImpl;
import org.springframework.stereotype.Component;

@Component
public class SqsConsumer {

    private final AccountServiceImpl service;
    private final AccountMapper accountMapper;
    private final ObjectMapper objectMapper;

    public SqsConsumer(AccountServiceImpl service, ObjectMapper objectMapper, AccountMapper accountMapper) {
        this.service = service;
        this.objectMapper = objectMapper;
        this.accountMapper = accountMapper;
    }

    @SqsListener(value = "${aws.sqs.queue-name}")
    public void consume(String message) {
        try {

            TransactionMessage payload = objectMapper.readValue(message, TransactionMessage.class);

            Account account = accountMapper.fromMessage(payload);

            service.upsert(account);

            System.out.println("Processed via listener: " + account.getId());

        } catch (Exception e) {
            System.err.println("Error processing message: " + message);
            e.printStackTrace();
        }
    }
}
