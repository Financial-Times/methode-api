package com.ft.methodeapi.service;

import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.google.common.base.Optional;
import com.yammer.metrics.annotation.Timed;

@Path("content")
@Produces(MediaType.APPLICATION_JSON)
public class ContentResource {

    private final MethodeContentRepository methodeContentRepository;

    public ContentResource(@NotNull MethodeContentRepository methodeContentRepository) {
        this.methodeContentRepository = methodeContentRepository;
    }

    @GET
    @Timed
    @Path("{uuid}")
    public Optional<Content> getContent(@PathParam("uuid") String uuid) {
        return methodeContentRepository.findContentByUuid(uuid);
    }

}
