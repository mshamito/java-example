package ru.cryptopro.support.spring.example.config.pki;

import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.cryptopro.support.spring.example.utils.io.EncodingHelper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.cert.CRLException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;
import java.util.*;

@Configuration
@ConfigurationProperties(prefix = "app.cp.crl")
@Data
@Log4j2
public class CrlConfig {
    private final CertificateFactory factory = CertificateFactory.getInstance("X509");

    private boolean crlFolderRequired;
    private String crlFolder;

    public CrlConfig() throws CertificateException {
    }

    private boolean isFolderExist() {
        return new File(crlFolder).exists();
    }

    private Optional<X509CRL> generateCrl(String filename) {
        File file = new File(crlFolder, filename);
        try (InputStream stream = Files.newInputStream(file.toPath())) {
            log.info("reading crl: {}", filename);
            X509CRL crl = (X509CRL) factory.generateCRL(
                    EncodingHelper.decodeDerOrB64Stream(stream)
            );
            if (crl.getNextUpdate().compareTo(new Date()) >= 0) {
                return Optional.of(crl);
            } else {
                log.warn("skipping expired crl: {}", filename);
                return Optional.empty();
            }
        } catch (CRLException | IOException e) {
            log.error(e);
            return Optional.empty();
        }
    }

    @Bean
    public Set<X509CRL> getLocalCRLs() {
        if (!isFolderExist())
            return Collections.emptySet();
        File[] files = new File(crlFolder).listFiles((dir, name) -> name.toLowerCase().endsWith(".crl"));
        if (files == null || files.length == 0)
            return Collections.emptySet();
        List<X509CRL> result = new ArrayList<>();
        for (File walk : files) {
            Optional<X509CRL> crl = generateCrl(walk.getName());
            crl.ifPresent(result::add);
        }
        return new HashSet<>(result);
    }
}