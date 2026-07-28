package org.acme;

import java.util.List;

import org.hibernate.StatelessSession;
import org.acme.users.Gift;

import io.quarkus.hibernate.orm.PersistenceUnit;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class GiftService {

    @Inject
    @PersistenceUnit("users")
    StatelessSession usersStatelessSession;

    @Transactional
    public Gift createGift(String giftDescription) {
        Gift gift = new Gift();
        gift.setName(giftDescription == null ? "" : giftDescription.trim());
        usersStatelessSession.insert(gift);
        return gift;
    }

    @Transactional
    public List<Gift> findAll() {
        return usersStatelessSession
                .createSelectionQuery("from Gift order by id", Gift.class)
                .getResultList();
    }

    @Transactional
    public boolean deleteGift(Long id) {
        Gift gift = usersStatelessSession.get(Gift.class, id);
        if (gift == null) {
            return false;
        }

        usersStatelessSession.delete(gift);
        return true;
    }
}
