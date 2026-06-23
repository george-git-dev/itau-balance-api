package itau_balance_api.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.sqs.annotation.SqsListener;
import itau_balance_api.dto.TransactionMessage;
import itau_balance_api.entity.Account;
import itau_balance_api.mapper.AccountMapper;
import itau_balance_api.service.AccountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SqsConsumer {

    private final AccountService service;
    private final AccountMapper accountMapper;
    private final ObjectMapper objectMapper;

    public SqsConsumer(AccountService service, ObjectMapper objectMapper, AccountMapper accountMapper) {
        this.service = service;
        this.objectMapper = objectMapper;
        this.accountMapper = accountMapper;
    }

    @SqsListener(value = "${aws.sqs.queue-name}")
    public void consume(String message) {
        log.debug("Received message from SQS: {}", message);
        try {

            TransactionMessage payload = objectMapper.readValue(message, TransactionMessage.class);

            Account account = accountMapper.fromMessage(payload);

            service.upsert(account);

            log.debug("Processed via listener: {}", account.getId());

        } catch (Exception e) {
            log.error("Error processing message: {}", message, e);
            throw new RuntimeException(e);
        }
    }
}
