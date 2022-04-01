package com.file.impl.type; //@date 31.03.2022

import com.file.stream.LangPipeline;

import java.io.IOException;
import java.text.ParseException;

public abstract class TypeParser<T> extends LangPipeline<TypeParser<T>> {

    public static <E> TypeParser<E> stream() {
        return null;
    }

    public static <E, P extends TypeParser<E>> P stream(Class<P> type) {
        return null;
    }

    public abstract T toObject() throws IOException, ParseException;
}
