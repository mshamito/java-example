package ru.cryptopro.support.spring.example.config.tls;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.cp.tls")
@Getter
@Setter
public class TlsConfig {
    private String[] ciphersSuites;
    private String[] protocols;
}
