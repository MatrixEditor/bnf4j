package com.file.impl.csv; //@date 30.03.2022

import com.file.FileScanner;
import com.file.FragmentFileScanner;
import com.file.InputFactory;
import com.file.impl.csv.event.CSVEventAllocator;
import com.file.stream.EventAllocator;

public class CSVInputFactory extends InputFactory {

    private final EventAllocator eventAllocator = new CSVEventAllocator();

    @Override
    public EventAllocator getEventAllocator() {
        return eventAllocator;
    }

    @Override
    public EventAllocator newEventAllocator() {
        return new CSVEventAllocator();
    }

    @Override
    public FragmentFileScanner createFragmentFileScanner(FileScanner scanner) {
        return new CSVFragmentedScanner(scanner);
    }
}
