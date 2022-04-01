package com.bnf; //@date 01.04.2022

import com.bnf.parser.BNFFragmentedScanner;
import com.bnf.parser.BNFParser;
import com.bnf.parser.BNFParserImpl;
import com.bnf.parser.elements.BNFSequenceGroup;
import com.bnf.parser.elements.RangeBNFElement;
import com.bnf.parser.elements.TextElement;

import java.io.IOException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * This class is the base for creating grammar structures from definitions in
 * the Backus-Naur Form (BNF). Actually, this implementation refers to the
 * modified version of the standard BNF, called Augmented BNF (ABNF).
 * <p>
 * The implementation makes use of the <a href="https://datatracker.ietf.org/doc/html/rfc2234">
 * RFC2234</a> document, which specifies the structure of ABNF. All rules
 * are encoded in {@code UTF8}. Note that this implementation also uses
 * an event-based parser.
 * <p>
 * This class has two features: First, in this object all rules related to
 * a grammar are stored as a {@link BNFElement}. Second, the method
 * {@link BNF#is(BNFElement, String)} matches the given element with the
 * given value. All steps in this method are highly documented.
 * <p>
 * An example usage of this library is described below. Let's consider the following
 * rule:
 * <pre>
 * example := "Hello" ("You" / "World")
 * </pre>
 * In order to implement this rule we have to specify three different components:
 * <pre>
 *     BNFElement&lt;?> youOrWorld = bnf.rule("'you'").or(bnf.rule("'World'");
 *     BNFElement&lt;?> hello = bnf.rule("'Hello'");
 *
 *     bnf.rule("example", hello.append(youOrWorld));
 * </pre>
 * <strong>Note:</strong> Calling the 'append()' or 'or()' method of the BNFElement
 * class does not change the base element. The called method returns a new instance
 * of the required element type.
 * <p>
 * <strong>Note:</strong> You can use the single quote (') character to indicate
 * a DQUOTE, which is specified in RFC2234. Alternatively you can use an escaped
 * double quote (\") in the rule string.
 *
 * @see BNFElement
 * @see BNFParserImpl
 * @see BNFFragmentedScanner
 * @see BNFCharSpec
 */
public final class BNF {

    public static final int RANGE_INFINITY = -1;

    private final Map<String, BNFElement<?>> reg = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    private final BNFParser<BNFElement<?>> bnfParser = new BNFParserImpl();

    private BNF() {}

    public static BNF getInstance() {
        return new BNF();
    }

    public boolean is(BNFElement<?> element, String value) {
        Objects.requireNonNull(element);
        Objects.requireNonNull(value);

        return internalEq(element, value, 0) != -1;
    }

    public <B extends BNFElement<B>> BNFElement<B> ref(String name) {
        if (!reg.containsKey(name)) {
            return null;
        }
        //noinspection unchecked
        return (BNFElement<B>) reg.get(name);
    }

    public <B extends BNFElement<B>> BNFElement<B> rule(String name, BNFElement<B> rule) {
        if (reg.containsKey(name)) {
            return null;
        }
        reg.put(name, rule);
        return rule;
    }

    public <B extends BNFElement<B>> BNFElement<B> rule(String name, String rule) throws IOException, ParseException {
        Objects.requireNonNull(name);
        Objects.requireNonNull(rule);

        //noinspection unchecked
        return rule(name, (BNFElement<B>) bnfParser.parse(rule));
    }

    public <B extends BNFElement<B>> BNFElement<B> rule(String rule) throws IOException, ParseException {
        Objects.requireNonNull(rule);

        //noinspection unchecked
        return (BNFElement<B>) bnfParser.parse(rule);
    }

    private int internalEq(BNFElement<?> element, String value, int offset) {
        if (element instanceof TextElement) {
            TextElement e = (TextElement) element;
            // The type defines how to handle this TextElement and was set during
            // the parsing process. There are five types: Text, Concat, Range,
            // Single and Ref
            switch (e.getType()) {
                case Ref: {
                    // We have to resolve the referred element. If the returned
                    // object is null the reference is undefined.
                    // REVISIT: maybe throw exception here
                    BNFElement<?> ref = ref(e.getName());
                    if (ref == null) return -1;
                    // Normal checking is done below
                    int rel = internalEq(ref, value, offset);
                    if (rel == -1) return -1;
                    offset += rel;
                    break;
                }

                // With Text and Concat type every character beginning at the
                // offset position is compared and if it doesn't fit -1 is returned.
                case Text:
                case Concat: {
                    char[] x = (char[]) e.getContent();
                    for (int i = offset; i < x.length; i++) {
                        // REVISIT: additional check of index bounds
                        if (i >= value.length()) return -1;
                        if (x[i] != value.charAt(i)) return -1;
                    }
                    offset += x.length;
                    break;
                }

                // Checking if the current character at offset position meets
                // the defined range from the TextElement.
                case Range: {
                    // REVISIT: additional check of index bounds
                    if (offset >= value.length()) return -1;
                    char[] y  = (char[]) e.getContent();
                    char current = value.charAt(offset);
                    if (!(current >= y[0] && current <= y[1])) return -1;
                    offset++;
                    break;
                }

                // Checking if the current character at offset position meets
                // the defined range from the TextElement.
                case Single: {
                    // REVISIT: additional check of index bounds
                    if (offset >= value.length()) return -1;
                    if ((char) e.getContent() != value.charAt(offset)) {
                        return -1;
                    }
                    offset++;
                    break;
                }
            }
            // Checking is done now and the relative offset ca be returned. This
            // value is guaranteed to be not -1.
            return offset;

        }
        else if (element instanceof RangeBNFElement) {
            RangeBNFElement e   = (RangeBNFElement) element;
            int  min = e.getMin(), max = e.getMax();

            int cOffset = internalEq(e.getLinked(), value, offset);
            // Case 1: the first try on the linked object was not a match which
            // leads to the question if this object is optional. The object is
            // optional if, and only if the minimum is set to -1.
            if (cOffset == -1) {
                // Therefore, if min is greater than 0, there is no match.
                if (min > 0) return -1;

            }
            offset += cOffset;
            // Case 2: The minimum is greater than 1. We can continue if we
            // reach the minimum amount of checks with the linked object.
            if (min > 1) {
                // The loop starts from 1 and ends at the min-value. As described
                // above if the return value is -1 there was no match.
                for (int i = 1; i < min; i++) {
                    // REVISIT: additional check of index bounds
                    if (offset >= value.length()) return -1;

                    cOffset = internalEq(e.getLinked(), value, offset);
                    if (cOffset == -1) return -1;
                    offset += cOffset;
                }
            }
            // Case 3: The maximum is -1. In this case we loop until we have
            // no match by the underlying linked BNFElement.
            if (max == RANGE_INFINITY) {
                while ((cOffset = internalEq(e.getLinked(), value, offset)) != -1) {
                    offset += cOffset;
                    // REVISIT: additional check of index bounds. Here we have
                    // to return the offset because it is optional to have this
                    // value another time.
                    if (offset >= value.length()) return offset;
                }
                return offset;
            }
            // Case 4: we have a defined maximum. The case is not very different
            // to Case 2 where we looped until no match wa given. The same procedure
            // is used in this case:
            for (int i = min; i < max; i++) {
                // REVISIT: additional check of index bounds
                if (offset >= value.length()) return -1;

                cOffset = internalEq(e.getLinked(), value, offset);
                if (cOffset == -1) return -1;
                offset += cOffset;
            }
            // Checking is done now and the relative offset ca be returned. This
            // value is guaranteed to be not -1.
            return offset;

        }
        else if (element instanceof BNFSequenceGroup) {
            BNFSequenceGroup group = (BNFSequenceGroup) element;
            BNFElement<?>[] elements = group.getElements();

            // If there are no values stored the check can be passed.
            if (elements.length == 0) return offset;
            int cOffset;
            // The type defines how to handle this BNFSequenceGroup and was set during
            // the parsing process. There are four types: OPEN, OR, STRICT and STRICT_OR
            switch (group.getType()) {
                // The OPEN type can be described as a logical AND concatenation
                // of different elements.
                case STRICT:
                case OPEN: {
                    for (BNFElement<?> tmp : elements) {
                        if ((cOffset = internalEq(tmp, value, offset)) == -1) return -1;
                        offset += cOffset;
                    }
                    break;
                }
                // The OR type can be described as a logical OR concatenation
                // of different elements. The loop does not stop if one element
                // fails to match.
                case STRICT_OR:
                case OR: {
                    for (BNFElement<?> tmp : elements) {
                        if ((cOffset = internalEq(tmp, value, offset)) != -1) {
                            // The jump would be out of this loop but not out of
                            // the switch statement, so we have to return the
                            // current offset directly.
                            return cOffset + offset;
                        }
                    }
                    return -1;
                }
            }
            return offset;
        }
        else return -1;
    }

}
