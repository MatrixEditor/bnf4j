package com.file; //@date 30.03.2022

import java.io.IOException;
import java.text.ParseException;

public abstract class FragmentFileScanner {

    protected int state;
    protected int eventType;

    protected FileScanner scanner;
    protected FragmentDriver driver;
    protected StringBuffer buffer;

    public FragmentFileScanner(FileScanner scanner) {
        setScanner(scanner);
        buffer = new StringBuffer();
        scanner.setDelegatedScanner(this);
    }

    public abstract void endFile() throws IOException;

    public int next() throws IOException, ParseException {
        return driver.next();
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getState() {
        return state;
    }

    public FileScanner getScanner() {
        return scanner;
    }

    public void setScanner(FileScanner scanner) {
        this.scanner = scanner;
    }

    public void setDriver(FragmentDriver driver) {
        this.driver = driver;
    }

    public FragmentDriver getDriver() {
        return driver;
    }

    public int getEventType() {
        return eventType;
    }

    public boolean hasNext() throws IOException {
        return getEventType() != -1;
    }

    public void setEventType(int eventType) {
        this.eventType = eventType;
    }

    public StringBuffer getBuffer(boolean clear) {
        if (clear) {
            buffer.delete(0, buffer.length());
        }
        return buffer;
    }

    public String getTextElement() {
        return getBuffer(false).toString();
    }
}
