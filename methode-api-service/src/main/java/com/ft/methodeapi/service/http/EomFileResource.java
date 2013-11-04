package com.ft.methodeapi.service.http;

import static java.lang.String.format;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.ft.api.jaxrs.errors.ClientError;
import com.ft.api.jaxrs.errors.ServerError;
import com.ft.methodeapi.model.EomFile;
import com.ft.methodeapi.service.methode.MethodeException;
import com.ft.methodeapi.service.methode.MethodeFileRepository;
import com.ft.methodeapi.service.methode.NotFoundException;
import com.google.common.base.Optional;
import com.yammer.metrics.annotation.Timed;
import org.omg.CORBA.SystemException;

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
        try {
            return methodeContentRepository.findFileByUuid(uuid);
        } catch(MethodeException | SystemException ex) {
            throw ServerError.status(503).error("error accessing upstream system").exception(ex);
        }
    }

    @DELETE
    @Timed
    @Path("{uuid}")
    public void deleteByUuid(@PathParam("uuid") String uuid) {
        try {
            methodeContentRepository.deleteFileByUuid(uuid);
        } catch (NotFoundException e) {
            throw ClientError.status(404).error(format("%s not found", uuid)).exception();
        } catch (MethodeException | SystemException e) {
            throw ServerError.status(503).error("error accessing upstream system").exception(e);
        }
    }

}
