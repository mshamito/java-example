package ru.cryptopro.support.spring.example.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import ru.cryptopro.support.spring.example.utils.io.FileStreamWrapper;
import ru.cryptopro.support.spring.example.utils.io.MemoryStreamWrapper;
import ru.cryptopro.support.spring.example.utils.io.StreamWrapper;

@Configuration
@ConfigurationProperties(prefix = "app.cp.files")
@Data
public class StreamWrapperConfig {
    private boolean useTemporaryFiles;

    public StreamWrapper getStreamWrapperInstance() {
        return useTemporaryFiles ? new FileStreamWrapper() : new MemoryStreamWrapper();
    }
}
