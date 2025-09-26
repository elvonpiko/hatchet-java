package io.hatchet.sdk.exceptions;

public class HatchetException extends RuntimeException {
    public HatchetException(String message) { super(message); }
    public HatchetException(String message, Throwable cause) { super(message, cause); }
}
