package itau_balance_api.service.impl;

import itau_balance_api.entity.Account;
import itau_balance_api.repository.AccountRepository;
import itau_balance_api.service.AccountService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AccountServiceImpl implements AccountService {
    private final AccountRepository accountRepository;

    public AccountServiceImpl(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public void upsert(Account incomingAccount) {
        accountRepository.findById(incomingAccount.getId()).ifPresentOrElse(existingAccount -> {
            if (incomingAccount.getUpdatedAt().isAfter(existingAccount.getUpdatedAt())) {
                accountRepository.save(incomingAccount);
            }
        }, () -> accountRepository.save(incomingAccount));
    }

    @Override
    public Optional<Account> findById(String id) {
        return accountRepository.findById(id);
    }
}
