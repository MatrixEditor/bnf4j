package com.file.stream;//@date 30.03.2022

import com.file.InputFactory;

import java.io.*;
import java.text.ParseException;
import java.util.Objects;
import java.util.function.Consumer;

public interface LangStream<S extends LangStream<S>> {

    S setSource(Reader reader);

    S configure(InputFactory factory);

    boolean hasNext();

    LangEvent nextEvent() throws IOException, ParseException;

    default void forEach(Consumer<? super LangEvent> consumer) {}

    default S setSource(String src) throws IOException {
        Objects.requireNonNull(src);
        return setSource(new StringReader(src));
    }

    default S setSource(File source) throws IOException {
        Objects.requireNonNull(source);
        return setSource(new FileReader(source));
    }
}
