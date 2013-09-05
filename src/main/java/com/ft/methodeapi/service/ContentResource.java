package com.ft.methodeapi.service;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import EOM.FileSystemAdmin;
import EOM.FileSystemObject;
import EOM.Session;
import com.ft.methodeapi.connectivity.EomRepositoryFactory;
import com.ft.methodeapi.connectivity.EomSessionFactory;
import com.ft.methodeapi.connectivity.EomSessionWrapper;
import com.google.common.base.Optional;
import com.yammer.metrics.annotation.Timed;

@Path("content")
@Produces(MediaType.APPLICATION_JSON)
public class ContentResource {

    private final String methodeHostName;
    private final int methodePort;
    private final String methodeUserName;
    private final String methodePassword;

    public ContentResource(String methodeHostName, int methodePort, String methodeUserName, String methodePassword) {
        this.methodeHostName = methodeHostName;
        this.methodePort = methodePort;
        this.methodeUserName = methodeUserName;
        this.methodePassword = methodePassword;
    }

    @GET
    @Timed
    @Path("{uuid}")
    public Optional<Content> getContent(@PathParam("uuid") String uuid) throws Exception {
        try (EomRepositoryFactory repositoryFactory = new EomRepositoryFactory(methodeHostName, methodePort);
             EomSessionWrapper sessionWrapper = new EomSessionFactory(methodeUserName, methodePassword, repositoryFactory.createRepository()).createSession()) {
            Session eomSession = sessionWrapper.getSession();

            FileSystemAdmin fileSystemAdmin = EOM.FileSystemAdminHelper
                    .narrow(eomSession
                            .resolve_initial_references("FileSystemAdmin"));

            String uri = "eom:/uuids/" + uuid;

            FileSystemObject fso = fileSystemAdmin.get_object_with_uri(uri);
            String type = fso.get_type_name();

            if ("EOM::Story".equals(type) || "EOM::CompoundStory".equals(type)) {
                EOM.File eomFile = EOM.FileHelper.narrow(fso);
                Content content = new Content(eomFile.read_all());
                return Optional.of(content);
            } else {
                return Optional.absent();
            }
        }
    }
}
