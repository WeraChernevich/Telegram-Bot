package ru.chernevich.configuration;

import org.springframework.beans.factory.annotation.Value;
import ru.chernevich.utils.CryptoTool;

public class RestServiceConfiguration {
    @Value("${salt}")
    private String salt;

    public CryptoTool getCryptoTool() {
        return new CryptoTool(salt);
    }
}
