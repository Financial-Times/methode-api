package com.ft.methodeapi.service;

import EOM.RepositoryError;
import EOM.RepositoryPackage.InvalidLogin;
import EOM.Session;
import com.ft.methodeapi.connectivity.EomRepositoryFactory;
import com.ft.methodeapi.connectivity.EomSessionFactory;
import com.ft.methodeapi.connectivity.EomSessionWrapper;
import com.ft.methodeapi.repository.ContentRepository;
import com.ft.methodeapi.repository.ContentRepositoryFactory;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.NotFound;

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

//        try (EomRepositoryFactory repositoryFactory = new EomRepositoryFactory(host, port);
//             EomSessionWrapper sessionWrapper = new EomSessionFactory(username, password, repositoryFactory.createRepository()).createSession()) {
//            Session eomSession = sessionWrapper.getSession();
//
//            FileSystemAdmin fileSystemAdmin = EOM.FileSystemAdminHelper
//                    .narrow(eomSession
//                            .resolve_initial_references("FileSystemAdmin"));
//
//            String uri = "eom:/uuids/" + uuid;
//
//            FileSystemObject fso = fileSystemAdmin.get_object_with_uri(uri);
//            String type = fso.get_type_name();

//            if ("EOM::Story".equals(type) || "EOM::CompoundStory".equals(type)) {
//                EOM.File eomFile = EOM.FileHelper.narrow(fso);
//                Content content = new Content(eomFile.read_all());
//                return Optional.of(content);
//            } else {
//                return Optional.absent();
//            }

        return new MethodeContentRepository(host,port,username,password);
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
