package com.bnf.parser; //@date 01.04.2022

import com.bnf.BNFElement;
import com.bnf.parser.elements.BNFElementUtils;
import com.bnf.parser.elements.BNFSequenceGroup;
import com.bnf.parser.elements.TextElement;
import com.bnf.parser.event.BNFEvent;
import com.bnf.parser.event.BNFEvent.OccurrenceEvent;
import com.bnf.parser.event.BNFInputFactory;
import com.file.FragmentFileScanner;
import com.file.InputFactory;
import com.file.stream.EventAllocator;
import com.file.stream.LangEvent;

import java.io.IOException;
import java.io.Reader;
import java.text.ParseException;

public class BNFParserImpl implements BNFParser<BNFElement<?>> {

    protected final InputFactory   factory   = new BNFInputFactory();
    protected final EventAllocator allocator = factory.getEventAllocator();

    protected FragmentFileScanner scanner;

    @Override
    public BNFElement<?> parse(Reader reader) throws IOException, ParseException {
        scanner = factory.createFragmentFileScanner(reader);
        return read0(-1);
    }

    private BNFSequenceGroup createGroup(BNFElement<?> current, BNFSequenceGroup.SequenceType type) {
        return new BNFSequenceGroup(new BNFElement[]{current}, type);
    }

    private BNFElement<?> read0(int groupType) throws IOException, ParseException {
        BNFElement<?> group = null, current = null;

        // Below some useful variables are defined to react the right way
        // in each different event.
        LangEvent event;
        OccurrenceEvent rangeEvent = null;

        // The different behaviour if this variable is set to true is
        // described below in each statement.
        boolean orCallSpecified = false;

        while (scanner.hasNext()) {
            // Iterating over every element of the given BNF-String or -document
            scanner.next();
            event = allocator.allocate(scanner);

            switch (event.getType()) {
                // There are a few cases to have in mind while looking at an
                // event of type OR_EVENT. But this switch branch just sets
                // the orCallSpecified variable to true.
                case BNFEvent.OR_EVENT: {
                    orCallSpecified = true;
                    break;
                }
                // The TEXT_EVENT and REF_EVENT work almost equal - there is
                // one difference at creating the TextElement to distinguish
                // between the type of the given rule.
                case BNFEvent.TEXT_EVENT:
                case BNFEvent.REF_EVENT: {
                    // First, we create the new TextElement and optionally add
                    // the OccurrenceEvent which was defined above as 'rangeEvent'.
                    // NOTE: The type MUST be of BNFElement<?>, because we change
                    // the base type to RangeBNFElement when adding a range to it.
                    BNFElement<?> txtElement = new TextElement(event.toString(), event.getType() == BNFEvent.REF_EVENT);
                    // If a range is specified we apply it on the created element.
                    if (rangeEvent != null) {
                        txtElement = txtElement.range(rangeEvent.getMin(), rangeEvent.getMax());
                        // IMPORTANT: Set the variable to null prevents another
                        // usage of it.
                        rangeEvent = null;
                    }
                    // Now we have to consider some different cases:
                    if (current != null) {
                        // if the current element is already defined we have
                        // to add the txtElement to the 'current' variable.
                        if (orCallSpecified) {
                            // If there is an OR_EVENT specified before, we
                            // do not add the current element to the group.
                            current = current.or(txtElement);
                            orCallSpecified = false;
                            txtElement = null; // IMPORTANT
                        }
                        // The group might not be initialized, so we have to
                        // create the group from a single element.
                        else if (group == null) {
                            // The helper method automatically creates the right
                            // group instance from the given groupType.
                            group = BNFElementUtils.groupOf(groupType, current);
                        }
                        // REVISIT: The behaviour on this case might not be
                        // the right one.
                        else {
                            // calling this method on a sequence group actually
                            // has no effect since the given elements are
                            // copied into the element list of this group.
                            group = group.append(current);
                        }
                    }
                    // If an OR_EVENT was specified before and the current
                    // element was not null the txtElement was added to it
                    // and set to null afterwards.
                    if (txtElement != null) {
                        current = txtElement;
                    }
                    break;
                }
                // Called if a range was specified. Always called before a
                // TEXT_EVENT is given. DEBUG: ...
                case BNFEvent.OCCURRENCE_EVENT: {
                    // This statement checks if any errors while parsing have
                    // been occurred.
                    if (!(event instanceof OccurrenceEvent)) {
                        throw new ParseException("Event not an instance of OccurrenceEvent.", -1);
                    }
                    rangeEvent = (OccurrenceEvent) event;
                    break;
                }
                // This event is called if the specified group has been closed
                // or a new group was opened.
                case BNFEvent.GROUP_EVENT: {
                    BNFEvent.GroupEvent groupEvent = (BNFEvent.GroupEvent) event;
                    //same code from above
                    // Now we have to consider some different cases:
                    if (current != null) {
                        // if the current element is already defined we have
                        // to add it to the 'current' variable.
                        if (group == null) {
                            if (current instanceof BNFSequenceGroup) {
                                if (groupType == -1) group = current;
                                else group = BNFElementUtils.groupOf(groupType, current);
                            } else {
                                // The helper method automatically creates the right
                                // group instance from the given groupType.
                                group = BNFElementUtils.groupOf(groupType, current);
                            }
                        }
                        // REVISIT: The behaviour on this case might not be
                        // the right one.
                        else {
                            // if the current element is already defined we have
                            // to add it to the 'group' variable.
                            if (orCallSpecified) {
                                group = group.or(current);
                            } else {
                                // calling this method on a sequence group actually
                                // has no effect since the given elements are
                                // copied into the element list of this group.
                                group = group.append(current);
                            }
                        }
                        current = null;
                    }
                    // If we are parsing a strictGroup or optionalGroup the
                    // GroupEvent notifies whether this group element is closed.
                    if (groupType == groupEvent.getGroupType()) {
                        if (groupEvent.isClosed()) {
                            return group;
                        }
                    }
                    // read a new group
                    BNFElement<?> parsedGroup = read0(groupEvent.getGroupType());
                    if (rangeEvent != null) {
                        parsedGroup = parsedGroup.range(rangeEvent.getMin(), rangeEvent.getMax());
                        rangeEvent = null;
                    }

                    if (group == null) {
                        group = parsedGroup;
                    }
                    else {
                        if (orCallSpecified) {
                            group = group.or(parsedGroup);
                        } else {
                            group = group.append(parsedGroup);
                        }
                    }
                    break;
                }
            }
        }
        if (current != null) {
            if (group == null) {
                //noinspection ConstantConditions
                if (current instanceof BNFSequenceGroup) {
                    group = current;
                } else {
                    group = BNFElementUtils.groupOf(groupType, current);
                }
            }
            else {
                if (orCallSpecified) {
                    group = group.or(current);
                } else group = group.append(current);
            }
        }
        // What if current != null;
        return group;
    }

