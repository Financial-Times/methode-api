package com.ft.methodeapi.service.http;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;

import com.ft.api.jaxrs.errors.ServerError.ServerErrorBuilder;
import com.ft.methodeapi.service.methode.MethodeException;
import com.ft.methodeapi.service.methode.MethodeFileRepository;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.ws.rs.WebApplicationException;

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
    private MethodeFileRepository methodeFileRepository = mock(MethodeFileRepository.class);
    private EomFileResource eomFileResource = new EomFileResource(methodeFileRepository);
    
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

    @Test(expected = WebApplicationException.class)
    public void shouldLogFailureAtError() {
        when(methodeFileRepository.findFileByUuid(anyString())).thenThrow(methodeException());

        try {
            eomFileResource.getByUuid(uuid);
        } finally {
            assertLogEvent("error accessing upstream system", Level.ERROR);
        }
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
