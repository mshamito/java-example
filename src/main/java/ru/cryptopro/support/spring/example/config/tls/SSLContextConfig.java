package ru.cryptopro.support.spring.example.config.tls;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

@Configuration
@Log4j2
@RequiredArgsConstructor
public class SSLContextConfig {
    private final TrustManager[] trustManagers;
    private final KeyManager[] keyManagers;

    @Bean("TLS")
    public SSLContext getSSLContext() {
        return getInstance(false);
    }

    @Bean("mTLS")
    public SSLContext getMutualSSLContext() {
        return getInstance(true);
    }

    @SneakyThrows
    public SSLContext getInstance(boolean mTLS) {
        SSLContext context;
        try {
            log.info("trying to load GOST TLS Provider (sspiSSL.jar) with GostTLSv1.3 support");
            context = SSLContext.getInstance("GostTLSv1.3");
            log.info("sspiSSL loaded");
        } catch (NoSuchAlgorithmException e) {
            log.warn("failed to load GostTLSv1.3 Provider.");
            log.warn("trying to load GOST TLS Provider (cpSSL.jar) with GostTLSv1.2 support");
            try {
                context = SSLContext.getInstance("GostTLSv1.2");
                log.info("cpSSL loaded");
            } catch (NoSuchAlgorithmException ex) {
                log.error("no GOST TLS Provider was found. Try to use cpSSL.jar / sspiSSL.jar");
                throw new NoSuchAlgorithmException(ex);
            }
        }
        context.init(mTLS ? keyManagers : null, trustManagers, null);
        return context;
    }

    public TrustManager getTrustmanager() {
        return trustManagers[0];
    }

    @PostConstruct
    @SneakyThrows
    public void init() {
        SSLContext context = getInstance(false);
        try (
                SSLSocket socket = (SSLSocket) getInstance(false)
                        .getSocketFactory()
                        .createSocket()
        ) {
            log.info("TLS: Provider = {}", context.getProvider());
            log.info("TLS: supported = {}", Arrays.toString(socket.getSupportedProtocols()));
            log.info("TLS: enabled = {}", Arrays.toString(socket.getEnabledProtocols()));
            log.info("TLS: cipher suites = {}", Arrays.toString(socket.getSupportedCipherSuites()));
        }
    }
}
