package ru.cryptopro.support.spring.example.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.cryptopro.support.spring.example.model.tls.ClientTlsRequest;
import ru.cryptopro.support.spring.example.model.tls.ClientTlsResult;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ClientTlsServiceTest {
    @Autowired
    ClientTlsService service;

    @Test
    void connect() {
        ClientTlsResult result = service.connect(
                ClientTlsRequest.builder()
                        .mTLS(false)
                        .url("https://cryptopro.ru")
                        .build()
        );
        assertNotNull(result);
        assertEquals(200, result.getCode());
    }
}