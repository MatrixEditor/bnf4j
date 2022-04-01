package com.bnf.parser; //@date 01.04.2022

import com.bnf.parser.event.BNFEvent;
import com.file.FragmentFileScanner;
import com.file.stream.EventAllocator;
import com.file.stream.LangEvent;

public class BNFEventAllocator implements EventAllocator {

    @Override
    public LangEvent allocate(FragmentFileScanner fragmentFileScanner) {
        LangEvent event = null;

        BNFFragmentedScanner sc = (BNFFragmentedScanner) fragmentFileScanner;
        switch (fragmentFileScanner.getEventType()) {
            case BNFEvent.TEXT_EVENT:
            case BNFEvent.COMMENT_EVENT:
            case BNFEvent.REF_EVENT:
                event = new BNFEvent.TextEvent(sc.getTextElement(), sc.isAddable());
                break;

            case BNFEvent.OCCURRENCE_EVENT:
                event = new BNFEvent.OccurrenceEvent(sc.getMin(), sc.getMax(), sc.isAddable());
                break;

            case BNFEvent.GROUP_EVENT:
                event = new BNFEvent.GroupEvent(sc.isGroupClosed(), sc.getGroupType());
                break;

            case BNFEvent.OR_EVENT:
                event = new BNFEvent.OREvent();
                break;


        }
        return event;
    }
}
