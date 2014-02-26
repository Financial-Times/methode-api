package com.ft.methodeapi.service.methode.connection;

import org.joda.time.DateTime;

/**
 * RecoverableAllocationException
 *
 * @author Simon.Gibbs
 */
public class RecoverableAllocationException extends RuntimeException {

    private final DateTime creationTime;

    public RecoverableAllocationException(Throwable cause) {
        super(cause);
        this.creationTime = DateTime.now();
    }

    @Override
    public String getMessage() {
        return String.format("Recoverable Exception at ts=%s: %s", creationTime, super.getMessage() );
    }
}
