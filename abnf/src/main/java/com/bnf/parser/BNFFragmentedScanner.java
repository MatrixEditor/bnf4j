package com.bnf.parser; //@date 01.04.2022

import com.bnf.BNFCharSpec;
import com.bnf.BNFElement;
import com.bnf.parser.event.BNFEvent;
import com.file.FileScanner;
import com.file.FragmentDriver;
import com.file.FragmentFileScanner;

import java.io.EOFException;
import java.io.IOException;
import java.text.ParseException;

public class BNFFragmentedScanner extends FragmentFileScanner implements BNFCharSpec {

    public static final int SCANNER_STATE_TERMINATED        = 1;
    public static final int SCANNER_STATE_PROLOG            = 2;
    public static final int SCANNER_STATE_STRICT_GROUP      = 3;
    public static final int SCANNER_STATE_OCCURRENCE        = 4;
    public static final int SCANNER_STATE_OPTIONAL_GROUP    = 5;

    private final FragmentDriver prologDriver = new PrologDriver();

    private int min, max;

    private boolean add;
    private boolean closed;

    private int groupType;

    public BNFFragmentedScanner(FileScanner scanner) {
        super(scanner);
        getScanner().setCharSet(this);
        setState(SCANNER_STATE_PROLOG);
        setDriver(prologDriver);
    }

    public BNFElement<?> getElement() {
        return null;
    }

    @Override
    public void endFile() throws IOException {
        setState(SCANNER_STATE_TERMINATED);
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    public int getGroupType() {
        return groupType;
    }

    /**
     * As defined in <a href="https://datatracker.ietf.org/doc/html/rfc2234">
     * RFC2234</a> a {@code repeat} element:
     * <pre>
     * [12] repeat = 1*DIGIT / (*DIGIT "*" *DIGIT)
     * </pre>
     * How the parsing is done will be described with comments in the method
     * code below.<p>
     * <strong>Note:</strong> If the '*' is the character at the current
     * position no more steps are done forwards. This implementation only
     * supports single character numbers.
     */
    private boolean scanOccurrence() throws IOException {
        char current = getScanner().peekChar();

        int l, h;
        if (current == OCCURRENCE_INDICATOR) {
            l = -1;
            // guaranteed skip
            getScanner().skipChar(current);
        } else if (Character.isDigit(current)) {
            l = Character.getNumericValue(current);
        } else {
            return false;
        }

        getScanner().skipChar(OCCURRENCE_INDICATOR);
        char c = getScanner().peekChar();
        if (Character.isDigit(c)) {
            h   = Character.getNumericValue(c);
            min = l;
            max = h;

            getScanner().skipChar(c);
        }
        // only '*'
        else {
            min = 0;
            max = -1;
        }
        return true;
    }

    public boolean isAddable() {
        return add;
    }

    @Override
    public boolean hasNext() throws IOException {
        return getState() != SCANNER_STATE_TERMINATED;
    }

    public boolean isGroupClosed() {
        return closed;
    }

    protected class PrologDriver implements FragmentDriver {

        @Override
        public int next() throws IOException, ParseException {
            try {
                do {
                    // if the rule starts with a ' ', this should be skipped
                    add = getScanner().skipDeclSpaces();

                    // TextElement: can start with '"' or '%'
                    char current = getScanner().peekChar();
                    if (Character.isDigit(current)) {
                        if (!scanOccurrence()) {
                            throw new IOException();
                        }
                        return eventType = BNFEvent.OCCURRENCE_EVENT;
                    }

                    switch (current) {
                        case TEXT_QUOTE:
                            // TExtElement: the element is textOnly
                            getScanner().skipChar(TEXT_QUOTE);
                            getScanner().scanData(asString(TEXT_QUOTE), getBuffer(true), false);
                            return eventType = BNFEvent.TEXT_EVENT;

                        case TEXT_QUOTE_2:
                            getScanner().skipChar(TEXT_QUOTE_2);
                            getScanner().scanData(asString(TEXT_QUOTE_2), getBuffer(true), false);
                            return eventType = BNFEvent.TEXT_EVENT;

                        case OCCURRENCE_INDICATOR:
                            if (!scanOccurrence()) {
                                throw new IOException();
                            }
                            return eventType = BNFEvent.OCCURRENCE_EVENT;

                        case GROUP_OPENING:
                            getScanner().skipChar(GROUP_OPENING);
                            closed = false;
                            groupType = SCANNER_STATE_STRICT_GROUP;
                            return eventType = BNFEvent.GROUP_EVENT;

                        case GROUP_CLOSING:
                            getScanner().skipChar(GROUP_CLOSING);
                            closed = true;
                            groupType = SCANNER_STATE_STRICT_GROUP;
                            return eventType = BNFEvent.GROUP_EVENT;

                        case OR_DELIMITER:
                            getScanner().skipChar(OR_DELIMITER);
                            return eventType = BNFEvent.OR_EVENT;

                        case TEXT_NUM:
                            scanText(SPACE, BNFEvent.TEXT_EVENT);
                            return getEventType();

                        case COMMENT:
                            scanText(LINE_FEED, BNFEvent.COMMENT_EVENT);
                            return getEventType();

                        case OPTIONAL_OPENING:
                            getScanner().skipChar(OPTIONAL_OPENING);
                            closed = false;
                            groupType = SCANNER_STATE_OPTIONAL_GROUP;
                            return eventType = BNFEvent.GROUP_EVENT;

                        case OPTIONAL_CLOSING:
                            getScanner().skipChar(OPTIONAL_CLOSING);
                            closed = true;
                            groupType = SCANNER_STATE_OPTIONAL_GROUP;
                            return eventType = BNFEvent.GROUP_EVENT;

                    }

                    if (Character.isAlphabetic(current)) {
                        scanText(SPACE, BNFEvent.REF_EVENT);
                        return getEventType();
                    }

                } while (getState() == SCANNER_STATE_PROLOG);
            } catch (EOFException e) {
                setState(SCANNER_STATE_TERMINATED);
                return getEventType();
            }

            return -1; //error
        }
    }

    private void scanText(char delim, int type) throws IOException {
        scanText(delim, type, true);
    }

    private void scanText(char delim, int type, boolean stop) throws IOException {
        setEventType(type);
        getScanner().scanData(asString(delim), getBuffer(true), stop);
        getScanner().getEntity().position--;
    }
}
