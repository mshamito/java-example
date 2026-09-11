package ru.cryptopro.support.spring.example.config.tls;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import ru.CryptoPro.JCP.JCP;
import ru.cryptopro.support.spring.example.model.TrustedCertificateHolder;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.UUID;

@Log4j2
@Configuration
@RequiredArgsConstructor
@AutoConfigureAfter(TrustedCertsConfig.class)
public class TrustManagerConfig {
    private final TrustedCertificateHolder certs;

    @Bean
    @SneakyThrows
    @Profile("!disable_certificate_verification_in_tls")
    public TrustManager[] getGostTrustManager() {
        KeyStore keyStore = KeyStore.getInstance(JCP.CERT_STORE_NAME);
        keyStore.load(null, null);
        for (X509Certificate cert : certs.getTrustedCerts()) {
            keyStore.setCertificateEntry(UUID.randomUUID().toString(), cert);
        }

        log.info("TLS trusted store size: {}", keyStore.size());

        TrustManagerFactory factory = TrustManagerFactory.getInstance("GostX509");
        factory.init(keyStore);
        return factory.getTrustManagers();
    }

    @Bean
    @Profile("disable_certificate_verification_in_tls")
    @SneakyThrows
    public TrustManager[] getTrustManagerTrustAny() {
        return new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[]{};
                    }

                    @Override
                    public void checkClientTrusted(X509Certificate[] arg0, String arg1)
                            throws CertificateException {
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] arg0, String arg1)
                            throws CertificateException {
                    }
                }
        };
    }
}
