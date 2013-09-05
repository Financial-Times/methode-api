package com.ft.methodeapi.service;

import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.ft.methodeapi.repository.ContentRepository;
import com.ft.methodeapi.repository.ContentRepositoryFactory;
import com.google.common.base.Optional;
import com.yammer.metrics.annotation.Timed;

@Path("content")
@Produces(MediaType.APPLICATION_JSON)
public class ContentResource {

    private final ContentRepositoryFactory contentRepositoryFactory;

    public ContentResource(@NotNull ContentRepositoryFactory contentRepositoryFactory) {
        this.contentRepositoryFactory = contentRepositoryFactory;
    }

    @GET
    @Timed
    @Path("{uuid}")
    public Optional<Content> getContent(@PathParam("uuid") String uuid) {

        ContentRepository contentRepository = contentRepositoryFactory.newRepository();
        try {
            return contentRepository.findContentByUuid(uuid);
        } finally {
            contentRepository.close();
        }

    }

}
