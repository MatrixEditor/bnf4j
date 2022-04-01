package com.file.impl.csv.event; //@date 30.03.2022

import com.file.FragmentFileScanner;
import com.file.impl.csv.CSVConstants;
import com.file.impl.csv.CSVFragmentedScanner;
import com.file.stream.EventAllocator;
import com.file.stream.LangEvent;

public class CSVEventAllocator implements EventAllocator {

    @Override
    public LangEvent allocate(FragmentFileScanner fragmentFileScanner) {
        if (!(fragmentFileScanner instanceof CSVFragmentedScanner)) {
            throw new ClassFormatError("invalid fragment scanner class");
        }

        CSVFragmentedScanner scanner = (CSVFragmentedScanner) fragmentFileScanner;
        LangEvent            event   = null;

        switch (scanner.getEventType()) {
            case CSVConstants.CSV_CELL_EVENT:
                CSVCellEvent cellEvent = new CSVCellEvent();
                cellEvent.setValues(scanner.getTextElement(), scanner.getRow(), scanner.getColumn());
                event = cellEvent;
                break;

            case CSVConstants.CSV_COLUMN_EVENT:
                CSVColumnEvent columnEvent = new CSVColumnEvent();
                columnEvent.setColumnName(scanner.getTextElement());
                event = columnEvent;
                break;

            case CSVConstants.CSV_EOF_EVENT:
                CSVEndDocumentEvent cellEvent1 = new CSVEndDocumentEvent();
                cellEvent1.setValues(scanner.getTextElement(), scanner.getRow(), scanner.getColumn());
                event = cellEvent1;
                break;
        }

        return event;
    }
}
