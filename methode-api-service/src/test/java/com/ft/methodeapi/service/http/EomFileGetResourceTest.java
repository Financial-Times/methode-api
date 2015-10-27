package com.ft.methodeapi.service.http;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;

import com.ft.api.jaxrs.errors.ServerError.ServerErrorBuilder;
import com.ft.api.util.transactionid.TransactionIdUtils;
import com.ft.methodeapi.service.methode.MethodeException;
import com.ft.methodeapi.service.methode.MethodeFileRepository;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.sun.jersey.api.client.ClientResponse;

import io.dropwizard.testing.junit.ResourceTestRule;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.atLeastOnce;

/**
 * EomFileGetResourceTest
 *
 * @author Simon.Gibbs
 */
public class EomFileGetResourceTest {
    private final MethodeFileRepository methodeFileRepository = mock(MethodeFileRepository.class);
    
    @Rule
    public final ResourceTestRule resources = ResourceTestRule.builder()
            .addResource(new EomFileResource(methodeFileRepository))
            .build();

    ch.qos.logback.classic.Logger logger;
    Level logLevel;
    
    Appender<ILoggingEvent> mockAppender;

    private String uuid;

    @SuppressWarnings("unchecked")
    @Before
    public void setupMockAppender() {
        logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(ServerErrorBuilder.class);

        mockAppender = mock(Appender.class);
        when(mockAppender.getName()).thenReturn("MOCK");
        logger.addAppender(mockAppender);
        logLevel = logger.getLevel();
        logger.setLevel(Level.DEBUG);
        
        uuid = UUID.randomUUID().toString();
    }

    @After
    public void tearDown() {
        logger.detachAppender(mockAppender);
        if (logLevel != null) {
            logger.setLevel(logLevel);
        }
    }

    @Test
    public void shouldLogFailureAtError() {
        when(methodeFileRepository.findFileByUuid(anyString())).thenThrow(methodeException());

        try {
            doSimpleGet();
        } finally {
            assertLogEvent("error accessing upstream system", Level.ERROR);
        }
    }

    private void doSimpleGet() {
        final ClientResponse clientResponse = resources.client().resource("/eom-file/").path(uuid).header(TransactionIdUtils.TRANSACTION_ID_HEADER, "tid_test").get(ClientResponse.class);
        clientResponse.getEntity(String.class);
    }

    private void assertLogEvent(final String keyPhrase, Level level) {
        ArgumentCaptor<LoggingEvent> argument = ArgumentCaptor.forClass(LoggingEvent.class);
        verify(mockAppender,atLeastOnce()).doAppend(argument.capture());

        List<LoggingEvent> values = argument.getAllValues();
        
        Iterable<LoggingEvent> matches = Iterables.filter(values, new Predicate<LoggingEvent>() {
            @Override
            public boolean apply(@Nullable LoggingEvent input) {
                if(input==null) {
                    return false;
                }
                
                return input.getMessage().contains(keyPhrase);
            }
        });
        
        LoggingEvent match = matches.iterator().next();
        
        assertThat(match.getLevel(), is(level));
    }

    private MethodeException methodeException() {
        return new MethodeException(new RuntimeException());
    }
}
