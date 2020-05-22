package ru.ilyahiguti.authmetitle.title;

public class TitleDescriptionException extends Exception {
    public TitleDescriptionException(String message) {
        super(message);
    }

    @Override
    public synchronized Throwable initCause(Throwable cause) {
        return this;
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
