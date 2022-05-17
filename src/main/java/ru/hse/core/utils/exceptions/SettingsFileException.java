package ru.hse.core.utils.exceptions;

/**
 * Exception class for setting file errors.
 */
public class SettingsFileException extends Exception {
    /**
     * The class' constructor.
     *
     * @param message message with exception information
     */
    public SettingsFileException(String message) {
        super(message);
    }
}
