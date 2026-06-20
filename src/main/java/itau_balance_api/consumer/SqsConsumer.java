package itau_balance_api.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import itau_balance_api.service.AccountService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SqsConsumer {

    private final AccountService service;
    private final ObjectMapper mapper;

    public SqsConsumer(AccountService service, ObjectMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @Scheduled(fixedDelay = 5000)
    public void consume() {
        System.out.println("Consumer running...");
    }

}
