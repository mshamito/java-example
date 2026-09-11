package ru.cryptopro.support.spring.example.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.security.cert.X509Certificate;
import java.util.Set;

@RequiredArgsConstructor
@Getter
public class TrustedCertificateHolder {
    private final Set<X509Certificate> trustedCerts;
}
