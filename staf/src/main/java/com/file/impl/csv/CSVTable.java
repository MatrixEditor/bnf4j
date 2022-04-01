package com.file.impl.csv; //@date 31.03.2022

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.StringJoiner;

public final class CSVTable {

    public static final Object[] EMPTY_ELEMENT_DATA = {};

    private int columnCount;
    private int rowCount;

    private final List<CSVRow> rows = new LinkedList<>();
    private final List<String> columns = new LinkedList<>();

    public static final class CSVRow {
        public Object[] values = EMPTY_ELEMENT_DATA;
    }

    public int getColumnCount() {
        return columnCount = getColumns().size();
    }

    public int getRowCount() {
        return rowCount = getRows().size();
    }

    public List<CSVRow> getRows() {
        return rows;
    }

    public List<String> getColumns() {
        return columns;
    }

    @Override
    public String toString() {
        StringJoiner joiner= new StringJoiner(", ", CSVTable.class.getSimpleName() + "{\n", "\n}")
                .add("    " + Arrays.toString(columns.toArray()));
        getRows().forEach(x -> joiner.add("\n    " + Arrays.toString(x.values)));
        return joiner.toString();
    }
}
