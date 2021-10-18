package com.walt;

/**
 * Class BadOrderException: An exception signalling that the order creation
 * has failed.
 */
public class BadOrderException extends Exception {
    public BadOrderException(String errorMessage) {
        super(errorMessage);
    }
}
