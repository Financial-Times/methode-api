package com.ft.methodeapi.service.methode.connection;

import EOM.Repository;
import EOM.Session;
import com.ft.methodeapi.service.methode.MethodeException;
import com.google.common.base.Preconditions;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stormpot.Allocator;
import stormpot.Config;
import stormpot.Slot;
import stormpot.Timeout;
import stormpot.bpool.BlazePool;

import java.util.concurrent.TimeUnit;

/**
 * PoolingMethodeObjectFactory
 *
 * @author Simon.Gibbs
 */
public class PoolingMethodeObjectFactory implements MethodeObjectFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(PoolingMethodeObjectFactory.class);

    ThreadLocal < MethodeContext > allocatedContext = new ThreadLocal<MethodeContext>() {
        @Override
        protected MethodeContext initialValue() {
            try {
                return pool.claim(timeout);
            } catch (InterruptedException e) {
                throw new MethodeException(e);
            }
        }
    };

    private final MethodeObjectFactory implementation;
    private final BlazePool<MethodeContext> pool;
    private final Timeout timeout;


    public PoolingMethodeObjectFactory(final MethodeObjectFactory implementation, int poolSize) {
        Allocator<MethodeContext> allocator = new Allocator<MethodeContext>() {

            @Override
            public MethodeContext allocate(Slot slot) throws Exception {

                LOGGER.info("Allocating MethodeContext");

                ORB orb = implementation.createOrb();
                NamingContextExt namingService = implementation.createNamingService(orb);
                Repository repository = implementation.createRepository(namingService);
                Session session = implementation.createSession(repository);

                return new MethodeContext(slot, orb, namingService, repository, session);
            }

            @Override
            public void deallocate(MethodeContext methodeContext) throws Exception {
                implementation.maybeCloseSession(methodeContext.getSession());
                implementation.maybeCloseRepository(methodeContext.getRepository());
                implementation.maybeCloseNamingService(methodeContext.getNamingService());
                implementation.maybeCloseOrb(methodeContext.getOrb());

                LOGGER.info("Deallocated MethodeContext");

            }
        };

        Config<MethodeContext> config = new Config<MethodeContext>().setAllocator(allocator);
        config.setSize(poolSize);

        pool = new BlazePool<>(config);
        timeout = new Timeout(10, TimeUnit.SECONDS);
        this.implementation = implementation;

    }

    @Override
    public ORB createOrb() {
        return allocatedContext.get().getOrb();  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public NamingContextExt createNamingService(ORB orb) {
        Preconditions.checkState(orb==this.createOrb());
        return allocatedContext.get().getNamingService();
    }

    @Override
    public Repository createRepository(NamingContextExt namingService) {
        return allocatedContext.get().getRepository();
    }

    @Override
    public Session createSession(Repository repository) {
        return allocatedContext.get().getSession();
    }

    @Override
    public void maybeCloseSession(Session session) {
        // deferred to allocator
    }

    @Override
    public void maybeCloseRepository(Repository repository) {
        // deferred to allocator
    }

    @Override
    public void maybeCloseNamingService(NamingContextExt namingService) {
        // deferred to allocator
    }

    @Override
    public void maybeCloseOrb(ORB orb) {
        Preconditions.checkState(orb==this.createOrb());
        allocatedContext.get().release();
        allocatedContext.remove();
    }

    @Override
    public String getDescription() {
        return String.format("[%d x [%s]]",pool.getTargetSize(),implementation.getDescription());
    }
}
