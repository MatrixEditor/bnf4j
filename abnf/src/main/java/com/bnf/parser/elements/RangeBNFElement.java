package com.bnf.parser.elements; //@date 01.04.2022

import com.bnf.AbstractBNFElement;
import com.bnf.BNFElement;

import static com.bnf.BNFCharSpec.*;

public class RangeBNFElement extends AbstractBNFElement<RangeBNFElement> {

    private final int min, max;

    private final BNFElement<?> linked;

    public RangeBNFElement(int min, int max, BNFElement<?> linked) {
        this.min    = min;
        this.max    = max;
        this.linked = linked;

        StringBuilder sb = new StringBuilder();
        if (min == 0 && max == 1) {
            // At this point we have an optional BNFElement. The name of the
            // given element has to be appended to this object. The structure
            // will be the following:
            // this.rule = [ linked.name ]
            String n = linked.getName();
            name = sb.append(OPTIONAL_OPENING).append(SPACE)
                     .append(n)
                     .append(SPACE).append(OPTIONAL_CLOSING)
                     .toString();
        }
        // Handle the min or max occurrences, for instance if max or min is < 0
        // the values should be ignored
        else {
            if (min == max) {
                // RFC 2234: 3*3<element> allows exactly 3 elements
                sb.append(min);
            } else {
                if (min > 0) {
                    sb.append(min);
                }
                sb.append(OCCURRENCE_INDICATOR);
                if (max > 0) {
                    sb.append(max);
                }
            }
            // equivalent to <a>*<b>element
            name = sb.toString() + linked.getName();
        }
    }

    public int getMax() {
        return max;
    }

    public int getMin() {
        return min;
    }

    public BNFElement<?> getLinked() {
        return linked;
    }

}
