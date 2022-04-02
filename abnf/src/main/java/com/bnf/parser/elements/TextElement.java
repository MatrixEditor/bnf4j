package com.bnf.parser.elements; //@date 01.04.2022

import com.bnf.AbstractBNFElement;
import com.bnf.BNFCharSpec;

import java.util.function.Function;

public class TextElement extends AbstractBNFElement<TextElement> implements BNFCharSpec {

    public static final String decimalPattern     = "%d";
    public static final String hexadecimalPattern = "%x";

    private Object content;
    private String orig;

    private Type type;

    public TextElement(String value) {
        this(value, false);
    }

    public TextElement(String value, boolean ref) {
        if (value.startsWith(decimalPattern)) {
            readNum(value, this::decimalOf);
        } else if (value.startsWith(hexadecimalPattern)) {
            readNum(value, this::hexOf);
        } else {
            if (value.contains(")") || value.contains("]")) {
                if (!value.contains("\"")) {
                    throw new IllegalArgumentException();
                }
            }
            if (ref) {
                type = Type.Ref;
                name = value;
            } else {
                type = Type.Text;
                name = value.contains("\"") ? value : "\"" + value + "\"";
            }
            content = value.toCharArray();
            orig = value;
        }
    }

    private void readNum(String value, Function<String, Integer> mapper) {
        if (value.contains(asString(VALUE_SEQUENCE_DELIMITER))) {
            String[] values = value.substring(2).split("[.]");
            char[]   c      = new char[values.length];
            for (int i = 0; i < values.length; i++) {
                c[i] = (char) mapper.apply(values[i]).intValue();
            }
            content = c;
            type    = Type.Concat;
        } else if (value.contains(asString(RANGE_DELIMITER))) {
            String[] values = value.substring(2).split("[-]");
            content = new char[]{
                    (char) mapper.apply(values[0]).intValue(),
                    (char) mapper.apply(values[1]).intValue()
            };
            type    = Type.Range;
        } else {
            content = (char) mapper.apply(value.substring(2)).intValue();
            type    = Type.Single;
        }
        orig = value;
        name = value;
    }

    public Type getType() {
        return type;
    }

    public Object getContent() {
        return content;
    }

    public enum Type {
        Range, Concat, Text, Ref, Single
    }
}
