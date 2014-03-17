package com.ft.methodeapi.atc;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;

/**
 * WhereIsMethodeResource
 *
 * @author Simon.Gibbs
 */
@Path("/where-is-methode")
@Produces(MediaType.APPLICATION_JSON)
public class WhereIsMethodeResource {

    private LastKnownLocation location;


    public WhereIsMethodeResource(LastKnownLocation location) {
        this.location = location;
    }

    @GET
    public Response whereIsIt(@Context Request request) {

        WhereIsMethodeResponse entity = location.lastReport();

        Response.ResponseBuilder responseBuilder = request.evaluatePreconditions(entity.getTimestamp().toDate());
        if (responseBuilder != null) {
            return responseBuilder.build();
        }

        return Response.ok(entity).lastModified(entity.getTimestamp().toDate()).build();

    }



}
