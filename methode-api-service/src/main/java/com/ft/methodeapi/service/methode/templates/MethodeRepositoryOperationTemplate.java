package com.ft.methodeapi.service.methode.templates;

import EOM.Repository;

import com.ft.methodeapi.service.methode.connection.MethodeObjectFactory;
import com.ft.timer.FTTimer;
import com.ft.timer.RunningTimer;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;

/**
 * Encapsulates orchestration required to find, open and close the correct sequence of Methode objects to
 * obtain a Repository and perform operations on it. The operation to be performed is passed as a callback.
 * 
 * If <code>timerClass</code> and <code>timerName</code> are passed into the template constructor, then the
 * operation will be recorded using DropWizard metrics. The IP address for the actual Methode server used
 * is appended to the timer name, to allow distinction between "local" and "remote" data centre Methode calls.
 * 
 * Thread safety is not assured.
 *
 * @author Simon.Gibbs
 */
public class MethodeRepositoryOperationTemplate<T> {
    private final MethodeObjectFactory client;
    private final Class<?> timerClass;
    private final String timerName;
    
    /** Constructor for untimed operations.
     *  @param client the MethodeObjectFactory
     */
    public MethodeRepositoryOperationTemplate(MethodeObjectFactory client) {
        this(client, null, null);
    }

    /** Constructor for operations whose time will be recorded in DropWizard metrics.
     *  @param client the MethodeObjectFactory
     *  @param timerClass the class against which metrics will be recorded
     *  @param timerName the timer name against which metrics will be recorded (partitioned by Methode IP address)
     */
    public MethodeRepositoryOperationTemplate(MethodeObjectFactory client, Class<?> timerClass, String timerName) {
        this.client = client;
        this.timerClass = timerClass;
        this.timerName = timerName;
    }
    
    public T doOperation(RepositoryCallback<T> callback) {
        RunningTimer timerContext = null;
        if ((timerClass != null) && timerName != null) {
            String metricName = String.format("%s@%s", timerName, client.refreshMethodeLocation());
            FTTimer opTimer = FTTimer.newTimer(timerClass, metricName);
            timerContext = opTimer.start();
        }
        
        try {
            ORB orb = client.createOrb();
            try {
                return doOperationWithOrb(orb, callback);
                
            } finally {
                client.maybeCloseOrb(orb);
            }
        }
        finally {
            if (timerContext != null) {
                timerContext.stop();
            }
        }
    }

    private T doOperationWithOrb(ORB orb, RepositoryCallback<? extends T> callback) {
        NamingContextExt namingService = client.createNamingService(orb);
        try {
            return doOperationWithNamingService(namingService, callback);
        } finally {
            client.maybeCloseNamingService(namingService);
        }
    }

    private T doOperationWithNamingService(NamingContextExt namingService, RepositoryCallback<? extends T> callback) {
        Repository repository = client.createRepository(namingService);
        try {

            return callback.doOperation(repository);

        } finally {
            client.maybeCloseRepository(repository);
        }
    }

    public static interface RepositoryCallback<T> extends MethodeCallback<T,Repository> {

        public T doOperation(Repository repository);

    }




}
