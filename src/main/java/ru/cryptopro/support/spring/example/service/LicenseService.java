package ru.cryptopro.support.spring.example.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import ru.CryptoPro.JCP.tools.License;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.time.ZoneId;

@Service
@Log4j2
public class LicenseService {
    @EventListener(ApplicationReadyEvent.class)
    public void getLicense() {
        printLicense("JCP", "ru.CryptoPro.JCP.tools.License");
        printLicense("JCSP", "ru.CryptoPro.JCSP.JCSPLicense");
        printLicense("cpSSL", "ru.CryptoPro.ssl.ServerLicense");
    }

    private void printLicense(String providerName, String className) {
        try {
            Class<?> providerClass = Class.forName(className);
            Constructor<?> constructor = providerClass.getConstructor((Class<?>[]) null);
            constructor.setAccessible(true);
            License license = (License) constructor.newInstance((Object[]) null);
            log.info("{} {} {}, valid till {}",
                    providerName,
                    license.getDescriptionString(),
                    license.getProductID(),
                    Instant.ofEpochMilli(license.getEndDate()).atZone(ZoneId.systemDefault())

            );
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | InstantiationException |
                 IllegalAccessException e) {
            log.warn("failed to load {} license. e: {}", providerName, e);
        }
    }
}
