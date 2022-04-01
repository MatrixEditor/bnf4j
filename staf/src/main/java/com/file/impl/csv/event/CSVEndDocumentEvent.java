package com.file.impl.csv.event; //@date 31.03.2022

import com.file.impl.csv.CSVConstants;

public class CSVEndDocumentEvent extends CSVCellEvent implements CSVConstants {

    @Override
    public int getType() {
        return CSV_EOF_EVENT;
    }

    public boolean eof() {
        return true;
    }
}
