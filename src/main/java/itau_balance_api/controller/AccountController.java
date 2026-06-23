package itau_balance_api.controller;

import itau_balance_api.dto.BalanceDTO;
import itau_balance_api.dto.BalanceResponse;
import itau_balance_api.entity.Account;
import itau_balance_api.exception.AccountNotFoundException;
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

        Account account = service.findById(accountId).orElseThrow(() -> new AccountNotFoundException(accountId));

        BalanceDTO balanceDTO = BalanceDTO.builder()
                .amount(account.getBalance())
                .currency(account.getCurrency())
                .build();

        return BalanceResponse.builder()
                .id(account.getId())
                .owner(account.getOwner())
                .balance(balanceDTO)
                .updatedAt(OffsetDateTime.ofInstant(account.getUpdatedAt(), ZoneId.of("America/Sao_Paulo")))
                .build();
    }
}
