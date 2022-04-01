package com.file.impl.csv; //@date 31.03.2022

import com.file.InputFactory;
import com.file.impl.csv.event.CSVCellEvent;
import com.file.impl.csv.event.CSVColumnEvent;
import com.file.impl.type.TypeParser;
import com.file.stream.LangEvent;
import com.file.stream.LangPipeline;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;

public class CSVParser extends TypeParser<CSVTable> implements CSVConstants {

    private final CSVTable table = new CSVTable();

    private final List<CSVCellEvent> rowCache = new LinkedList<>();

    public static CSVParser stream() {
        return new CSVParser();
    }

    public CSVParser() {
        configure(new CSVInputFactory());
    }

    public CSVCellEvent nextCell() throws IOException, ParseException {
        if (!hasNext()) {
            return null;
        }
        LangEvent event = pullEvent();
        if (event == null) return null;

        return event instanceof CSVCellEvent ? (CSVCellEvent) event : null;
    }

    public CSVColumnEvent nextColumn() throws IOException, ParseException {
        if (!hasNext()) {
            return null;
        }
        LangEvent event = pullEvent();
        if (event == null) return null;

        return event instanceof CSVColumnEvent ? (CSVColumnEvent) event : null;
    }

    public CSVCellEvent[] nextRow() throws IOException {
        return table.getRows().size() == 0 ? new CSVCellEvent[0]
                : (CSVCellEvent[]) table.getRows().get(table.getRows().size() - 1).values;
    }

    private LangEvent pullEvent() throws IOException, ParseException {
        if (!hasNext()) {
            return null;
        }

        LangEvent event = nextEvent();
        switch (event.getType()) {
            case CSVConstants.CSV_COLUMN_EVENT:
                table.getColumns().add(((CSVColumnEvent) event).getColumnName());
                return event;

            case CSVConstants.CSV_CELL_EVENT:
                CSVCellEvent cellEvent = (CSVCellEvent) event;
                rowCache.add(cellEvent);
                if (rowCache.size() == table.getColumns().size()) {
                    CSVTable.CSVRow row = new CSVTable.CSVRow();
                    row.values = rowCache.stream().map(CSVCellEvent::getContent).toArray();
                    table.getRows().add(row);
                    rowCache.clear();
                }
                return cellEvent;

            case CSVConstants.CSV_EOF_EVENT:
                if (rowCache.size() + 1 == table.getColumns().size()) {
                    rowCache.add(((CSVCellEvent)event));
                    CSVTable.CSVRow row = new CSVTable.CSVRow();
                    row.values = rowCache.stream().map(CSVCellEvent::getContent).toArray();
                    table.getRows().add(row);
                    rowCache.clear();
                }
                return event;
        }
        return null;
    }

    public CSVTable toObject() throws IOException, ParseException {
        if (table.getColumns().size() == 0) {
            while (hasNext()) {
                pullEvent();
            }
        }
        return table;
    }

    @Override
    public CSVParser setSource(File source) throws IOException {
        super.setSource(source);
        return this;
    }

    @Override
    public CSVParser setSource(String file) throws IOException {
        super.setSource(file);
        return this;
    }

    @Override
    public CSVParser setSource(Reader reader) {
        if (factory == null) throw new NullPointerException("factory is null");

        scanner             = factory.createFileScanner(reader);
        fragmentFileScanner = factory.createFragmentFileScanner(scanner);

        peekedEvent = allocator.allocate(fragmentFileScanner);
        return this;
    }

    @Override
    public CSVParser configure(InputFactory factory) {
        this.factory = factory;
        if (factory != null) {
            allocator = factory.getEventAllocator();
        }
        return this;
    }
}
