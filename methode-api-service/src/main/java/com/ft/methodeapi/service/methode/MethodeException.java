package com.ft.methodeapi.service.methode;

/**
 * MethodeException
 *
 * @author Simon.Gibbs
 */
public class MethodeException extends RuntimeException {

    public MethodeException(Throwable cause) {
        super(cause);
    }

    public MethodeException(String message, Throwable cause) {
        super(message, cause);
    }
}
