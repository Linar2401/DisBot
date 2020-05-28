package ru.bot3.exceptions;

public class GameStopException extends Exception {
    public GameStopException() {
    }

    public GameStopException(String message) {
        super(message);
    }

    public GameStopException(String message, Throwable cause) {
        super(message, cause);
    }

    public GameStopException(Throwable cause) {
        super(cause);
    }

    public GameStopException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
