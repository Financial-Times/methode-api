package com.ft.methodeapi.service;

import com.ft.methodeapi.repository.ContentRepository;
import com.ft.methodeapi.repository.ContentRepositoryFactory;

public class MethodeContentRepositoryFactory implements ContentRepositoryFactory {

    private final String username;
    private final String password;
    private final String host;
    private final int port;

    public MethodeContentRepositoryFactory(Builder builder) {
        this.username = builder.username;
        this.password = builder.password;
        this.host = builder.host;
        this.port = builder.port;
    }

    @Override
    public ContentRepository newRepository() {
        return new MethodeContentRepository(host, port, username, password);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String username;
        private String password;
        private String host;
        private int port;

        public Builder withUsername(String username) {
            this.username = username;
            return this;
        }

        public Builder withPassword(String password) {
            this.password = password;
            return this;
        }

        public Builder withHost(String host) {
            this.host = host;
            return this;
        }

        public Builder withPort(int port) {
            this.port = port;
            return this;
        }

        public MethodeContentRepositoryFactory build() {
            return new MethodeContentRepositoryFactory(this);
        }
    }
}
