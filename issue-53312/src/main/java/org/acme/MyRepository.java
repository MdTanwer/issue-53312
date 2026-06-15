package org.acme;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;

@ApplicationScoped
public class MyRepository {

    private final EntityManager em;


    public MyRepository(EntityManager em) {
        this.em = em;
    }

    public String getOne() {
        return em.createNativeQuery("select 1").getSingleResult().toString();
    }
}
