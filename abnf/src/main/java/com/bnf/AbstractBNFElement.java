package com.bnf; //@date 01.04.2022

import com.bnf.parser.elements.BNFSequenceGroup;
import com.bnf.parser.elements.RangeBNFElement;

public abstract class AbstractBNFElement<B extends BNFElement<B>> implements BNFElement<B> {

    public static final String EMPTY_BNF_ELEMENT_NAME = "";

    protected String name;

    @Override
    public String getName() {
        if (name == null) {
            return EMPTY_BNF_ELEMENT_NAME;
        }
        return name;
    }

    @Override
    public B timesN(int n) {
        return range(0, n);
    }

    @Override
    public B timesOne() {
        return range(1, 1);
    }

    @Override
    public B optional() {
        return range(0, 1);
    }

    @Override
    public B range(int min, int max) {
        //noinspection unchecked
        return (B) new RangeBNFElement(min, max, this);
    }

    @Override
    public B setName(String name) {
        this.name = name;
        //noinspection unchecked
        return (B) this;
    }

    @Override
    public B append(BNFElement<?>... others) {
        return getSequence(others, BNFSequenceGroup.SequenceType.OPEN, true);
    }

    private B getSequence(BNFElement<?>[] others, BNFSequenceGroup.SequenceType type, boolean shouldAdd) {
        if (this instanceof BNFSequenceGroup && shouldAdd) {
            BNFSequenceGroup group = (BNFSequenceGroup) this;
            //noinspection unchecked
            return (B) group.add(others);
        }

        BNFElement<?>[] bnfElements = new BNFElement<?>[others.length + 1];
        bnfElements[0] = this;
        System.arraycopy(others, 0, bnfElements, 1, others.length);
        //noinspection unchecked
        return (B) new BNFSequenceGroup(bnfElements, type);
    }

    @Override
    public B strictAppend(BNFElement<?>... others) {
        return getSequence(others, BNFSequenceGroup.SequenceType.STRICT, false);
    }

    @Override
    public B or(BNFElement<?>... others) {
        return getSequence(others, BNFSequenceGroup.SequenceType.OR, false);
    }


    protected int hexOf(String val) {
        return Integer.parseInt(val, 16);
    }

    protected int decimalOf(String val) {
        return Integer.parseInt(val, 10);
    }

    protected int binaryOf(String val) {
        return Integer.parseInt(val, 2);
    }
}
