package itau_balance_api.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import itau_balance_api.dto.TransactionMessage;
import itau_balance_api.entity.Account;
import itau_balance_api.service.AccountService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

import java.time.Instant;
import java.util.List;

@Component
public class SqsConsumer {

    private final AccountService service;
    private final ObjectMapper mapper;
    private final SqsClient sqsClient;
    private final String queueUrl = "http://localhost:4566/000000000000/transacoes-financeiras-processadas";

    public SqsConsumer(SqsClient sqsClient, AccountService service, ObjectMapper mapper) {
        this.sqsClient = sqsClient;
        this.service = service;
        this.mapper = mapper;
    }

    @Scheduled(fixedDelay = 3000)
    public void consume() {

        ReceiveMessageRequest request = ReceiveMessageRequest.builder()
                .queueUrl(queueUrl)
                .maxNumberOfMessages(10)
                .build();

        List<Message> messages = sqsClient.receiveMessage(request).messages();

        if (!messages.isEmpty()) {
            System.out.println("Received messages: " + messages.size());
        }

        for (Message message : messages) {

            TransactionMessage payload;
            try {
                payload = mapper.readValue(message.body(), TransactionMessage.class);

                var accountData = payload.getAccount();
                var transaction = payload.getTransaction();

                Account account = new Account();
                account.setId(accountData.getId());
                account.setOwner(accountData.getOwner());
                account.setBalance(accountData.getBalance().getAmount());
                account.setCurrency(accountData.getBalance().getCurrency());

                account.setUpdatedAt(Instant.ofEpochMilli(transaction.getTimestamp() / 1000));

                service.upsert(account);

                System.out.println("Processed account: " + account.getId());

                sqsClient.deleteMessage(builder -> builder
                        .queueUrl(queueUrl)
                        .receiptHandle(message.receiptHandle()));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
