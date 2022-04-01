package com.bnf.parser.event; //@date 01.04.2022

import com.bnf.parser.BNFEventAllocator;
import com.bnf.parser.BNFFragmentedScanner;
import com.file.FileScanner;
import com.file.FragmentFileScanner;
import com.file.InputFactory;
import com.file.stream.EventAllocator;

public class BNFInputFactory extends InputFactory {

    @Override
    public EventAllocator getEventAllocator() {
        return newEventAllocator();
    }

    @Override
    public EventAllocator newEventAllocator() {
        return new BNFEventAllocator();
    }

    @Override
    public FragmentFileScanner createFragmentFileScanner(FileScanner scanner) {
        return new BNFFragmentedScanner(scanner);
    }
}
