package com.ft.methodeApi.service;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import com.yammer.metrics.annotation.Timed;

@Path("content")
public class ContentResource {

    @GET
    @Timed
    @Path("{uuid}")
    public void getContent(@PathParam("uuid") String uuid) {

    }
}
