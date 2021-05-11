package io.github.API.utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Objects;

public class IOWrapper {
    private final DataOutputStream writer;
    private final DataInputStream reader;

    private IOWrapper(Builder builder) {
        this.writer = builder.writer;
        this.reader = builder.reader;
    }
    public void writeLine(String source) {
        Objects.requireNonNull(writer, "must build with { Writer } to use -> void writeLine(String source)");
        try {
            writer.writeUTF(source);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String readLine() throws IOException {
        Objects.requireNonNull(reader, "must build with { Reader } to use -> String readLine()");
        return reader.readUTF();
    }

    public String read() throws IOException {
        Objects.requireNonNull(reader, "must build with { Reader } to use -> String read()");
        String response = "";
        int len;
        byte[] b = new byte[2048];
        len = reader.read(b);
        response = new String(b, 0, len);
        return response;
    }

    public void close() throws IOException {
        this.writer.close();
        this.reader.close();
    }

    public static class Builder {
        private DataOutputStream writer;
        private DataInputStream reader;

        public Builder() {
            // empty
        }

        public Builder withWriter(DataOutputStream writer) {
            this.writer = writer;
            return this;
        }

        public Builder withReader(DataInputStream reader) {
            this.reader = reader;
            return this;
        }

        public IOWrapper build() {
            return new IOWrapper(this);
        }
    }
}
