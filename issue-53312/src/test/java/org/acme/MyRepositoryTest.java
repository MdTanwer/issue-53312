package org.acme;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;

@QuarkusTest
class MyRepositoryTest {

    @Inject
    MyRepository myRepository;

    @Test
    void testGetOne() {
        Assertions.assertEquals("1", myRepository.getOne());
    }
}
