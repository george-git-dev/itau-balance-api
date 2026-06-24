package itau_balance_api.application.services;

import itau_balance_api.adapters.output.persistence.AccountRepository;
import itau_balance_api.application.ports.input.AccountService;
import itau_balance_api.domain.models.Account;
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
        accountRepository.upsert(incomingAccount);
    }

    @Override
    public Optional<Account> findById(String id) {
        log.info("Finding account by id: {}", id);
        return accountRepository.findById(id);
    }
}
