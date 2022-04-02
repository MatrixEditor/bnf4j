package com.bnf.parser.elements; //@date 02.04.2022

import com.bnf.BNFElement;
import com.bnf.parser.BNFFragmentedScanner;
import com.bnf.parser.elements.BNFSequenceGroup.SequenceType;

import java.util.Objects;

public final class BNFElementUtils {

    private BNFElementUtils() {}

    public static BNFSequenceGroup groupOf(int eType, BNFElement<?>... elements) {
        switch (eType) {
            case BNFFragmentedScanner.SCANNER_STATE_OPTIONAL_GROUP:
                return optionalGroup(elements);

            case BNFFragmentedScanner.SCANNER_STATE_STRICT_GROUP:
                return strictGroup(elements);

            default:
                return openGroup(elements);
        }

    }

    public static BNFSequenceGroup openGroup(BNFElement<?>... elements) {
        return group(SequenceType.OPEN, elements);
    }

    public static BNFSequenceGroup strictGroup(BNFElement<?>... elements) {
        return group(SequenceType.STRICT, elements);
    }

    public static BNFSequenceGroup optionalGroup(BNFElement<?>... elements) {
        return group(SequenceType.OPTIONAL, elements);
    }

    public static BNFSequenceGroup group(SequenceType type, BNFElement<?>... elements) {
        Objects.requireNonNull(type);
        return new BNFSequenceGroup(elements, type);
    }
}