    //TODO cleanup
    private BNFElement<?> parseGroup(int type) throws IOException, ParseException {
        BNFElement<?>            group      = null, current = null;
        LangEvent                event      = null;
        OccurrenceEvent rangeEvent = null;

        boolean orCall = false;
        while (scanner.hasNext()) {
            scanner.next();
            event = allocator.allocate(scanner);

            switch (event.getType()) {
                case BNFEvent.TEXT_EVENT:
                    if (((BNFEvent.TextEvent) event).isAddable() && current != null) {
                        if (group != null && orCall) {
                            group  = group.or(current);
                            orCall = false;
                        } else group = appendToGroup(group, current, type);
                    }

                    BNFElement<?> e = new TextElement(event.toString());
                    if (rangeEvent != null) {
                        e          = e.range(rangeEvent.getMin(), rangeEvent.getMax());
                        rangeEvent = null;
                    }
                    current = e;
                    break;

                case BNFEvent.OCCURRENCE_EVENT:
                    rangeEvent = (OccurrenceEvent) event;
                    if (rangeEvent.isAddable()) {
                        if (current != null) {
                            if (group != null && orCall) {
                                group  = group.or(current);
                                orCall = false;
                            } else group = appendToGroup(group, current, type);
                            current = null;
                        }
                    }
                    break;

                case BNFEvent.OR_EVENT:
                    if (current != null) {
                        group   = appendToGroup(group, current, type);
                        current = null;
                    }
                    orCall = true;
                    break;

                case BNFEvent.GROUP_EVENT:
                    BNFEvent.GroupEvent ge = (BNFEvent.GroupEvent) event;
                    if (current != null) {
                        if (group != null && orCall) {
                            group  = group.or(current);
                            orCall = false;
                        } else group = appendToGroup(group, current, type);
                        current = null;
                    }
                    if (ge.getGroupType() == type) {
                        if (ge.isClosed()) {
                            return group;
                        } else {
                            if (group != null && orCall) {
                                group  = group.or(parseGroup(ge.getGroupType()));
                                orCall = false;
                            } else group = appendToGroup(group, parseGroup(ge.getGroupType()), type);
                        }
                    } else {
                        if (ge.isClosed()) throw new IllegalStateException("wrong character: " + ge.getGroupType());
                        group = appendToGroup(group, parseGroup(ge.getGroupType()), type);
                    }
                    break;

                case BNFEvent.REF_EVENT:
                    if (((BNFEvent.TextEvent) event).isAddable() && current != null) {
                        if (group != null && orCall) {
                            group  = group.or(current);
                            orCall = false;
                        } else group = appendToGroup(group, current, type);
                    }

                    BNFElement<?> ex = new TextElement(event.toString(), true);
                    if (rangeEvent != null) {
                        ex         = ex.range(rangeEvent.getMin(), rangeEvent.getMax());
                        rangeEvent = null;
                    }
                    current = ex;
                    break;

                case BNFEvent.END_DOCUMENT:
                    break;
            }

        }

        if (current != null) {
            if (group != null && orCall) {
                group = group.or(current);
            } else group = appendToGroup(group, current, type);
        }
        return group;
    }

    private BNFElement<?> appendToGroup(BNFElement<?> group, BNFElement<?> current, int t) {
        if (group == null) {
            switch (t) {
                case -1: {
                    return createGroup(current, BNFSequenceGroup.SequenceType.OPEN);
                }
                case BNFFragmentedScanner.SCANNER_STATE_OPTIONAL_GROUP: {
                    return createGroup(current, BNFSequenceGroup.SequenceType.OPTIONAL);
                }
                case BNFFragmentedScanner.SCANNER_STATE_STRICT_GROUP: {
                    return createGroup(current, BNFSequenceGroup.SequenceType.STRICT);
                }
            }
            return createGroup(current, BNFSequenceGroup.SequenceType.OPEN);
        } else return group.append(current);
    }
}
