package com.file.impl.csv; //@date 31.03.2022

import com.file.spec.CharSetSpec;

public class CSVCharSet implements CharSetSpec {

    public static final char COMMA = 0x2C;

    @Override
    public boolean isNewLine(char cc) {
        return cc == 0x0A;
    }

    @Override
    public boolean isSpace(char cc) {
        return Character.isWhitespace(cc);
    }

    public boolean isComma(char c) {
        return c == COMMA;
    }

    public boolean isTextData(char c) {
        return inRange(c, 0x20, 0x21) || inRange(c, 0x23, 0x2B) || inRange(c, 0x2D, 0x7E);
    }

    private boolean inRange(char c, int start, int end) {
        return c >= start && c <= end;
    }
}
