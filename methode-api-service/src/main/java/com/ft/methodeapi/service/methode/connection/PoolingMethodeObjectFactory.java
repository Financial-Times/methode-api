package com.ft.methodeapi.service.methode.connection;

import static com.ft.methodeapi.MethodeApiApplication.METHODE_API_PANIC_GUIDE;

import EOM.FileSystemAdmin;
import EOM.Repository;
import EOM.Session;

import com.codahale.metrics.MetricRegistry;
import com.ft.methodeapi.service.methode.HealthcheckParameters;
import com.ft.methodeapi.service.methode.MethodeException;
import com.ft.timer.FTTimer;
import com.ft.timer.RunningTimer;
import com.google.common.base.Preconditions;
import com.yammer.dropwizard.lifecycle.Managed;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.health.HealthCheck;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stormpot.Config;
import stormpot.LifecycledResizablePool;
import stormpot.PoolException;
import stormpot.Timeout;
import stormpot.QueuePool;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Creating and destroying EOM objects is a performance overhead. This factory uses Stormpot
 * (https://github.com/chrisvest/stormpot) to pool these objects for re-use. The Poolable object, 
 * MethodeConnection, has all required EOM objects.
 * 
 * All create methods return the appropriate object associated with the allocated methodeConnection.
 * 
 * Methods intended for clean up are largely unimplemented here as we just want to return the MethodeConnection
 * to the pool - this is done in the maybeCloseOrb method.
 *
 * @author Simon.Gibbs
 */
public class PoolingMethodeObjectFactory implements MethodeObjectFactory, Managed {

    private static final Logger LOGGER = LoggerFactory.getLogger(PoolingMethodeObjectFactory.class);

    private final FTTimer claimConnectionTimer = FTTimer.newTimer(PoolingMethodeObjectFactory.class, "claim-connection");
    private final FTTimer releaseConnectionTimer = FTTimer.newTimer(PoolingMethodeObjectFactory.class, "release-connection");

    ThreadLocal<MethodeConnection> allocatedConnection = new ThreadLocal<MethodeConnection>() {
        @Override
        protected MethodeConnection initialValue() {

            RunningTimer timer = claimConnectionTimer.start();
            try {
                LOGGER.debug("Claiming MethodeConnection");
                MethodeConnection connection = pool.claim(claimTimeout);
                if(connection==null) {
                    throw new MethodeException("Timeout while claiming MethodeConnection");
                }
                LOGGER.debug("Claimed objects: {}",connection);

                return connection;
            } catch (InterruptedException | PoolException e) {
                throw new MethodeException(e);
            } finally {
                timer.stop();
            }
        }
    };

    private final MethodeObjectFactory implementation;
    private final LifecycledResizablePool<MethodeConnection> pool;
    private final Timeout claimTimeout;

    private final Gauge<Integer> deallocationQueueLength;

    public PoolingMethodeObjectFactory(final MethodeObjectFactory implementation, ExecutorService executorService, PoolConfiguration configuration, MetricRegistry metricRegistry) {

        Preconditions.checkArgument(configuration != null, "Not configured");
        Preconditions.checkArgument(implementation != null, "PoolingMethodeObjectFactory must wrap another MethodeObjectFactory");
        Preconditions.checkArgument(executorService != null, "A scheduling thread pool service is required");

        assert configuration != null; // suppresses warning
        Preconditions.checkArgument(configuration.getSize()>0,"Pool size must be a positive integer");

        this.implementation = implementation;

        final MethodeConnectionAllocator allocator = new MethodeConnectionAllocator(implementation,executorService, configuration.getMethodeStaleConnectionTimeout());

        String name = MetricRegistry.name(MethodeConnectionAllocator.class, implementation.getName(), "length");
        deallocationQueueLength = metricRegistry.register(name, new Gauge<Integer>() {
            @Override
            public Integer getValue() {
                return allocator.getNumberOfConnectionsAwaitingDeallocation();
            }
        });

        Config<MethodeConnection> poolConfig = new Config<MethodeConnection>().setAllocator(allocator);
        poolConfig.setSize(configuration.getSize());

        pool = new QueuePool<>(poolConfig);
        claimTimeout = new Timeout(
                configuration.getTimeout().getQuantity(),
                configuration.getTimeout().getUnit()
            );

    }

    @Override
    public ORB createOrb() {
        return allocatedConnection.get().getOrb();  
    }

    @Override
    public NamingContextExt createNamingService(ORB orb) {
        Preconditions.checkState(orb==this.createOrb()); 
        return allocatedConnection.get().getNamingService();
    }

    @Override
    public Repository createRepository(NamingContextExt namingService) {
        return allocatedConnection.get().getRepository();
    }

    @Override
    public Session createSession(Repository repository) {
        return allocatedConnection.get().getSession();
    }

    @Override
    public FileSystemAdmin createFileSystemAdmin(Session session) {
        return allocatedConnection.get().getFileSystemAdmin();
    }

    @Override
    public void maybeCloseFileSystemAdmin(FileSystemAdmin fileSystemAdmin) {
        // intentionally not implemented - clean up is done via the wrapped methodeObjectFactory, in the MethodeConnectionAllocator
    }

    @Override
    public void maybeCloseSession(Session session) {
        // intentionally not implemented - clean up is done via the wrapped methodeObjectFactory, in the MethodeConnectionAllocator
    }

    @Override
    public void maybeCloseRepository(Repository repository) {
        // intentionally not implemented - clean up is done via the wrapped methodeObjectFactory, in the MethodeConnectionAllocator
    }

    @Override
    public void maybeCloseNamingService(NamingContextExt namingService) {
        // intentionally not implemented - clean up is done via the wrapped methodeObjectFactory, in the MethodeConnectionAllocator
    }

    @Override
    public void maybeCloseOrb(ORB orb) {
    	// this is final clean up call from our Methode...Template classes, so this is where to release the MethodeConnection back to the pool
        RunningTimer timer = releaseConnectionTimer.start();
        try {
            Preconditions.checkState(orb==this.createOrb());
            MethodeConnection connection = allocatedConnection.get();
            LOGGER.debug("Releasing connection from slot: {}", connection);
            connection.release();
            connection.updateTimeSinceLastUsed();

            // Ensure we release the thread local, otherwise we break the contract with the pool
            allocatedConnection.remove();
        } finally {
            timer.stop();
        }
    }

    @Override
    public String getDescription() {
        return String.format("[%d x [%s]]",pool.getTargetSize(),implementation.getDescription());
    }

    @Override
    public void start() throws Exception {
        // not used
    }

    @Override
    public void stop() throws Exception {
        LOGGER.info("Shutdown Requested");
        pool.shutdown().await(new Timeout(20, TimeUnit.SECONDS));
        LOGGER.info("Shutdown Complete");
    }

    @Override
    public List<HealthCheck> createHealthChecks() {
        HealthCheck check = new DeallocationQueueSizeHealthCheck(
                new HealthcheckParameters(String.format("Methode connection deallocation queue (%s)", this.getName()), 3,
                        "Methode API may eventually be unable to fulfil requests.",
                        "Methode API may be overloaded or leaking connections.", METHODE_API_PANIC_GUIDE),
                deallocationQueueLength,pool.getTargetSize());
        return Collections.singletonList(check);
    }

    @Override
    public boolean isPooling() {
        return true;
    }

    @Override
    public String getName() {
        return implementation.getName();
    }

    @Override
    public String getMethodeLocation() {
        return implementation.getMethodeLocation();
    }
}
