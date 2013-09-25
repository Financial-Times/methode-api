package com.ft.methodeapi.service.http;

import com.ft.methodeapi.model.Message;
import com.ft.methodeapi.service.methode.MethodeException;
import com.google.common.base.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

/**
 * MethodeExceptionMapper
 *
 * @author Simon.Gibbs
 */
public class MethodeExceptionMapper implements ExceptionMapper<MethodeException> {

    private static final Logger LOG = LoggerFactory.getLogger(MethodeExceptionMapper.class);

    @Override
    public Response toResponse(MethodeException methodeException) {
        LOG.error("unhandled methode checked exception", methodeException);
        final Message message = new Message(Optional.of("methode access error"));
        return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(message)
                .build();
    }
}
