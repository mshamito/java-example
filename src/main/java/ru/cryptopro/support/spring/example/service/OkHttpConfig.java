package ru.cryptopro.support.spring.example.config;

import lombok.RequiredArgsConstructor;

import okhttp3.ConnectionPool;
import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.cryptopro.support.spring.example.config.tls.SSLContextConfig;
import ru.cryptopro.support.spring.example.config.tls.TlsConfig;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

@Configuration
@RequiredArgsConstructor
public class OkHttpConfig {

    private final SSLContextConfig contextConfig;
    private final TlsConfig tlsConfig;

    private ConnectionSpec connectionSpec() {
        return new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                .tlsVersions(tlsConfig.getProtocols())
                .cipherSuites(tlsConfig.getCiphersSuites())
                .build();
    }

    private OkHttpClient buildClient(SSLContext sslContext) throws Exception {
        SSLSocketFactory socketFactory = sslContext.getSocketFactory();

        return new OkHttpClient.Builder()
                .sslSocketFactory(
                        socketFactory,
                        (X509TrustManager) contextConfig.getTrustmanager()
                )
                .connectionSpecs(Collections.singletonList(connectionSpec()))
                .connectionPool(new ConnectionPool(50, 5, TimeUnit.MINUTES))
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();
    }

    @Bean
    public OkHttpClient tlsOkHttpClient(
            @Qualifier("TLS") SSLContext sslContext
    ) throws Exception {
        return buildClient(sslContext);
    }

    @Bean
    public OkHttpClient mutualTlsOkHttpClient(
            @Qualifier("mTLS") SSLContext sslContext
    ) throws Exception {
        return buildClient(sslContext);
    }
}