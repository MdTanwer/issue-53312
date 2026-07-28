package org.acme;

import java.util.List;

import org.hibernate.StatelessSession;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class GiftService {

    @Inject
    StatelessSession statelessSession;

    @Transactional
    public Gift createGift(String giftDescription) {
        Gift gift = new Gift();
        gift.setName(giftDescription == null ? "" : giftDescription.trim());
        statelessSession.insert(gift);
        return gift;
    }

    @Transactional
    public List<Gift> findAll() {
        return statelessSession
                .createSelectionQuery("from Gift order by id", Gift.class)
                .getResultList();
    }

    @Transactional
    public boolean deleteGift(Long id) {
        Gift gift = statelessSession.get(Gift.class, id);
        if (gift == null) {
            return false;
        }

        statelessSession.delete(gift);
        return true;
    }
}
