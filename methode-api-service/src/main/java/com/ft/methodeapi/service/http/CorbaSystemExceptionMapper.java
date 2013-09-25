package com.ft.methodeapi.service.http;

import com.ft.methodeapi.model.Message;
import com.google.common.base.Optional;
import org.omg.CORBA.SystemException;
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
public class CorbaSystemExceptionMapper implements ExceptionMapper<SystemException> {

    private static final Logger LOG = LoggerFactory.getLogger(CorbaSystemExceptionMapper.class);

    @Override
    public Response toResponse(SystemException methodeException) {
        LOG.error("unhandled CORBA system exception", methodeException);
        final Message message = new Message(Optional.of("CORBA system error"));
        return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(message)
                .build();
    }
}
