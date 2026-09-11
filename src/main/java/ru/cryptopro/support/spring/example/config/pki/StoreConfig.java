package ru.cryptopro.support.spring.example.config.pki;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;
import ru.CryptoPro.JCP.KeyStore.StoreInputStream;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.security.KeyStore;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;

@Log4j2
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app.cp")
@Validated
public class StoreConfig {
    @NotBlank
    @Pattern(
            regexp = "^(JCP|JCSP)$",
            message = "must be either JCP or JCSP"
    )
    private String providerName;
    @NotBlank
    private String keyStoreName;
    @NotBlank
    private String alias;
    private String pin;

    @SneakyThrows
    @Bean
    public KeyStore getKeyStore() {
        validateStore();
        KeyStore keyStore = KeyStore.getInstance(keyStoreName, providerName);
        keyStore.load(new StoreInputStream(alias), null);
        return keyStore;
    }

    @Bean
    public char[] getPassword() {
        return pin.toCharArray();
    }

    @SneakyThrows
    private void validateStore() {
        log.info("KeyStore loaded: {}", keyStoreName);
        KeyStore fullKeyStore = KeyStore.getInstance(keyStoreName, providerName);
        fullKeyStore.load(null, null);
        List<String> aliases = Collections.list(fullKeyStore.aliases());
        if (aliases.isEmpty())
            throw new RuntimeException("empty KeyStore");
        StringJoiner joiner = new StringJoiner(System.lineSeparator());
        joiner.add(String.format("available aliases on %s :", keyStoreName));
        aliases.forEach(
                walk -> joiner.add(String.format("%d) %s", aliases.indexOf(walk) + 1, walk))
        );
        log.info(joiner.toString());
        log.info("aliases enumerated");
        log.info("checking configured alias...");
        if (!aliases.contains(alias)) {
            log.error("alias {} not found in {}", alias, keyStoreName);
            throw new IllegalArgumentException(String.format("configured alias %s not found", alias));
        }
        log.info("configured alias found. trying to load...");
    }
}
