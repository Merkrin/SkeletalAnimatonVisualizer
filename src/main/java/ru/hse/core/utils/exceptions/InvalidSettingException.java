package ru.hse.core.utils.exceptions;

/**
 * Exception class for invalid setting error.
 */
public class InvalidSettingException extends Exception {
    /**
     * The class' constructor.
     *
     * @param message message with exception information
     */
    public InvalidSettingException(String message) {
        super(message);
    }
}
