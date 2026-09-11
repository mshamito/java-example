package ru.cryptopro.support.spring.example.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.cryptopro.support.spring.example.utils.io.EncodingHelper;

import java.io.ByteArrayInputStream;
import java.security.cert.X509Certificate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RawSignatureServiceTest {
    @Autowired
    RawSignatureService rawSignatureService;
    @Autowired
    X509Certificate certificate;

    private final byte[] data = "Test Content".getBytes();


    @Test
    void rawTest() {
        assertDoesNotThrow(() -> {
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            byte[] sign = rawSignatureService.sign(bais, false, false);
            bais.reset();
            assertNotEquals(0, sign.length);
            assertTrue(rawSignatureService.verify(bais, sign, certificate, false));
        });
    }

    @Test
    void rawInvertTest() {
        assertDoesNotThrow(() -> {
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            byte[] sign = rawSignatureService.sign(bais, false, true);
            bais.reset();
            assertNotEquals(0, sign.length);
            assertTrue(sign.length == 64 || sign.length == 128);
            assertTrue(rawSignatureService.verify(bais, sign, certificate, true));
        });
    }

    @Test
    void rawBase64Test() {
        assertDoesNotThrow(() -> {
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            byte[] signB64 = rawSignatureService.sign(bais, true, false);
            bais.reset();
            byte[] sign = EncodingHelper.decode(new String(signB64));
            assertNotEquals(0, sign.length);
            assertTrue(sign.length == 64 || sign.length == 128);
            assertTrue(rawSignatureService.verify(bais, sign, certificate, false));
        });
    }
}