package com.ft.methodeapi.service.methode.connection;

import EOM.FileSystemAdmin;
import EOM.Repository;
import EOM.Session;

import com.yammer.dropwizard.util.Duration;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;

import stormpot.Slot;

import java.util.concurrent.Executors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Check we expose accurate metrics.
 *
 * @author Simon.Gibbs
 */
@RunWith(MockitoJUnitRunner.class)
public class MethodeConnectionAllocatorTest {
	
	MethodeConnectionAllocator methodeConnectionAllocator;
	
	@Mock private MethodeObjectFactory mockMethodeObjectFactory;
	@Mock private ORB mockOrb;
    @Mock private NamingContextExt mockNamingService;
	@Mock private MethodeConnection mockConnection;
    @Mock private Session mockSession;
    @Mock private FileSystemAdmin mockFileSystemAdmin;
    @Mock private Repository mockRepository;
    @Mock private Slot mockSlot;
    
    private MethodeConnection existingMethodeConnection;


    public static final int LAG = 50;
    
    @Before
    public void setup() {
        methodeConnectionAllocator = new MethodeConnectionAllocator(mockMethodeObjectFactory, Executors.newFixedThreadPool(1), Duration.minutes(30));
        existingMethodeConnection = new MethodeConnection(
    			mockSlot, mockOrb, mockNamingService, 
    			mockRepository, mockSession, mockFileSystemAdmin);
        when(mockMethodeObjectFactory.createOrb()).thenReturn(mockOrb);
        when(mockMethodeObjectFactory.createNamingService(mockOrb)).thenReturn(mockNamingService);
        when(mockMethodeObjectFactory.createRepository(mockNamingService)).thenReturn(mockRepository);
        when(mockMethodeObjectFactory.createSession(mockRepository)).thenReturn(mockSession);
        when(mockMethodeObjectFactory.createFileSystemAdmin(mockSession)).thenReturn(mockFileSystemAdmin);
    }
    
    @Test
    public void shouldReturnAMethodeConnectionOnAllocate() throws Exception {
        MethodeConnection methodeConnection = methodeConnectionAllocator.allocate(mockSlot);
        validateMethodeConnectionParameters(methodeConnection);
    }
    
    @Test
    public void shouldCleanupMethodeConnectionOnDeallocate() throws Exception {
    	methodeConnectionAllocator.deallocate(existingMethodeConnection);
    	Thread.sleep(200);
    	verifyCloseWasAttempted();
    }
    
    @Test
    public void shouldReturnALiveMethodeConnectionWithoutReallocating() throws Exception {
    	when(mockSession._non_existent()).thenReturn(false);
    	MethodeConnection returnedMethodeConnection = methodeConnectionAllocator.reallocate(mockSlot, existingMethodeConnection);
    	assertThat(returnedMethodeConnection, equalTo(existingMethodeConnection));
    	verify(mockMethodeObjectFactory, never()).maybeCloseFileSystemAdmin(mockFileSystemAdmin);
    	verify(mockMethodeObjectFactory, never()).maybeCloseNamingService(mockNamingService);
    	verify(mockMethodeObjectFactory, never()).maybeCloseOrb(mockOrb);
    	verify(mockMethodeObjectFactory, never()).maybeCloseRepository(mockRepository);
    	verify(mockMethodeObjectFactory, never()).maybeCloseSession(mockSession);
    }
    
    @Test
    public void shouldDeallocateAndAllocateMethodeConnectionWhenMethodeConnectionHasNotBeenUsedForLongerThanStaleTimeout() throws Exception {
    	existingMethodeConnection.updateTimeSinceLastUsed();
    	methodeConnectionAllocator = new MethodeConnectionAllocator(mockMethodeObjectFactory, Executors.newFixedThreadPool(1), Duration.milliseconds(1));
    	Thread.sleep(10); //so our methode connection is stale
    	MethodeConnection returnedMethodeConnection = methodeConnectionAllocator.reallocate(mockSlot, existingMethodeConnection);
    	Thread.sleep(200);
    	assertThat(returnedMethodeConnection, not(equalTo(existingMethodeConnection)));
    	verifyCloseWasAttempted(); 	
    	validateMethodeConnectionParameters(returnedMethodeConnection);
    }
    
    
    @Test
    public void shouldDeallocateAndAllocateMethodeConnectionWhenSessionIsNonExistent() throws Exception {
    	when(mockSession._non_existent()).thenReturn(true);
    	MethodeConnection returnedMethodeConnection = methodeConnectionAllocator.reallocate(mockSlot, existingMethodeConnection);
    	Thread.sleep(200);
    	assertThat(returnedMethodeConnection, not(equalTo(existingMethodeConnection)));
    	verifyCloseWasAttempted(); 	
    	validateMethodeConnectionParameters(returnedMethodeConnection);
    }
    
