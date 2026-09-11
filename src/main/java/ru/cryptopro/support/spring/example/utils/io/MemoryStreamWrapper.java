package ru.cryptopro.support.spring.example.utils.io;

import java.io.*;

public class MemoryStreamWrapper implements StreamWrapper {
    private final ByteArrayOutputStream stream = new ByteArrayOutputStream();

    @Override
    public void writeTo(OutputStream outputStream) throws IOException {
        outputStream.write(stream.toByteArray());
    }

    @Override
    public OutputStream getOutputStream() {
        return stream;
    }

    @Override
    public InputStream getInputStream() {
        return new ByteArrayInputStream(stream.toByteArray());
    }
}
