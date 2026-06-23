package itau_balance_api.repository;

import itau_balance_api.entity.Account;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AccountRepository extends MongoRepository<Account, String>, AccountRepositoryCustom {
}
