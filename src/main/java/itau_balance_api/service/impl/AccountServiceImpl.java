package itau_balance_api.service.impl;

import itau_balance_api.entity.Account;
import itau_balance_api.repository.AccountRepository;
import itau_balance_api.service.AccountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
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
                log.debug("Updating account: {}", incomingAccount.getId());
                accountRepository.save(incomingAccount);
            }
            log.debug("Creating new account: {}", incomingAccount.getId());
        }, () -> accountRepository.save(incomingAccount));
    }

    @Override
    public Optional<Account> findById(String id) {
        log.info("Finding accountById: {}", id);
        return accountRepository.findById(id);
    }
}
