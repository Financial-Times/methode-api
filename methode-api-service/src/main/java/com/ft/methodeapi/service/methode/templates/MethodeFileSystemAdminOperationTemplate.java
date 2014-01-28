package com.ft.methodeapi.service.methode.templates;

import EOM.FileSystemAdmin;
import EOM.Session;

import com.ft.methodeapi.service.methode.connection.MethodeObjectFactory;
import com.ft.methodeapi.service.methode.templates.MethodeSessionOperationTemplate.SessionCallback;

public class MethodeFileSystemAdminOperationTemplate<T> {
	

	
	private final MethodeObjectFactory methodeObjectFactory;

    public MethodeFileSystemAdminOperationTemplate(MethodeObjectFactory client) {
        this.methodeObjectFactory = client;
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
		
		
		MethodeSessionOperationTemplate<T> template = new MethodeSessionOperationTemplate<>(methodeObjectFactory);
		return template.doOperation(callback);
	}
	
	public interface FileSystemAdminCallback<T>{
		T doOperation(FileSystemAdmin fileSystemAdmin);
	}


}
