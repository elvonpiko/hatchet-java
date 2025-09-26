package io.hatchet.sdk.exceptions;

/**
 * Thrown when client configuration is invalid or missing.
 */
public final class ConfigException extends HatchetException {
    public ConfigException(String msg) { super(msg); }
    public ConfigException(String msg, Throwable t) { super(msg, t); }
}
