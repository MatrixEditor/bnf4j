package com.bnf.parser.elements; //@date 01.04.2022

import com.bnf.AbstractBNFElement;
import com.bnf.BNFElement;

import java.util.StringJoiner;

public class BNFSequenceGroup extends AbstractBNFElement<BNFSequenceGroup> {

    private final BNFElement<?>[] elements;
    private final SequenceType    type;

    public BNFSequenceGroup(BNFElement<?>[] elements, SequenceType type) {
        this.elements = elements;
        this.type     = type;
        this.name     = groupBy(elements, type);
    }

    public BNFSequenceGroup(String name, BNFElement<?>... elements) {
        super.name    = name;
        this.elements = elements;
        this.type = SequenceType.OPEN;
    }

    private String groupBy(BNFElement<?>[] bnfElements, SequenceType type) {
        StringJoiner sb = new StringJoiner(type.delimiter, type.prefix, type.postfix);
        for (BNFElement<?> e : bnfElements) {
            String n = e.getName();
            sb.add(n);
        }
        return sb.toString();
    }

    public BNFElement<?>[] getElements() {
        return elements;
    }

    public SequenceType getType() {
        return type;
    }

    public enum SequenceType {
        OR("", "", " / "),
        STRICT("( ", " )", " "),
        STRICT_OR("[ ", " ]", " / "),
        OPEN("", "", " ");

        private final String prefix, postfix, delimiter;

        SequenceType(String prefix, String postfix, String delimiter) {
            this.prefix    = prefix;
            this.postfix   = postfix;
            this.delimiter = delimiter;
        }

        public String getPostfix() {
            return postfix;
        }

        public String getDelimiter() {
            return delimiter;
        }

        public String getPrefix() {
            return prefix;
        }
    }
}
