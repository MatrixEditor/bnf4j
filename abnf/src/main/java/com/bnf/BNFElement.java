package com.bnf;//@date 01.04.2022

public interface BNFElement<B extends BNFElement<B>> {

    String getName();

    B setName(String name);

    /**
     * Tests if the given text equals this definition of this object.
     *
     * @param value the inout text
     * @return true if the value is passed without errors
     */
    @Deprecated
    default boolean eq(String value, BNF bnf) {
        return bnf.is(this, value);
    }

    /**
     * Appends the given {@link BNFElement}s to this object by separating
     * them with a space character. Code:
     * <pre>
     *     &lt;name&gt; other1 other2
     * </pre>
     *
     * @param others the elements to append
     * @return a new qualified object
     */
    B append(BNFElement<?>... others);

    B strictAppend(BNFElement<?>... others);

    /**
     * Makes a sentence and appends the given elements to this. The delimiter
     * is defined as '/':
     * <pre>
     *     &lt;name&gt; / other1 / other2 ...
     * </pre>
     *
     * @param others other {@link BNFElement}s
     * @return a new qualified object
     */
    B or(BNFElement<?>... others);

    /**
     * Declares the occurrence of this element. The associated value
     * as {@link String} would be:
     * <pre>
     *     min*max&lt;name>
     * </pre>
     *
     * @param min the minimum occurrence
     * @param max the maximum occurrence
     * @return a new qualified object
     */
    B range(int min, int max);

    /**
     * Alows this object up to n-times (0 included). Code:
     * <pre>
     *     *&lt;name&gt;
     * </pre>
     *
     * @return a new qualified object
     */
    B timesN(int n);

    /**
     * Alows this object up to n-times (0 included). Code:
     * <pre>
     *     *&lt;name&gt;
     * </pre>
     *
     * @return a new qualified object
     */
    default B timesN() {
        return timesN(BNF.RANGE_INFINITY);
    }

    /**
     * Allows this object only 1-time (0 excluded). Code:
     * <pre>
     *     1*&lt;name&gt;
     * </pre>
     *
     * @return a new qualified object
     */
    B timesOne();

    /**
     * Declares this element or group as 'optional', so the code would
     * look like:
     * <pre>
     *     [&lt;name&gt;]
     * </pre>
     *
     * @return a new qualified object
     */
    B optional();
}
