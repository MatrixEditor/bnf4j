package com.file.impl.type; //@date 31.03.2022

import com.file.stream.LangPipeline;
import com.file.stream.LangStream;

import java.io.IOException;
import java.text.ParseException;

public abstract class TypeParser<T, S extends LangStream<S>> extends LangPipeline<S> {

    public abstract T toObject() throws IOException, ParseException;
}
