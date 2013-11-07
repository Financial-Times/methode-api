package com.ft.methodeapi.service.methode;

public class ActionNotPermittedException extends RuntimeException {
    public ActionNotPermittedException(String message) {
        super(message);
    }
}
