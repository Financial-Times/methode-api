package com.ft.methodeapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import com.google.common.base.Optional;

public class Message {

    private final Optional<String> message;

    public Message(@JsonProperty("message") Optional<String> message) {
        this.message = message;
    }

    public Optional<String> getMessage() {
        return message;
    }

    protected Objects.ToStringHelper toStringHelper() {
        return Objects
                .toStringHelper(this)
                .add("message", message);
    }

    @Override
    public String toString() {
        return toStringHelper().toString();
    }
}