    @Test
    public void shouldDeallocateAndAllocateMethodeConnectionWhenSessionNonExistentThrowsException() throws Exception {
    	when(mockSession._non_existent()).thenThrow(new RuntimeException("Session does not exist"));
    	MethodeConnection returnedMethodeConnection = methodeConnectionAllocator.reallocate(mockSlot, existingMethodeConnection);
    	Thread.sleep(200);
    	assertThat(returnedMethodeConnection, not(equalTo(existingMethodeConnection)));
    	verifyCloseWasAttempted(); 	
    	validateMethodeConnectionParameters(returnedMethodeConnection);
    }
    
    @Test
    public void shouldDeallocateAndAllocateAMethodeConnectionWhenSessionHereIAmThrowsException() throws Exception {
    	doThrow(new RuntimeException("MethodeConnection is no longer valid")).when(mockSession).here_i_am();		
    	MethodeConnection returnedMethodeConnection = methodeConnectionAllocator.reallocate(mockSlot, existingMethodeConnection);
    	Thread.sleep(200);
    	assertThat(returnedMethodeConnection, not(equalTo(existingMethodeConnection)));
    	verifyCloseWasAttempted(); 	
    	validateMethodeConnectionParameters(returnedMethodeConnection);
    }
    
    @Test
    public void shouldDeallocateAndAllocateAMethodeConnectionWhenRepositoryPingThrowsException() throws Exception {
    	doThrow(new RuntimeException("MethodeConnection is no longer valid")).when(mockRepository).ping();		
    	MethodeConnection returnedMethodeConnection = methodeConnectionAllocator.reallocate(mockSlot, existingMethodeConnection);
    	Thread.sleep(200);
    	assertThat(returnedMethodeConnection, not(equalTo(existingMethodeConnection)));
    	verifyCloseWasAttempted(); 	
    	validateMethodeConnectionParameters(returnedMethodeConnection);
    }

    // this metric is used in GaugeRangeHealthcheck
    @Test
    public void shouldCorrectlyTrackNumberOfConnectionsAwaitingDeallocation() throws Exception {
        doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) {
                try {
                    Thread.sleep(LAG);
                } catch (InterruptedException e) {
                    // ignore
                }
                return null;
            }})
            .when(mockMethodeObjectFactory).maybeCloseSession(mockSession);

        methodeConnectionAllocator.deallocate(mock(MethodeConnection.class));
        assertThat(methodeConnectionAllocator.getNumberOfConnectionsAwaitingDeallocation(),is(1));

        Thread.sleep(LAG*4);

        assertThat(methodeConnectionAllocator.getNumberOfConnectionsAwaitingDeallocation(),is(0));

    }

	private void validateMethodeConnectionParameters(MethodeConnection methodeConnection) {
		assertThat(methodeConnection.getFileSystemAdmin(), equalTo(mockFileSystemAdmin));
        assertThat(methodeConnection.getNamingService(), equalTo(mockNamingService));
        assertThat(methodeConnection.getOrb(), equalTo(mockOrb));
        assertThat(methodeConnection.getRepository(), equalTo(mockRepository));
        assertThat(methodeConnection.getSession(), equalTo(mockSession));
	}

	private void verifyCloseWasAttempted() {
		verify(mockMethodeObjectFactory).maybeCloseFileSystemAdmin(mockFileSystemAdmin);
    	verify(mockMethodeObjectFactory).maybeCloseNamingService(mockNamingService);
    	verify(mockMethodeObjectFactory).maybeCloseOrb(mockOrb);
    	verify(mockMethodeObjectFactory).maybeCloseRepository(mockRepository);
    	verify(mockMethodeObjectFactory).maybeCloseSession(mockSession);
	}
}
