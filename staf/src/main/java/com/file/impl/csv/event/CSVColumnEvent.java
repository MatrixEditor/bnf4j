package com.file.impl.csv.event; //@date 30.03.2022

import com.file.impl.csv.CSVConstants;
import com.file.stream.LangEvent;

public class CSVColumnEvent implements LangEvent, CSVConstants {

    private String columnName;

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    @Override
    public int getType() {
        return CSV_COLUMN_EVENT;
    }
}
