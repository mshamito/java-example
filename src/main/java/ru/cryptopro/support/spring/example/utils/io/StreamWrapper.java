package ru.cryptopro.support.spring.example.utils.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface StreamWrapper {
    void writeTo(OutputStream outputStream) throws IOException;
    OutputStream getOutputStream() throws IOException;
    InputStream getInputStream() throws IOException;
}
