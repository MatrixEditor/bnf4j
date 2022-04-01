package com.file; //@date 30.03.2022

import com.file.stream.EventAllocator;

import java.io.Reader;

public abstract class InputFactory {

    public abstract EventAllocator getEventAllocator();

    public abstract EventAllocator newEventAllocator();

    public FileScanner createFileScanner(Reader reader) {
        return new FileScanner(reader);
    }

    public FragmentFileScanner createFragmentFileScanner(Reader reader) {
        return createFragmentFileScanner(createFileScanner(reader));
    }

    public abstract FragmentFileScanner createFragmentFileScanner(FileScanner scanner);
}