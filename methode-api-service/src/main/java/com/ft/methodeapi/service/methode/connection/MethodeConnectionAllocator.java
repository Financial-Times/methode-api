package com.ft.methodeapi.service.methode.connection;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import org.omg.CORBA.ORB;
import org.omg.CORBA.TIMEOUT;
import org.omg.CORBA.TRANSIENT;
import org.omg.CosNaming.NamingContextExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stormpot.Reallocator;
import stormpot.Slot;
import EOM.FileSystemAdmin;
import EOM.Repository;
import EOM.Session;

import com.ft.timer.FTTimer;
import com.ft.timer.RunningTimer;
import com.yammer.dropwizard.util.Duration;

/**
 * Responsible for EOM object creation and clean up. 
 * 
 * The reallocate method allows us to avoid expensive recreation of objects if the connection
 * is still fine.
 *
 * @author Simon.Gibbs
 */
public class MethodeConnectionAllocator implements Reallocator<MethodeConnection> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodeConnectionAllocator.class);

	// if the methode connection isn't used for a long time (configurable), it's very likely to be stale anyway, so shortcircuit any checks on staleness
    private final Duration methodeStaleConnectionTimeout;

    private final FTTimer allocationTimer = FTTimer.newTimer(MethodeConnectionAllocator.class, "allocate-connection");
    private final FTTimer deallocationTimer = FTTimer.newTimer(MethodeConnectionAllocator.class, "deallocate-connection");
    private final FTTimer reallocationTimer = FTTimer.newTimer(MethodeConnectionAllocator.class,"reallocate-connection");


    private final MethodeObjectFactory implementation;
    private final ExecutorService executorService;

    private AtomicInteger numberOfConnectionsAwaitingDeallocation = new AtomicInteger(0);

    public MethodeConnectionAllocator(MethodeObjectFactory implementation, ExecutorService executorService, Duration methodeStaleConnectionTimeout) {
        this.implementation = implementation;
        this.executorService = executorService;
        this.methodeStaleConnectionTimeout = methodeStaleConnectionTimeout;
    }

    @Override
    public MethodeConnection allocate(Slot slot) throws Exception {

        RunningTimer timer = allocationTimer.start();
        ORB orb = null;
        try {
            orb = implementation.createOrb();
            NamingContextExt namingService = implementation.createNamingService(orb);
            Repository repository = implementation.createRepository(namingService);
            Session session = implementation.createSession(repository);
            FileSystemAdmin fileSystemAdmin = implementation.createFileSystemAdmin(session);

            MethodeConnection connection = new MethodeConnection(slot, orb, namingService, repository, session, fileSystemAdmin);
            LOGGER.debug("Allocated objects: {}",connection.toString());
            return connection;

        } catch (TIMEOUT | TRANSIENT se) {
            LOGGER.debug("Corba error",se);
        	LOGGER.warn("Corba error: {}", se.getClass().getSimpleName());

            implementation.maybeCloseOrb(orb);

            // Adds a timestamp
            throw new RecoverableAllocationException(se);

        } catch (Error error) {
            implementation.maybeCloseOrb(orb);
            LOGGER.error("Fatal error detected",error);
            throw error;
        } catch (Throwable e) {
        	implementation.maybeCloseOrb(orb);
        	LOGGER.error(e.getMessage(), e); // logging here because Stormpot will poison this connection and swallow the exception without logging it
        	throw e;
        } finally {
            timer.stop();
        }
    }

    @Override
    public void deallocate(final MethodeConnection connection) throws Exception {

        numberOfConnectionsAwaitingDeallocation.incrementAndGet();
        executorService.submit(new Runnable() {
            @Override
            public void run() {

                RunningTimer timer = deallocationTimer.start();
                try {
                    implementation.maybeCloseFileSystemAdmin(connection.getFileSystemAdmin());
                    implementation.maybeCloseSession(connection.getSession());
                    implementation.maybeCloseRepository(connection.getRepository());
                    implementation.maybeCloseNamingService(connection.getNamingService());
                    implementation.maybeCloseOrb(connection.getOrb());

                    LOGGER.debug("Requested deallocation of objects: {}",connection.toString());
                } catch (Error error) {
                    LOGGER.error("Fatal error detected",error);
                    throw error;
                } finally {
                    timer.stop();
                    numberOfConnectionsAwaitingDeallocation.decrementAndGet();
                }
            }
        });


    }

	@Override
	public MethodeConnection reallocate(Slot slot, MethodeConnection connection) throws Exception {
		LOGGER.debug("Starting reallocation for slot {} and connection {}", slot, connection);
        RunningTimer timer = reallocationTimer.start();
        
		try {        
	        
	        if (connectionIsStale(connection)) {
	        	return replaceConnection(slot, connection);
	        }
			
            connection.getRepository().ping(); // this throws for example an org.omg.CORBA.COMM_FAILURE exception on failure
            Session session = connection.getSession();
            if(session._non_existent()) {
                LOGGER.info("Session is gone");
                return replaceConnection(slot, connection);
            }
            session.here_i_am(); // throws exception if session isn't here any more
        } catch (Throwable e) {
        	LOGGER.info("Methode connection {} is no longer valid, expiring it", connection, e);
        	return replaceConnection(slot, connection);
        } finally {
            timer.stop();
        }
        return connection;
	}

	private MethodeConnection replaceConnection(Slot slot, MethodeConnection connection) throws Exception {
		deallocate(connection);
		return allocate(slot);
	}

    private boolean connectionIsStale(MethodeConnection connection) {
    	Duration durationSinceLastUsed = connection.getDurationSinceLastUsed();
    	long countSinceLastUsed = methodeStaleConnectionTimeout.getUnit().convert(durationSinceLastUsed.getQuantity(), durationSinceLastUsed.getUnit());

        return countSinceLastUsed > methodeStaleConnectionTimeout.getQuantity();
	}

	public int getNumberOfConnectionsAwaitingDeallocation() {
        return numberOfConnectionsAwaitingDeallocation.get();
    }

}
