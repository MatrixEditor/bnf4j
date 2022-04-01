package com.file.impl.csv.event; //@date 30.03.2022

import com.file.impl.csv.CSVConstants;
import com.file.stream.LangEvent;

import java.util.StringJoiner;

public class CSVCellEvent implements LangEvent, CSVConstants {

    private int row;
    private int column;

    private String content;

    public CSVCellEvent() {
    }

    public CSVCellEvent(int row, int column) {
        setValues("", row, column);
    }

    public void setValues(String content, int r, int c) {
        this.content = content;
        row = r;
        column = c;
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    public String getContent() {
        return content;
    }

    @Override
    public String toString() {
        return "CSVCellEvent{row=" + row + ", column=" + column + ", content='" + content + "'}";
    }

    @Override
    public int getType() {
        return CSV_CELL_EVENT;
    }
}
