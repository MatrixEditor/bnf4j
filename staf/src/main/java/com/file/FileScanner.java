package com.file; //@date 30.03.2022

import com.file.spec.CharSetSpec;

import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;

public class FileScanner {

    public static final int DEFAULT_SCANNER_BUFFER_SIZE = 64;

    protected FileEntity entity = new FileEntity();
    protected FragmentFileScanner scanner;

    protected CharSetSpec charSet = null;

    public FileScanner(Reader reader) {
        entity.setReader(reader);
    }

    protected final boolean load(int offset) throws IOException {
        // refreshing the raw offset position
        entity.fileOffset = entity.fileOffset + entity.lastCount;

        // At first, we have to specify the amount of data to read. This length
        // is most likely to be > DEFAULT_SCANNER_BUFFER_SIZE, so it will be
        // the default value (64 bytes).
        int len = entity.buf.length - offset;
        if (len > DEFAULT_SCANNER_BUFFER_SIZE) {
            // this check has to be done because of the capacity being too big
            len = DEFAULT_SCANNER_BUFFER_SIZE;
        }
        boolean changed = false;

        // The reading is delegated by this object and returning the actual number
        // of characters read.
        int count = entity.doRead(offset, len);
        if (count != -1) {
            if (count != 0) {
                entity.position = offset;
                entity.count = count + offset;
                entity.lastCount = count;
            }
        }
        // EOF: to ensure every resource opened is being closed before the program
        // ends the FileEntity#close method is called.
        else {
            entity.position = offset;
            entity.count = offset;
            changed = true;
            //need to close the reader first since the program can end abruptly
            entity.getReader().close();
            if (scanner != null) {
                scanner.endFile();
            }
            throw new EOFException();
        }
        return changed;
    }

    protected final void loadIfPossible(int offset) throws IOException {
        if (entity.position == entity.count) {
            load(offset);
        }
    }

    public boolean scanData(String delimiter, StringBuffer buffer, boolean stopOnNewLine) throws IOException {
        return scanData(delimiter, buffer, 0, stopOnNewLine);
    }

    public boolean scanData(String delimiter, StringBuffer buffer, int chunkLimit, boolean stopOnNewLine) throws IOException {
        boolean done = false;

        // collect some information about the delimiter
        int len = delimiter.length();
        char c0 = delimiter.charAt(0);

        do {
            loadIfPossible(0);

            boolean changed = false;
            while ((entity.position > entity.count - len) && (!changed)) {
                // INFO:
                System.arraycopy(entity.buf, entity.position, entity.buf, 0,
                                 entity.count - entity.position);

                changed = load(entity.count - entity.position);
                entity.position = 0;
            }

            int offset = entity.position;
            char c = entity.buf[offset];
            // iterating over the given buffer while looking for the given delimiter
            DELIM_CHECK: while (entity.position < entity.count) {
                c = entity.buf[entity.position++];
                if (c == c0) {
                    int delimOffset = entity.position - 1;
                    for (int i = 1; i < len; i++) {
                        if (entity.position == entity.count) {
                            entity.position--;
                            break DELIM_CHECK;
                        }
                        else if (charSet.isNewLine(c)) {
                            if (stopOnNewLine) {
                                entity.position--;
                                break DELIM_CHECK;
                            }
                        }
                        c = entity.buf[entity.position++];
                        if (delimiter.charAt(i) != c) {
                            entity.position--;
                            break;
                        }
                    }
                    if (entity.position == delimOffset + len) {
                      // We found the delimiter string in the given text
                        done = true;
                        break;
                    }
                }
                else if (charSet.isNewLine(c)) {
                    if (stopOnNewLine) {
                        entity.position--;
                        done = true;
                        break;
                    }
                }
            }

            int length = entity.position - offset;
            if (done) {
                length -= delimiter.length();
            }
            buffer.append(entity.buf, offset, length);
            if (chunkLimit > 0 && buffer.length() >= chunkLimit) {
                break;
            }
        } while (!done && chunkLimit == 0);
        return !done;
    }

    public boolean skip(String s) throws IOException {
        return skip(s.toCharArray());
    }

    public boolean skip(char[] s) throws IOException {
        final int len = s.length;
        if (arrangeBuffer(len)) {
            int before = entity.position;
            for (char c : s) {
                if (!(entity.buf[before++] == c)) {
                    return false;
                }
            }

            entity.position = entity.position + len;
            entity.column += len;
            return true;
        }
        return false;
    }

    public char peekChar() throws IOException {
        loadIfPossible(0);
        return entity.buf[entity.position];
    }

    public boolean skipChar(char c) throws IOException {
        loadIfPossible(0);

        char x = entity.buf[entity.position];
        if (x == c) {
            entity.position++;
            if (charSet.isNewLine(c)) {
                entity.column = 1;
            } else entity.column++;
            return true;
        }
        else if (charSet.isNewLine(c)) {
            loadIfPossible(0);
            entity.position++;
            if (charSet.isNewLine(entity.buf[entity.position])) {
                entity.position++;
            }
            entity.column = 1;
            return true;
        }
        return false;
    }

    public boolean arrangeBuffer(int length) throws IOException {
        if ((entity.count - entity.position) >= length) {
            return true;
        }

        boolean changed = false;
        while ((entity.count - entity.position) < length) {
            if ((entity.buf.length - entity.position) < length) {
                System.arraycopy(entity.buf, entity.position, entity.buf, 0,
                                 entity.count - entity.position);
                entity.count = entity.count - entity.position;
                entity.position = 0;
            }

            if ((entity.count- entity.position) < length) {
                int p = entity.position;
                changed = load(entity.count);
                entity.position = p;
                if (changed) break;
            }
        }
        return (entity.count - entity.position) >= length;
    }

    public boolean skipDeclSpaces() throws IOException {
        loadIfPossible(0);

        char c = entity.buf[entity.position];
        boolean changed = false;
        if (charSet.isSpace(c)) {
            do {
                if (charSet.isNewLine(c)) {
                    entity.column = 1;

                    if (entity.position == entity.count - 1) {
                        entity.buf[0] = c;
                        if (!(changed = load(1))) {
                            entity.position = 0;
                        }
                    }
                } else entity.column++;

                if (!changed) {
                    entity.position++;
                }
                loadIfPossible(0);
            } while (charSet.isSpace(c = entity.buf[entity.position]));
            return true;
        }
        return false;
    }

    public void setCharSet(CharSetSpec charSet) {
        this.charSet = charSet;
    }

    public CharSetSpec getCharSet() {
        return charSet;
    }

    public FileEntity getEntity() {
        return entity;
    }

    public void setDelegatedScanner(FragmentFileScanner scanner) {
        this.scanner = scanner;
    }
}
