package ru.cryptopro.support.spring.example.config.pki;

import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import ru.CryptoPro.JCP.KeyStore.JCPPrivateKeyEntry;
import ru.CryptoPro.JCP.params.JCPProtectionParameter;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

@Log4j2
@Configuration
public class UserKeyContainerConfig {

    @Bean
    public X509Certificate getCertificate(StoreConfig storeConfig, KeyStore keyStore) {
        String alias = storeConfig.getAlias();
        X509Certificate certificate;
        try {
            certificate = (X509Certificate) keyStore.getCertificate(alias);
            log.info("User certificate loaded from KeyStore");
        } catch (KeyStoreException e) {
            throw new RuntimeException(e);
        }
        return certificate;
    }

    @Bean
    @SneakyThrows
    @Profile("use_get_entry_for_jcsp_and_hsm")
    public PrivateKey getKeyFromEntry(StoreConfig storeConfig, KeyStore keyStore) {
        JCPProtectionParameter parameter = new JCPProtectionParameter(storeConfig.getPin().toCharArray());
        PrivateKey privateKey = ( //getEntry нужен в случае работы jcsp и hsm
                (JCPPrivateKeyEntry) keyStore.getEntry(storeConfig.getAlias(), parameter)
        ).getPrivateKey();
        log.info("PrivateKey loaded from alias: {}", storeConfig.getAlias());
        return privateKey;
    }

    @Bean
    @SneakyThrows
    @Profile("!use_get_entry_for_jcsp_and_hsm")
    public PrivateKey getKey(StoreConfig storeConfig, KeyStore keyStore, char[] pass) {
        return (PrivateKey) keyStore.getKey(storeConfig.getAlias(), pass);
    }
}
