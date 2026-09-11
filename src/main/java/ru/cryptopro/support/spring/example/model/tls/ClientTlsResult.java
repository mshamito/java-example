package ru.cryptopro.support.spring.example.model.tls;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import ru.cryptopro.support.spring.example.model.ParsedCertificateInfo;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClientTlsResult {
    private int code;
    private String status;
    private Map<String, List<String>> headers;
    private String cipherSuites;
    private String http;
    private String protocol;
    private List<ParsedCertificateInfo> certs;
}
