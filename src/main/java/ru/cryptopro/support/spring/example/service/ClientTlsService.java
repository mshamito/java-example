package ru.cryptopro.support.spring.example.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.cryptopro.support.spring.example.exception.CryptographicException;
import ru.cryptopro.support.spring.example.model.ParsedCertificateInfo;
import ru.cryptopro.support.spring.example.model.tls.ClientTlsRequest;
import ru.cryptopro.support.spring.example.model.tls.ClientTlsResult;

import java.net.URI;
import java.security.cert.X509Certificate;
import java.util.Objects;
import java.util.stream.Collectors;

@Log4j2
@Service
@RequiredArgsConstructor
public class ClientTlsService {

    private final OkHttpClient tlsOkHttpClient;
    private final OkHttpClient mutualTlsOkHttpClient;

    public ClientTlsResult connect(ClientTlsRequest clientTlsDto) {
        return connectOkHttp(clientTlsDto);
    }

    @SneakyThrows
    public ClientTlsResult connectOkHttp(ClientTlsRequest clientTlsRequest) {
        OkHttpClient client = clientTlsRequest.isMTLS()
                ? mutualTlsOkHttpClient
                : tlsOkHttpClient;

        Request request = new Request.Builder()
                .url(URI.create(clientTlsRequest.getUrl()).toURL())
                .build();

        ClientTlsResult result = new ClientTlsResult();

        try (Response response = client.newCall(request).execute()) {
            result.setHttp(response.protocol().name());
            result.setCode(response.code());
            result.setStatus(response.code() + " " + response.message());
            result.setHeaders(response.headers().toMultimap());

            var handshake = response.handshake();

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
        } catch (Exception e) {
            throw new CryptographicException(e);
        }

        return result;
    }
}