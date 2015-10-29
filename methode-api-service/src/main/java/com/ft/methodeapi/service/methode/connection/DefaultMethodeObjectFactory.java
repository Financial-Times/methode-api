package com.ft.methodeapi.service.methode.connection;

import EOM.FileSystemAdmin;
import EOM.ObjectNotFound;
import EOM.PermissionDenied;
import EOM.Repository;
import EOM.RepositoryError;
import EOM.RepositoryHelper;
import EOM.RepositoryPackage.InvalidLogin;
import EOM.Session;

import com.ft.methodeapi.service.methode.MethodeException;
import com.yammer.metrics.Metrics;
import com.codahale.metrics.health.HealthCheck;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.core.TimerContext;

import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.NotFound;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;
import java.util.Properties;


/**
 * Encapsulates logic to create and destroy Methode objects and holds the connection and credential details.
 *
 * @author Simon.Gibbs
 */
public class DefaultMethodeObjectFactory implements MethodeObjectFactory {

    private static final String FILE_SYSTEM_ADMIN = "FileSystemAdmin";

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultMethodeObjectFactory.class);
    private static final String CORBA_LOC = "NS=corbaloc:iiop:%s:%d/NameService";
    
    private final String username;
    private final String password;
    private final String orbClass;
    private String name;
    private final String hostname;
	private final int port;
	private final int connectionTimeout;

    private final MetricsRegistry metricsRegistry = Metrics.defaultRegistry();

    private final Timer createFileSystemAdminTimer = metricsRegistry.newTimer(DefaultMethodeObjectFactory.class, "create-file-system-admin");
    private final Timer closeFileSystemAdminTimer = metricsRegistry.newTimer(DefaultMethodeObjectFactory.class, "close-file-system-admin");

    private final Timer createSessionTimer = metricsRegistry.newTimer(DefaultMethodeObjectFactory.class, "create-methode-session");
    private final Timer closeSessionTimer = metricsRegistry.newTimer(DefaultMethodeObjectFactory.class, "close-methode-session");

    private final Timer createNamingServiceTimer = metricsRegistry.newTimer(DefaultMethodeObjectFactory.class, "create-naming-service");
    private final Timer closeNameServiceTimer = metricsRegistry.newTimer(DefaultMethodeObjectFactory.class, "close-naming-service");

    private final Timer createOrbTimer = metricsRegistry.newTimer(DefaultMethodeObjectFactory.class, "create-orb");
    private final Timer closeOrbTimer = metricsRegistry.newTimer(DefaultMethodeObjectFactory.class, "close-orb");

    private final Timer createRepositoryTimer = metricsRegistry.newTimer(DefaultMethodeObjectFactory.class, "create-repository");
    private final Timer closeRepositoryTimer = metricsRegistry.newTimer(DefaultMethodeObjectFactory.class, "close-repository");
    
    private String hostIP;
    
    
    public DefaultMethodeObjectFactory(String name, String hostname, int port, String username, String password, int connectionTimeout, String orbClass) {
        this.name = name;
        this.hostname = hostname;
		this.port = port;
        this.username = username;
        this.password = password;
		this.connectionTimeout = connectionTimeout;
        this.orbClass = orbClass;
    }

    public DefaultMethodeObjectFactory(MethodeObjectFactoryBuilder builder) {
        this(builder.name, builder.host, builder.port, builder.username, builder.password, builder.connectionTimeout, builder.orbClass);
    }

    @Override
    public FileSystemAdmin createFileSystemAdmin(Session session) {

        final TimerContext timerContext = createFileSystemAdminTimer.time();
        FileSystemAdmin fileSystemAdmin = null;
        try {
           fileSystemAdmin = EOM.FileSystemAdminHelper.narrow(session.resolve_initial_references(FILE_SYSTEM_ADMIN));
        } catch (ObjectNotFound | RepositoryError | PermissionDenied e) {
        	throw new MethodeException("Failed to create file system admin", e);
        } finally {
            timerContext.stop();
        }
        return fileSystemAdmin;
    }

    @Override
    public Session createSession(Repository repository) {
        Session session;
        final TimerContext timerContext = createSessionTimer.time();
        try {
            session = repository.login(username, password, "", null);
        } catch (InvalidLogin | RepositoryError e) {
            throw new MethodeException("Failed to create session", e);
        } finally {
            timerContext.stop();
        }
        return session;
    }

    @Override
    public NamingContextExt createNamingService(ORB orb) {
        final TimerContext timerContext = createNamingServiceTimer.time();
        try {
            return NamingContextExtHelper.narrow(orb.resolve_initial_references("NS"));
        } catch (InvalidName invalidName) {
            throw new MethodeException("Failed to create naming service", invalidName);
        } finally {
            timerContext.stop();
        }
    }

    @Override
    public Repository createRepository(NamingContextExt namingService) {
      final TimerContext timerContext = createRepositoryTimer.time();
      try {
            return RepositoryHelper.narrow(namingService.resolve_str("EOM/Repositories/cms2"));
      } catch (org.omg.CosNaming.NamingContextPackage.InvalidName
              | CannotProceed | NotFound e) {
    	  throw new MethodeException("Failed to create repository", e);
      } finally {
          timerContext.stop();
      }
    }

    @Override
    public ORB createOrb() {
        final TimerContext timerContext = createOrbTimer.time();
        try {
            String orbInitRef = refreshMethodeLocation();
            
            String[] orbInits = {"-ORBInitRef", orbInitRef};
            Properties properties = new Properties();
            properties.setProperty("org.omg.CORBA.ORBClass", orbClass);
            properties.setProperty("jacorb.connection.client.connect_timeout", "" + connectionTimeout);
            
            return ORB.init(orbInits, properties);
        } finally {
            timerContext.stop();
        }
    }

    @Override
    public void maybeCloseFileSystemAdmin(FileSystemAdmin fileSystemAdmin) {
        if(fileSystemAdmin !=null) {
            final TimerContext timerContext = closeFileSystemAdminTimer.time();
            try {
                fileSystemAdmin._release();
            } catch(Exception e) {
                LOGGER.warn("Failed to release EOM.FileSystemAdmin", e);
            } finally {
                timerContext.stop();
            }
        }
    }

    @Override
    public void maybeCloseNamingService(NamingContextExt namingService) {
        if (namingService != null) {
            final TimerContext timerContext = closeNameServiceTimer.time();
            try {
                namingService._release();
            } finally {
                timerContext.stop();
            }
        }
    }

    @Override
    public void maybeCloseSession(Session session) {
        if (session != null) {
            final TimerContext timerContext = closeSessionTimer.time();
            try {
                try {
                    session.destroy();
                } catch (Exception e) {
                    LOGGER.warn("Failed to destroy EOM.Session", e);
                }
                try {
                    session._release();
                } catch (Exception e) {
                    LOGGER.warn("Failed to release EOM.Session", e);
                }
            } finally {
                timerContext.stop();
            }
        }
    }

    @Override
    public void maybeCloseOrb(ORB orb) {
        if (orb != null) {
            final TimerContext timerContext = closeOrbTimer.time();
            try {
                orb.destroy();
            } catch (Exception e) {
                LOGGER.warn("Failed to destroy ORB", e);
            } finally {
                timerContext.stop();
            }
        }
    }

    @Override
    public void maybeCloseRepository(Repository repository) {
        if(repository!=null) {
            final TimerContext timerContext = closeRepositoryTimer.time();
            try {
                repository._release();
            } catch (Exception e) {
                LOGGER.warn("Failed to release EOM.Repository", e);
            } finally {
                timerContext.stop();
            }
        }
    }

	public String getHostname() {
		return hostname;
	}

	public int getPort() {
		return port;
	}

	public String getUsername() {
		return username;
	}

    @Override
    public String getDescription() {
        return String.format("hostname: %s, nsPort: %d, userName: %s", getHostname(), getPort(), getUsername());
    }

    @Override
    public List<HealthCheck> createHealthChecks() {
        return Collections.emptyList();
    }

    @Override
    public boolean isPooling() {
        return false;
    }

    public String getName() {
        return name;
    }

    @Override
    public String getMethodeLocation() {
        return hostIP;
    }
    
    private String refreshMethodeLocation() {
        LOGGER.info("Refresh Methode IP for hostname: {}", hostname);
        
        String orbInitRef = null;
        try {
            hostIP = InetAddress.getByName(hostname).getHostAddress();
            LOGGER.info("MethodeAPI is calling host IP: {}", hostIP);
            orbInitRef = String.format(CORBA_LOC, hostIP, port);
        }
        catch (UnknownHostException e) {
            LOGGER.error(String.format("unable to resolve methode host %s", hostname), e);
            orbInitRef = String.format(CORBA_LOC, hostname, port);
        }
        
        return orbInitRef;
    }
}
