package ru.cryptopro.support.spring.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.cryptopro.support.spring.example.model.ParsedCertificateInfo;
import ru.cryptopro.support.spring.example.model.CertificateVerifyResult;
import ru.cryptopro.support.spring.example.utils.certificate.CastX509Helper;

import java.security.cert.*;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Log4j2
@RequiredArgsConstructor
public class CertificateVerifyService {
    private final Set<X509Certificate> certificateSet;

    public CertificateVerifyResult validateCertificate(MultipartFile cert) {
        return validateCertificate(CastX509Helper.castCertificate(cert));
    }

    public CertificateVerifyResult validateCertificate(X509Certificate cert) {
        CertificateVerifyResult certificateVerifyResult = new CertificateVerifyResult();
        try {
            Set<TrustAnchor> trustAnchors = new HashSet<>();
            for (X509Certificate walk : certificateSet)
                trustAnchors.add(new TrustAnchor(walk, null));
            PKIXBuilderParameters parameters = new PKIXBuilderParameters(trustAnchors, null);
            parameters.setSigProvider(null);
            CollectionCertStoreParameters certStoreParameters = new CollectionCertStoreParameters(certificateSet);
            CertStore store = CertStore.getInstance("Collection", certStoreParameters);
            parameters.addCertStore(store);
            X509CertSelector selector = new X509CertSelector();
            selector.setCertificate(cert);
            parameters.setTargetCertConstraints(selector);
            parameters.setRevocationEnabled(false);
            PKIXCertPathBuilderResult result =
                    (PKIXCertPathBuilderResult) CertPathBuilder.getInstance("CPPKIX", "RevCheck")
                            .build(parameters);
            CertPath certPath = result.getCertPath();
            log.info("certificate chain built");
            certificateVerifyResult.setTrusted(true);
            certificateVerifyResult.setCerts(
                    Stream.concat(
                                    certPath.getCertificates().stream(),
                                    Stream.of(result.getTrustAnchor().getTrustedCert())
                            )
                            .map(X509Certificate.class::cast)
                            .map(ParsedCertificateInfo::new)
                            .collect(Collectors.toList())
            );

            CertPathValidator certPathValidator = CertPathValidator.getInstance("CPPKIX", "RevCheck");
            parameters.setRevocationEnabled(true);
            certPathValidator.validate(certPath, parameters);
            log.info("certificate chain validated");
            certificateVerifyResult.setRevoked(false);
            return certificateVerifyResult;
        } catch (Exception e) {
            log.error(e);
            return certificateVerifyResult;
        }
    }
}
