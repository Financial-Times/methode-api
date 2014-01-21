package com.ft.methodeapi.service.http;

import static java.lang.String.format;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import com.ft.api.jaxrs.errors.ClientError;
import com.ft.api.jaxrs.errors.ServerError;
import com.ft.methodeapi.model.EomFile;
import com.ft.methodeapi.service.methode.ActionNotPermittedException;
import com.ft.methodeapi.service.methode.InvalidEomFileException;
import com.ft.methodeapi.service.methode.MethodeException;
import com.ft.methodeapi.service.methode.MethodeFileRepository;
import com.ft.methodeapi.service.methode.NotFoundException;
import com.google.common.base.Optional;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.yammer.metrics.annotation.Timed;
import org.apache.commons.lang.StringUtils;
import org.omg.CORBA.SystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Path("eom-file")
@Api(value = "/eom-file", description = "Resource for managing EOM files.")
public class EomFileResource {

	private static final Logger LOGGER = LoggerFactory.getLogger(EomFileResource.class);
	private static final String TRANSACTION_ID_HEADER = "X-Request-Id";

    private final MethodeFileRepository methodeContentRepository;

    public EomFileResource(MethodeFileRepository methodeContentRepository) {
        this.methodeContentRepository = methodeContentRepository;
    }

	@ApiOperation(
	            value = "Returns a EOM file.",
	            notes = "Returns a EOM file by UUID from the configured Methode instance.",
	            response = EomFile.class,
	            responseContainer = "Optional",
	            produces = MediaType.APPLICATION_JSON)
    @GET
    @Timed
    @Path("/{uuid}")
    @Produces(MediaType.APPLICATION_JSON)
    public Optional<EomFile> getByUuid(@PathParam("uuid") String uuid, @Context HttpHeaders httpHeaders) {
		String transactionId = getOrGenerateTransactionId(httpHeaders, uuid, "EOM File requested.");
		try {
			try {
				Optional<EomFile> eomFile = methodeContentRepository.findFileByUuid(uuid);
				LOGGER.info("message=\"File retrieved successfully.\" transaction_id={} uuid={}.", transactionId, uuid);
				return eomFile;
			} catch(MethodeException | SystemException ex) {
				throw ServerError.status(503).error("error accessing upstream system").exception(ex);
			}
		} catch (RuntimeException e) {
			String errorMessage = String.format("message=\"%s.\" transaction_id=%s uuid=%s",
					e.getMessage(), transactionId, uuid);
			LOGGER.error(errorMessage, e);
			throw e;
		}
    }

	@ApiOperation(
	            value = "Writes a new file to Methode.",
	            notes = "Writes the given EOM file to the configured Methode instance, and then returns it.",
	            response = EomFile.class,
	            produces = MediaType.APPLICATION_JSON,
				consumes = MediaType.APPLICATION_JSON)
    @POST
    @Timed
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public EomFile newTestFile(final EomFile eomFile, @Context HttpHeaders httpHeaders) {
		String transactionId = getOrGenerateTransactionId(httpHeaders, eomFile.getUuid(), "Create new test EOM File.");
        final String filename = "test-file-" + System.currentTimeMillis() + ".xml";
        try {
			try {
				EomFile newEomFile = methodeContentRepository.createNewTestFile(filename, eomFile);
				LOGGER.info("message=\"Test file created successfully.\" transaction_id={} uuid={}.", transactionId, newEomFile.getUuid());
				return newEomFile;
			} catch (InvalidEomFileException e) {
				throw ClientError.status(422).exception(e);
			}
		} catch (RuntimeException e) {
			String errorMessage = String.format("message=\"%s.\" transaction_id=%s uuid=%s",
					e.getMessage(), transactionId, eomFile.getUuid());
			LOGGER.error(errorMessage, e);
			throw e;
		}
    }

	@ApiOperation(
	            value = "Deletes an EOM file in Methode.",
	            notes = "Deletes an EOM file by UUID from the configured Methode instance.",
				consumes = MediaType.APPLICATION_JSON)
    @DELETE
    @Timed
    @Path("/{uuid}")
    public void deleteByUuid(@PathParam("uuid") String uuid, @Context HttpHeaders httpHeaders) {
		String transactionId = getOrGenerateTransactionId(httpHeaders, uuid, "Delete EOM file.");
		try {
			try {
				methodeContentRepository.deleteTestFileByUuid(uuid);
				LOGGER.info("message=\"File deleted successfully.\" transaction_id={} uuid={}.", transactionId, uuid);
			} catch (NotFoundException e) {
				throw ClientError.status(404).error(format("%s not found", uuid)).exception();
			} catch (ActionNotPermittedException e) {
				throw ClientError.status(403).error(format("not allowed to delete %s", uuid)).exception(e);
			} catch (MethodeException | SystemException e) {
				throw ServerError.status(503).error("error accessing upstream system").exception(e);
			}
		} catch (RuntimeException e) {
			String errorMessage = String.format("message=\"%s.\" transaction_id=%s uuid=%s",
					e.getMessage(), transactionId, uuid);
			LOGGER.error(errorMessage, e);
			throw e;
		}
    }

	private String getOrGenerateTransactionId(HttpHeaders httpHeaders, String uuid, String message) {
		String transactionId = getHeaderValue(httpHeaders, TRANSACTION_ID_HEADER);
		if (StringUtils.isEmpty(transactionId)) {
			LOGGER.error("Transaction ID ({} header) not found.", TRANSACTION_ID_HEADER);
			throw new IllegalStateException("Transaction ID not found.");
		} else {
			LOGGER.info("message=\"{}\" transaction_id={} uuid={}.", message, transactionId, uuid);
			return transactionId;
		}
	}

	private String getHeaderValue(HttpHeaders httpHeaders, String headerName) {
		List<String> headerValues = httpHeaders.getRequestHeader(headerName);
		if (headerValues == null || headerValues.isEmpty()) {
			return null;
		} else {
			return headerValues.get(0);
		}
	}

}
