package com.file.impl.csv; //@date 30.03.2022

import com.file.FileScanner;
import com.file.FragmentDriver;
import com.file.FragmentFileScanner;
import com.file.impl.csv.event.CSVColumnEvent;
import com.file.spec.CharSetSpec;

import java.io.EOFException;
import java.io.IOException;
import java.text.ParseException;

public class CSVFragmentedScanner extends FragmentFileScanner {

    private static final int DEFAULT_CSV_DELIMITER    = 0x3B;
    private static final int SCANNER_STATE_TERMINATED = 35;
    private static final int SCANNER_STATE_HEADER     = 33;
    private static final int SCANNER_STATE_PROLOG     = 32;
    private static final int SCANNER_STATE_CELL       = 34;

    private char[] columnDelimiter;

    private FragmentDriver headerDriver;
    private FragmentDriver cellDriver;

    private int column = 0;
    private int row    = 1;

    public CSVFragmentedScanner(FileScanner scanner) {
        super(scanner);
        setUpScanner();
    }

    @Override
    public void endFile() throws IOException {
        setState(SCANNER_STATE_TERMINATED);
    }

    private void setUpScanner() {
        setState(SCANNER_STATE_HEADER);
        setColumnDelimiter((char) DEFAULT_CSV_DELIMITER);

        headerDriver = new TableHeaderDriver();
        cellDriver   = new TableCellDriver();
        setDriver(headerDriver);

        getScanner().setCharSet(new CSVCharSet());
    }

    public char[] getColumnDelimiter() {
        return columnDelimiter;
    }

    public void setColumnDelimiter(char columnDelimiter) {
        this.columnDelimiter = new char[]{columnDelimiter};
    }

    public String getDelimiterAsString() {
        return String.valueOf(columnDelimiter);
    }

    public int getColumn() {
        return column;
    }

    public int getRow() {
        return row;
    }

    @Override
    public boolean hasNext() throws IOException {
        return getState() != SCANNER_STATE_TERMINATED;
    }

    private class TableHeaderDriver implements FragmentDriver {

        @Override
        public int next() throws IOException, ParseException {
            try {
                do {
                    switch (getState()) {
                        case SCANNER_STATE_PROLOG: {
                            getScanner().skipDeclSpaces();
                            setState(SCANNER_STATE_HEADER);
                            break;
                        }

                        case SCANNER_STATE_HEADER: {
                            if (getScanner().skip("\n")) {
                                setDriver(cellDriver);
                                setState(SCANNER_STATE_CELL);
                                return CSVFragmentedScanner.this.next();
                            }
                            // we assume that there are no spaces left, so we can start
                            // parsing the single column. If there is a delimiter char
                            // left we should skip that.
                            getScanner().skip(getColumnDelimiter());
                            getScanner().scanData(getDelimiterAsString(), getBuffer(true), true);
                            setEventType(CSVColumnEvent.CSV_COLUMN_EVENT);
                            return getEventType();
                        }
                    }
                } while (getState() == SCANNER_STATE_PROLOG || getState() == SCANNER_STATE_HEADER);
                return getEventType();
            } catch (EOFException e) {
                //EOF only throw while loading content
                setState(SCANNER_STATE_TERMINATED);
            }

            return CSVConstants.CSV_EOF_EVENT;

        }
    }

    private class TableCellDriver implements FragmentDriver {

        @Override
        public int next() throws IOException, ParseException {
            try {
                if (getScanner().skip("\n")) {
                    column = 0;
                    row++;
                } else column++;

                if (getScanner().skip(getColumnDelimiter())) {
                    column++;
                }

                getScanner().scanData(getDelimiterAsString(), getBuffer(true), true);
                setEventType(CSVConstants.CSV_CELL_EVENT);
                return getEventType();
            } catch (EOFException e) {
                setState(SCANNER_STATE_TERMINATED);
            }
            setEventType(CSVConstants.CSV_EOF_EVENT);
            return CSVConstants.CSV_EOF_EVENT;
        }
    }
}
