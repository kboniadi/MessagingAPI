package utils;

import java.io.*;
import java.util.Objects;

public class BufferWrapper {
    private final BufferedWriter writer;
    private final BufferedReader reader;

    private BufferWrapper(Builder builder) {
        this.writer = builder.writer;
        this.reader = builder.reader;
    }
    public void writeLine(String source) {
        Objects.requireNonNull(writer, "must build with { Writer } to use -> void writeLine(String source)");
        try {
            writer.write(source);
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String readLine() throws IOException {
        Objects.requireNonNull(reader, "must build with { Reader } to use -> String readLine()");
        return reader.readLine();
    }

    public String read() throws IOException {
        Objects.requireNonNull(reader, "must build with { Reader } to use -> String read()");
        String response = "";
        int len;
        char[] b = new char[2048];
        len = reader.read(b);
        response = new String(b, 0, len);
        return response;
    }


    public static class Builder {
        private BufferedWriter writer;
        private BufferedReader reader;

        public Builder() {
            // empty
        }

        public Builder withWriter(Writer writer) {
            this.writer = (BufferedWriter) writer;
            return this;
        }

        public Builder withReader(Reader reader) {
            this.reader = (BufferedReader) reader;
            return this;
        }

        public BufferWrapper build() {
            return new BufferWrapper(this);
        }
    }
}
