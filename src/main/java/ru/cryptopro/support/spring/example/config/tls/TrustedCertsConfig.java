package ru.cryptopro.support.spring.example.config.tls;

import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import ru.cryptopro.support.spring.example.model.TrustedCertificateHolder;
import ru.cryptopro.support.spring.example.utils.io.EncodingHelper;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

@Configuration
@Log4j2
public class TrustedCertsConfig {
    @SneakyThrows
    @Bean
    @Profile("load_certs_from_cacerts")
    public TrustedCertificateHolder getCertsFromCaCerts() {
        Set<X509Certificate> result = new HashSet<>();
        KeyStore caCerts = KeyStore.getInstance("JKS");
        String caCertsPath = System.getProperty("java.home") + "/lib/security/cacerts".replace("/", File.separator);
        caCerts.load(Files.newInputStream(Paths.get(caCertsPath)), "changeit".toCharArray());
        Enumeration<String> aliases = caCerts.aliases();
        while (aliases.hasMoreElements()) {
            String alias = aliases.nextElement();
            X509Certificate x509Certificate = (X509Certificate) caCerts.getCertificate(alias);
            result.add(x509Certificate);
            log.debug("loading cert {}", x509Certificate.getSubjectX500Principal());
        }
        log.info("{} certificates loaded from CaCerts", result.size());
        return new TrustedCertificateHolder(result);
    }

    @SneakyThrows
    @Bean
    @Profile("load_certs_from_resources")
    public TrustedCertificateHolder getCertsFromResources() {
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        Set<X509Certificate> result = new HashSet<>();
        ClassLoader classLoader = this.getClass().getClassLoader();
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(classLoader);
        Resource[] resources = resolver.getResources("classpath:/certs/*");
        if (resources.length == 0) {
            log.warn("no certs found in classpath:/certs/");
            return new TrustedCertificateHolder(result);
        }
        for (Resource resource : resources) {
            log.info("found {} in resource folder", resource.getFilename());
            try {
                X509Certificate certificate = (X509Certificate) factory.generateCertificate(EncodingHelper.decodeDerOrB64Stream(resource.getInputStream()));
                result.add(certificate);
                log.debug("loaded ca certificate from resources, {}", certificate.getSubjectX500Principal());
            } catch (Exception e) {
                log.error("failed to read file as certificate {}, exception: {}", resource.getFilename(), e.getMessage());
                log.error(e);
            }
        }
        log.info("{} certificates loaded from resource folder", result.size());
        return new TrustedCertificateHolder(result);
    }

    @SneakyThrows
    @Bean
    @Profile("load_certs_from_local_csp_stores")
    public TrustedCertificateHolder getCertsFromCSP() {
        Set<X509Certificate> result = new HashSet<>();
        KeyStore rootStore = KeyStore.getInstance("ROOT", "JCSP");
        rootStore.load(null, null);
        Enumeration<String> rootAliases = rootStore.aliases();
        while (rootAliases.hasMoreElements()) {
            String alias = rootAliases.nextElement();
            X509Certificate x509Certificate = (X509Certificate) rootStore.getCertificate(alias);
            result.add(x509Certificate);
            log.debug("loading cert {} from ROOT", x509Certificate.getSubjectX500Principal());
        }
        log.info("{} certificates loaded from ROOT store (CSP)", result.size());

        int countCA = 0;
        KeyStore caStore = KeyStore.getInstance("CA", "JCSP");
        caStore.load(null, null);
        Enumeration<String> caAliases = caStore.aliases();
        while (caAliases.hasMoreElements()) {
            countCA++;
            String alias = caAliases.nextElement();
            X509Certificate x509Certificate = (X509Certificate) caStore.getCertificate(alias);
            result.add(x509Certificate);
            log.debug("loading cert {} from CA", x509Certificate.getSubjectX500Principal());
        }

        log.info("{} certificates loaded from CA store (CSP)", countCA);
        return new TrustedCertificateHolder(result);
    }
}
