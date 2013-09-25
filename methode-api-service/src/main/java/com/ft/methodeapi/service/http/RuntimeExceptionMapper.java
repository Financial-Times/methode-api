package com.ft.methodeapi.service.http;


import com.ft.methodeapi.model.Message;
import com.google.common.base.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class RuntimeExceptionMapper implements ExceptionMapper<RuntimeException> {

    private static final Logger LOG = LoggerFactory.getLogger(RuntimeExceptionMapper.class);

    // TODO resolve copy and past of this class across all three components.

    @Override
    public Response toResponse(RuntimeException re) {

        if (re instanceof WebApplicationException) {
            WebApplicationException wae = (WebApplicationException) re;
            return handleWebApplicationException(wae);
        } else {
            return handleRuntimeException(re);
        }

    }

    private Response handleRuntimeException(RuntimeException re) {
        LOG.error("unhandled exception", re);
        final Message message = new Message(Optional.of("server error"));
        return Response.serverError()
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(message)
                .build();
    }

    private Response handleWebApplicationException(WebApplicationException wae) {
        final String exceptionMessage = wae.getMessage();
        final Message message = new Message(Optional.fromNullable(exceptionMessage));

        return Response.fromResponse(wae.getResponse())
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(message).build();
    }

}
