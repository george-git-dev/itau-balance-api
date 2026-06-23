package itau_balance_api.repository.impl;

import itau_balance_api.entity.Account;
import itau_balance_api.repository.AccountRepositoryCustom;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
public class AccountRepositoryCustomImpl implements AccountRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    public AccountRepositoryCustomImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void upsert(Account incomingAccount) {

        Query updateQuery = new Query(
                Criteria.where("_id").is(incomingAccount.getId())
                        .and("updatedAt").lt(incomingAccount.getUpdatedAt()));

        Update update = new Update()
                .set("balance", incomingAccount.getBalance())
                .set("currency", incomingAccount.getCurrency())
                .set("updatedAt", incomingAccount.getUpdatedAt());

        Query insertQuery = new Query(
                Criteria.where("_id").is(incomingAccount.getId()));

        Update insertUpdate = new Update()
                .setOnInsert("_id", incomingAccount.getId())
                .setOnInsert("owner", incomingAccount.getOwner())
                .setOnInsert("balance", incomingAccount.getBalance())
                .setOnInsert("currency", incomingAccount.getCurrency())
                .setOnInsert("updatedAt", incomingAccount.getUpdatedAt());

        mongoTemplate.updateFirst(updateQuery, update, Account.class);
        mongoTemplate.upsert(insertQuery, insertUpdate, Account.class);

        log.debug("Upsert executed for account: {}", incomingAccount.getId());
    }
}
