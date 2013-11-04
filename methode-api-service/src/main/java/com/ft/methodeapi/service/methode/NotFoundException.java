package com.ft.methodeapi.service.methode;

import static java.lang.String.format;

import com.google.common.base.Preconditions;

public class NotFoundException extends MethodeException {

    private final String uuid;

    public NotFoundException(String uuid) {
        super(format("%s not found", uuid));
        Preconditions.checkArgument(uuid != null, "uuid must not be null");
        this.uuid = uuid;
    }

    public String getUuid() {
        return uuid;
    }
}
