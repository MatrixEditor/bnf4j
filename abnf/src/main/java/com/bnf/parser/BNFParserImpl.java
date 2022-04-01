package com.bnf.parser; //@date 01.04.2022

import com.bnf.BNFElement;
import com.bnf.parser.elements.BNFSequenceGroup;
import com.bnf.parser.elements.TextElement;
import com.bnf.parser.event.BNFEvent;
import com.bnf.parser.event.BNFInputFactory;
import com.file.FragmentFileScanner;
import com.file.InputFactory;
import com.file.stream.EventAllocator;
import com.file.stream.LangEvent;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.text.ParseException;

public class BNFParserImpl implements BNFParser<BNFElement<?>> {

    protected final InputFactory   factory   = new BNFInputFactory();
    protected final EventAllocator allocator = factory.getEventAllocator();

    protected FragmentFileScanner scanner;

    @Override
    public BNFElement<?> parse(Reader reader) throws IOException, ParseException {
        scanner = factory.createFragmentFileScanner(reader);
        return parseGroup(-1);
    }

    private BNFSequenceGroup createGroup(BNFElement<?> current) {
        return new BNFSequenceGroup(new BNFElement[]{current}, BNFSequenceGroup.SequenceType.OPEN);
    }

    private BNFElement<?> parseGroup(int type) throws IOException, ParseException {
        BNFElement<?>            group      = null, current = null;
        LangEvent                event      = null;
        BNFEvent.OccurrenceEvent rangeEvent = null;

        while (scanner.hasNext()) {
            scanner.next();
            event = allocator.allocate(scanner);

            switch (event.getType()) {
                case BNFEvent.TEXT_EVENT:
                    if (((BNFEvent.TextEvent) event).isAddable() && current != null) {
                        group = appendToGroup(group, current);
                    }

                    BNFElement<?> e = new TextElement(event.toString());
                    if (rangeEvent != null) {
                        e = e.range(rangeEvent.getMin(), rangeEvent.getMax());
                    }
                    current = e;
                    break;

                case BNFEvent.OCCURRENCE_EVENT:
                    rangeEvent = (BNFEvent.OccurrenceEvent) event;
                    if (rangeEvent.isAddable()) {
                        if (current != null) {
                            group = appendToGroup(group, current);
                        }
                    }
                    break;

                case BNFEvent.OR_EVENT:
                    if (current != null) {
                        current = current.or(current);
                    }
                    break;

                case BNFEvent.GROUP_EVENT:
                    BNFEvent.GroupEvent ge = (BNFEvent.GroupEvent) event;
                    if (ge.getGroupType() == type) {
                        if (ge.isClosed()) {
                            return group;
                        } else {
                            group = appendToGroup(group, parseGroup(ge.getGroupType()));
                        }
                    } else {
                        if (ge.isClosed()) throw new IllegalStateException("wrong character: " + ge.getGroupType());
                        group = appendToGroup(group, parseGroup(ge.getGroupType()));
                    }
                    break;

                case BNFEvent.REF_EVENT:
                    if (((BNFEvent.TextEvent) event).isAddable() && current != null) {
                        group = appendToGroup(group, current);
                    }

                    BNFElement<?> ex = new TextElement(event.toString(), true);
                    if (rangeEvent != null) {
                        ex = ex.range(rangeEvent.getMin(), rangeEvent.getMax());
                    }
                    current = ex;
                    break;
            }

        }

        if (current != null) {
            group = appendToGroup(group, current);
        }
        return group;
    }

    private BNFElement<?> appendToGroup(BNFElement<?> group, BNFElement<?> current) {
        if (group == null) {
            return createGroup(current);
        } else return group.append(current);
    }
}
