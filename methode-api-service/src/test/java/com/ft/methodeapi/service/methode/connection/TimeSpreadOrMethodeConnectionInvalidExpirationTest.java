package com.ft.methodeapi.service.methode.connection;


import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import stormpot.SlotInfo;
import EOM.Session;


@RunWith(MockitoJUnitRunner.class)
public class TimeSpreadOrMethodeConnectionInvalidExpirationTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    
    TimeSpreadOrMethodeConnectionInvalidExpiration timeSpreadExpiration;

    @Mock private SlotInfo<MethodeConnection> info;
    @Mock private MethodeConnection connection;
    @Mock private Session session;
    
	
	@Before
	public void setup() {
		when(info.getPoolable()).thenReturn(connection);
		when(connection.getSession()).thenReturn(session);
	}
	
	@Test
	public void shouldFailIfLowerBoundLessThan1() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(equalTo("The lower bound cannot be less than 1."));
        timeSpreadExpiration = new TimeSpreadOrMethodeConnectionInvalidExpiration(0, 10, TimeUnit.SECONDS);
	}
	
	@Test
	public void shouldFailIfUpperBoundLessThanLowerBound() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(equalTo("The upper bound must be greater than the lower bound."));
        timeSpreadExpiration = new TimeSpreadOrMethodeConnectionInvalidExpiration(10, 0, TimeUnit.SECONDS);
	}

	@Test
	public void shouldFailIfTimeUnitIsNull() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(equalTo("The TimeUnit cannot be null."));
        timeSpreadExpiration = new TimeSpreadOrMethodeConnectionInvalidExpiration(1, 10, null);
	}
	
	@Test
	public void shouldNotExpireIfHereIAmSuccessfulAndAgeLessThanExpirationAge() {
		when(info.getAgeMillis()).thenReturn(1999L);
		timeSpreadExpiration = new TimeSpreadOrMethodeConnectionInvalidExpiration(2, 10, TimeUnit.SECONDS);
		assertThat(timeSpreadExpiration.hasExpired(info), equalTo(false));
	}
	
	@Test
	public void shouldExpireWhenAgeGreaterThanUpperBoundForAge() {
		when(info.getAgeMillis()).thenReturn(10001L);
		timeSpreadExpiration = new TimeSpreadOrMethodeConnectionInvalidExpiration(2, 10, TimeUnit.SECONDS);
		assertThat(timeSpreadExpiration.hasExpired(info), equalTo(true));
	}
	
	
	@Test
	public void shouldExpireWhenHereIAmThrowsException() {
		when(info.getAgeMillis()).thenReturn(5000L);
		timeSpreadExpiration = new TimeSpreadOrMethodeConnectionInvalidExpiration(2, 10, TimeUnit.SECONDS);
		doThrow(new RuntimeException("MethodeConnection is no longer valid")).when(session).here_i_am();
		assertThat(timeSpreadExpiration.hasExpired(info), equalTo(true));
	}
	
}
