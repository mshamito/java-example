package ru.cryptopro.support.spring.example.utils.gost;

import java.util.HashMap;
import java.util.Map;

public class GostProvidersNames {
    private static final Map<String,String> map;
    static {
        map = new HashMap<>();
        map.put("ru.CryptoPro.JCSP.JCSP", "JCSP");
        map.put("ru.CryptoPro.JCP.JCP", "JCP");
        map.put("ru.CryptoPro.Crypto.CryptoProvider", "Crypto");
        map.put("ru.CryptoPro.sspiSSL.SSPISSL", "JTLS");
        map.put("ru.CryptoPro.ssl.Provider", "JTLS");
        map.put("ru.CryptoPro.reprov.RevCheck", "RevCheck");
    }

    public static String mapNames(String longName) {
        if (!map.containsKey(longName))
            throw new RuntimeException("provider name not recognized");
        return map.get(longName);
    }
}
