package com.ft.methodeapi.service.methode.connection;

import EOM.FileSystemAdmin;
import EOM.Repository;
import EOM.Session;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;

import java.util.concurrent.Executors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;


/**
 * Check we expose accurate metrics.
 *
 * @author Simon.Gibbs
 */
@RunWith(MockitoJUnitRunner.class)
public class MethodeConnectionAllocatorTest {


    public static final int LAG = 50;

    @Test
    public void shouldIncrementAndDecrementCounterForGauge() throws Exception {
        MethodeConnectionAllocator allocator = new MethodeConnectionAllocator(slowFailingObjectFactory, Executors.newFixedThreadPool(1));
        allocator.deallocate(mock(MethodeConnection.class));
        assertThat(allocator.getQueueSize(),is(1));

        Thread.sleep(LAG*4);

        assertThat(allocator.getQueueSize(),is(0));

    }


    MethodeObjectFactory slowFailingObjectFactory = new MethodeObjectFactory() {
        @Override
        public FileSystemAdmin createFileSystemAdmin(Session session) {
            return null;
        }

        @Override
        public Session createSession(Repository repository) {
            return null;
        }

        @Override
        public NamingContextExt createNamingService(ORB orb) {
            return null;
        }

        @Override
        public void maybeCloseNamingService(NamingContextExt namingService) {

        }

        @Override
        public Repository createRepository(NamingContextExt namingService) {
            return null;
        }

        @Override
        public ORB createOrb() {
            return null;
        }

        @Override
        public void maybeCloseFileSystemAdmin(FileSystemAdmin fileSystemAdmin) {

        }

        @Override
        public void maybeCloseSession(Session session) {
            try {
                Thread.sleep(LAG);
            } catch (InterruptedException e) {
                // ignore
            }
        }

        @Override
        public void maybeCloseOrb(ORB orb) {

        }

        @Override
        public void maybeCloseRepository(Repository repository) {

        }

        @Override
        public String getDescription() {
            return "Mock";
        }
    };

}
