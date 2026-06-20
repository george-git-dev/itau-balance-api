package itau_balance_api.consumer;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

import java.util.List;

@Component
public class SqsConsumer {

    private final SqsClient sqsClient;
    private final String queueUrl = "http://localhost:4566/000000000000/transacoes-financeiras-processadas";

    public SqsConsumer(SqsClient sqsClient) {
        this.sqsClient = sqsClient;
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
            System.out.println(message.body());
        }

    }

}
