package ru.cryptopro.support.spring.example.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CertificateVerifyResult {
    private boolean trusted = false;
    private boolean revoked = true;
    private List<ParsedCertificateInfo> certs;
}