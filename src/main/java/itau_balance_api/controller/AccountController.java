package itau_balance_api.controller;

import itau_balance_api.entity.Account;
import itau_balance_api.service.AccountService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/balances")
public class AccountController {

    private final AccountService service;

    public AccountController(AccountService service) {
        this.service = service;
    }

    @GetMapping("/{accountId}")
    public Account getBalance(@PathVariable String accountId) {
        return service.findById(accountId).orElseThrow(() -> new RuntimeException("Account not found"));
    }
}
