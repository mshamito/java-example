package ru.cryptopro.support.spring.example.service;

import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.cryptopro.support.spring.example.config.tls.SSLContextConfig;
import ru.cryptopro.support.spring.example.config.tls.TlsConfig;
import ru.cryptopro.support.spring.example.exception.CryptographicException;
import ru.cryptopro.support.spring.example.model.ParsedCertificateInfo;
import ru.cryptopro.support.spring.example.model.tls.ClientTlsRequest;
import ru.cryptopro.support.spring.example.model.tls.ClientTlsResult;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;
import java.net.URI;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Log4j2
@Service
public class ClientTlsService {
    private final SSLContextConfig contextConfig;
    private final TlsConfig tlsConfig;
    private final SSLContext sslContext;
    private final SSLContext mutualSSLContext;

    public ClientTlsService(
            SSLContextConfig contextConfig,
            TlsConfig tlsConfig,
            @Qualifier("TLS")
            SSLContext sslContext,
            @Qualifier("mTLS")
            SSLContext mutualSSLContext
    ) {
        this.contextConfig = contextConfig;
        this.tlsConfig = tlsConfig;
        this.sslContext = sslContext;
        this.mutualSSLContext = mutualSSLContext;
    }

    @SneakyThrows
    public ClientTlsResult connect(ClientTlsRequest clientTlsDto) {
        return connectOkHttp(clientTlsDto);
    }

    @SneakyThrows
    public ClientTlsResult connectOkHttp(ClientTlsRequest clientTlsRequest) {
        URL url = URI.create(clientTlsRequest.getUrl()).toURL();

        ConnectionSpec spec = new ConnectionSpec.Builder(
                ConnectionSpec.MODERN_TLS)
                .tlsVersions(tlsConfig.getProtocols())
                .cipherSuites(tlsConfig.getCiphersSuites())
                .build();

        SSLContext context = clientTlsRequest.isMTLS() ?
                mutualSSLContext :
                sslContext;
        SSLSocketFactory socketFactory = context.getSocketFactory();

        OkHttpClient client = new OkHttpClient.Builder()
                .sslSocketFactory(socketFactory, (X509TrustManager) contextConfig.getTrustmanager())
                .connectionSpecs(Collections.singletonList(spec))
                .connectionPool(new ConnectionPool(100, 60_000, TimeUnit.MILLISECONDS))
                .connectTimeout(10_000L, TimeUnit.MILLISECONDS)
                .writeTimeout(15_000L, TimeUnit.MILLISECONDS)
                .readTimeout(30_000L, TimeUnit.MILLISECONDS)
                .build();

        Request request = new Request.Builder().url(url).build();

        ClientTlsResult result = new ClientTlsResult();
        try (Response response = client.newCall(request)
                .execute()
        ) {
            result.setHttp(response.protocol().name());
            result.setCode(response.code());
            result.setStatus(response.code() + " " + response.message());
            result.setHeaders(response.headers().toMultimap());
            Handshake handshake = response.handshake();
            if (Objects.nonNull(handshake)) {
                result.setCipherSuites(handshake.cipherSuite().javaName());
                result.setProtocol(handshake.tlsVersion().javaName());
                result.setCerts(
                        handshake.peerCertificates().stream()
                                .map(X509Certificate.class::cast)
                                .map(ParsedCertificateInfo::new)
                                .collect(Collectors.toList())
                );
            }
        } catch (
                Exception e) {
            throw new CryptographicException(e);
        }
        return result;
    }
}
