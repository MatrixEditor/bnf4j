package com.bnf;//@date 01.04.2022

import com.file.spec.CharSetSpec;

public interface BNFCharSpec extends CharSetSpec {

    char LINE_FEED = 0x0A;

    char SPACE = 0x20;

    char OPTIONAL_CLOSING = 0x5D;

    char OPTIONAL_OPENING = 0x5B;

    char GROUP_CLOSING = 0x29;

    char GROUP_OPENING = 0x28;

    char OCCURRENCE_INDICATOR = 0x2A; //.

    char OR_DELIMITER = 0x2F;

    char RANGE_DELIMITER = 0x2D; //-

    char VALUE_SEQUENCE_DELIMITER = 0x2E;

    default String asString(char x) {
        return String.valueOf(x);
    }

    char TEXT_QUOTE = 0x22;
    char TEXT_QUOTE_2 = 0x27;

    char TEXT_NUM = 0x25;

    char COMMENT = 0x3B;

    /**
     * As defined in <a href="https://datatracker.ietf.org/doc/html/rfc2234#appendix-A">
     * Appendix-A</a> of {@code RFC2234}:
     * <pre>
     *     SP = %x20 = 0x20 = ' '
     * </pre>
     *
     * @param cc the character to be compared
     */
    @Override
    default boolean isSpace(char cc) {
        return cc == SPACE;
    }

    /**
     * As defined in <a href="https://datatracker.ietf.org/doc/html/rfc2234#appendix-A">
     * Appendix-A</a> of {@code RFC2234}:
     * <pre>
     *     LF  =  %x0A  =  \n
     * </pre>
     *
     * @param cc the character to be compared
     */
    @Override
    default boolean isNewLine(char cc) {
        return cc == LINE_FEED;
    }
}
