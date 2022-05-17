package ru.hse.core.utils.exceptions;

/**
 * Exception class for command line arguments error.
 */
public class CommandLineArgumentsException extends Exception {
    /**
     * The class' constructor.
     *
     * @param message message with exception information
     */
    public CommandLineArgumentsException(String message) {
        super(message);
    }
}
