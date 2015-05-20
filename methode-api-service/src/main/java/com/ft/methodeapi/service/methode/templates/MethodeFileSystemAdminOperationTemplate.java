package com.ft.methodeapi.service.methode.templates;

import EOM.FileSystemAdmin;
import EOM.Session;

import com.ft.methodeapi.service.methode.connection.MethodeObjectFactory;
import com.ft.methodeapi.service.methode.templates.MethodeSessionOperationTemplate.SessionCallback;

public class MethodeFileSystemAdminOperationTemplate<T> {
	private final MethodeObjectFactory methodeObjectFactory;
    private final Class<?> timerClass;
    private final String timerName;

    /** Constructor for untimed operations.
     *  @param client the MethodeObjectFactory
     */
    public MethodeFileSystemAdminOperationTemplate(MethodeObjectFactory client) {
        this(client, null, null);
    }
    
    /** Constructor for operations whose time will be recorded in DropWizard metrics.
     *  @param client the MethodeObjectFactory
     *  @param timerClass the class against which metrics will be recorded
     *  @param timerName the timer name against which metrics will be recorded (partitioned by Methode IP address)
     */
    public MethodeFileSystemAdminOperationTemplate(MethodeObjectFactory client, Class<?> timerClass, String timerName) {
        this.methodeObjectFactory = client;
        this.timerClass = timerClass;
        this.timerName = timerName;
    }
	
	public T doOperation(final FileSystemAdminCallback<T> fileSystemAdminCallback){
		final SessionCallback<T> callback = new SessionCallback<T>() {

			@Override
			public T doOperation(Session session) {
				FileSystemAdmin fileSystemAdmin = null;
				try {
					fileSystemAdmin = methodeObjectFactory.createFileSystemAdmin(session);

					return fileSystemAdminCallback.doOperation(fileSystemAdmin);
				
				} finally{
                    methodeObjectFactory.maybeCloseFileSystemAdmin(fileSystemAdmin);
				}
			}
        };
		
		MethodeSessionOperationTemplate<T> template =
		        new MethodeSessionOperationTemplate<>(methodeObjectFactory, timerClass, timerName);
		
		return template.doOperation(callback);
	}
	
	public interface FileSystemAdminCallback<T> extends MethodeCallback<T,FileSystemAdmin> {
		T doOperation(FileSystemAdmin fileSystemAdmin);
    }


}
