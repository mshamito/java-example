package ru.cryptopro.support.spring.example.utils.io;

import lombok.extern.log4j.Log4j2;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

@Log4j2
public class FileStreamWrapper implements StreamWrapper {
    private final File file;

    {
        try {
            file = File.createTempFile("temp", ".bin");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private final static int BUFFER_SIZE = 2 * 1024 * 1024;

    @Override
    public void writeTo(OutputStream outputStream) throws IOException {
        try (InputStream inputStream = getInputStream()) {
            int read;
            byte[] buffer = new byte[BUFFER_SIZE];
            while ((read = inputStream.read(buffer)) != -1)
                outputStream.write(buffer, 0, read);
        }
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        validateFile();
        log.info("temporary file was opened for writing: {}", file.getAbsolutePath());
        return Files.newOutputStream(file.toPath());
    }

    @Override
    public InputStream getInputStream() throws IOException {
        validateFile();
        log.info("temporary file was opened for reading, will be destroyed after reading: {}", file.getAbsolutePath());
        return Files.newInputStream(file.toPath(), StandardOpenOption.DELETE_ON_CLOSE);
    }

    private void validateFile() {
        if (!file.exists())
            log.error("temporary file not exists: {}", file.getAbsolutePath());
        else if (!file.canWrite())
            log.error("incorrect file permission: {}", file.getAbsolutePath());
    }
}
