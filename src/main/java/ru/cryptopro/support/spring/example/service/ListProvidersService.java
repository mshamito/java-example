package ru.cryptopro.support.spring.example.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.security.Provider;
import java.security.Security;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;

@Service
@Log4j2
public class ListProvidersService {
    @EventListener(ApplicationReadyEvent.class)
    public void listProviders() {
        List<Provider> providers = Arrays.asList(Security.getProviders());
        StringJoiner joiner = new StringJoiner(System.lineSeparator());
        joiner.add("java security providers:");
        providers.forEach(
                walk -> joiner.add(String.format("#%d %s", providers.indexOf(walk) + 1, walk.getName()))
        );
        log.info(joiner.toString());
    }
}
