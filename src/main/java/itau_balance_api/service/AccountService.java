package itau_balance_api.service;

import itau_balance_api.entity.Account;
import itau_balance_api.repository.AccountRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AccountService {
    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public void upsert(Account incomingAccount) {
        accountRepository.findById(incomingAccount.getId()).ifPresentOrElse(existingAccount -> {
            if (incomingAccount.getUpdatedAt().isAfter(existingAccount.getUpdatedAt())) {
                accountRepository.save(incomingAccount);
            }
        }, () -> accountRepository.save(incomingAccount));
    }

    public Optional<Account> findById(String id) {
        return accountRepository.findById(id);
    }
}
