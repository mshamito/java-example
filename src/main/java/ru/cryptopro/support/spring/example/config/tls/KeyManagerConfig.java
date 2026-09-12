package ru.cryptopro.support.spring.example.config.tls;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.ssl.JavaTLSCertPathManagerParameters;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import java.security.KeyStore;
import java.security.cert.*;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

@Log4j2
@Configuration
@RequiredArgsConstructor
public class KeyManagerConfig {
    private final KeyStore keyStore;
    private final Set<X509Certificate> certs;
    private final char[] password;

    @SneakyThrows
    @Bean
    public KeyManager[] getKeyManagers() {
        KeyManagerFactory factory = KeyManagerFactory.getInstance("GostX509", "JTLS");
        boolean chainRequired = keyStore.getCertificateChain(null).length < 2;
        if (chainRequired) {
            log.warn("Certificate chain missing in container. Mutual TLS requires a chain. Falling back to local certificates");
            KeyStore trustStore = KeyStore.getInstance(JCP.CERT_STORE_NAME);
            trustStore.load(null, null);
            if (certs == null || certs.isEmpty())
                throw new IllegalArgumentException("Trusted certificates not provided");
            for (X509Certificate certificate : certs)
                trustStore.setCertificateEntry(UUID.randomUUID().toString(), certificate);
            PKIXBuilderParameters parameters = new PKIXBuilderParameters(trustStore, new X509CertSelector());
            parameters.setRevocationEnabled(true);
            parameters.setCertStores(Collections.singletonList(CertStore.getInstance("Collection", new CollectionCertStoreParameters(certs))));
            JavaTLSCertPathManagerParameters managerParameters = new JavaTLSCertPathManagerParameters(keyStore, password);
            managerParameters.setParameters(parameters);
            factory.init(managerParameters);
            return factory.getKeyManagers();
        }
        factory.init(keyStore, password);
        return factory.getKeyManagers();
    }
}
