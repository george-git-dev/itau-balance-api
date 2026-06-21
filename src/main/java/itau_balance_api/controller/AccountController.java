package itau_balance_api.controller;

import itau_balance_api.dto.BalanceDTO;
import itau_balance_api.dto.BalanceResponse;
import itau_balance_api.entity.Account;
import itau_balance_api.service.AccountService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.time.ZoneId;

@RestController
@RequestMapping("/balances")
public class AccountController {

    private final AccountService service;

    public AccountController(AccountService service) {
        this.service = service;
    }

    @GetMapping("/{accountId}")
    public BalanceResponse getBalance(@PathVariable String accountId) {

        Account account = service.findById(accountId).orElseThrow(() -> new RuntimeException("Account not found"));

        BalanceDTO balanceDTO = new BalanceDTO();
        balanceDTO.setAmount(account.getBalance());
        balanceDTO.setCurrency(account.getCurrency());

        BalanceResponse response = new BalanceResponse();
        response.setId(account.getId());
        response.setOwner(account.getOwner());
        response.setBalance(balanceDTO);
        response.setUpdated_at(OffsetDateTime.ofInstant(account.getUpdatedAt(), ZoneId.of("America/Sao_Paulo")));


        return response;
    }
}
