package com.file; //@date 30.03.2022

import java.io.IOException;
import java.io.Reader;

public class FileEntity {

    public static final int DEFAULT_BUFFER_SIZE = 8192;

    public int bufferSize = DEFAULT_BUFFER_SIZE;

    public char[] buf = null;

    public int position;
    public int column;

    public int lastCount;
    public int count;

    public int fileOffset = 0;

    private Reader reader;

    public FileEntity() {
        buf = new char[DEFAULT_BUFFER_SIZE];
    }

    public synchronized int doRead(int offset, int len) throws IOException {
        if (getReader() != null) {
            return getReader().read(buf, offset, len);
        }
        throw new IOException("no reader specified");
    }

    public Reader getReader() {
        return reader;
    }

    public void setReader(Reader reader) {
        this.reader = reader;
    }

}
