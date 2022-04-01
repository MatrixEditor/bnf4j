package com.file.stream; //@date 30.03.2022

import com.file.FileScanner;
import com.file.FragmentFileScanner;
import com.file.InputFactory;

import java.io.IOException;
import java.io.Reader;
import java.text.ParseException;

public abstract class LangPipeline<S extends LangStream<S>> implements LangStream<S> {

    protected FileScanner         scanner;
    protected InputFactory        factory;
    protected EventAllocator      allocator;
    protected FragmentFileScanner fragmentFileScanner;

    protected LangEvent peekedEvent;
    protected LangEvent lastEvent;

    private boolean done = false;

    public LangPipeline() {}


    @Override
    public boolean hasNext() {
        if (peekedEvent != null) {
            return true;
        }

        boolean next = false;
        try {
            next = fragmentFileScanner.hasNext();
        } catch (IOException e) {
            return false;
        }
        return next;
    }

    @Override
    public LangEvent nextEvent() throws IOException, ParseException {
        if (peekedEvent != null) {
            lastEvent = peekedEvent;
            peekedEvent = null;
            return lastEvent;
        }
        if (hasNext()) {
            fragmentFileScanner.next();
            return lastEvent = allocator.allocate(fragmentFileScanner);
        }
        lastEvent = null;
        throw new ParseException("", -1);
    }
}
