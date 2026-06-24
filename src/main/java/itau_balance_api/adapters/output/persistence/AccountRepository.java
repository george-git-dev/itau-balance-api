package itau_balance_api.adapters.output.persistence;

import itau_balance_api.domain.models.Account;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AccountRepository extends MongoRepository<Account, String>, AccountRepositoryCustom {
}
