package com.ft.methodeapi.service;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.google.common.base.Optional;
import com.yammer.metrics.annotation.Timed;

@Path("eom-file")
@Produces(MediaType.APPLICATION_JSON)
public class EomFileResource {

    private final MethodeFileRepository methodeContentRepository;

    public EomFileResource(MethodeFileRepository methodeContentRepository) {
        this.methodeContentRepository = methodeContentRepository;
    }

    @GET
    @Timed
    @Path("{uuid}")
    public Optional<EomFile> getByUuid(@PathParam("uuid") String uuid) {
        return methodeContentRepository.findFileByUuid(uuid);
    }

}
