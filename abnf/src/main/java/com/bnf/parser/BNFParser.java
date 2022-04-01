package com.bnf.parser; //@date 01.04.2022

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.text.ParseException;

public interface BNFParser<T> {

    T parse(Reader reader) throws IOException, ParseException;

    default T parse(String src) throws IOException, ParseException {
        return parse(new StringReader(src));
    }
}
