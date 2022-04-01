package com.bnf.parser.event; //@date 01.04.2022

import com.file.stream.LangEvent;

public abstract class BNFEvent implements LangEvent {

    public static final int TEXT_EVENT       = 3;
    public static final int OCCURRENCE_EVENT = 4;
    public static final int GROUP_EVENT      = 5;
    public static final int OR_EVENT         = 6;
    public static final int COMMENT_EVENT    = 7;
    public static final int REF_EVENT        = 8;

    public static class GroupEvent extends BNFEvent {

        private final int     groupType;
        private final boolean closed;

        public GroupEvent(boolean closed, int groupType) {
            this.closed    = closed;
            this.groupType = groupType;
        }

        public int getGroupType() {
            return groupType;
        }

        public boolean isClosed() {
            return closed;
        }

        @Override
        public int getType() {
            return GROUP_EVENT;
        }
    }

    public static class OREvent extends BNFEvent {
        @Override
        public int getType() {
            return OR_EVENT;
        }
    }

    public static class TextEvent extends BNFEvent {
        private final boolean addable;
        private final String  content;

        public TextEvent(String content, boolean addable) {
            this.addable = addable;
            this.content = content;
        }

        public String getContent() {
            return content;
        }

        public boolean isAddable() {
            return addable;
        }

        @Override
        public int getType() {
            return TEXT_EVENT;
        }

        @Override
        public String toString() {
            return getContent();
        }
    }

    public static class OccurrenceEvent extends BNFEvent {

        private final int     min;
        private final int     max;
        private final boolean addable;

        public OccurrenceEvent(int min, int max, boolean addable) {
            this.min     = min;
            this.max     = max;
            this.addable = addable;
        }

        public boolean isAddable() {
            return addable;
        }

        public int getMin() {
            return min;
        }

        public int getMax() {
            return max;
        }

        @Override
        public int getType() {
            return OCCURRENCE_EVENT;
        }
    }
}
