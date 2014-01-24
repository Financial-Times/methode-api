package com.ft.methodeapi.service.methode.templates;

import EOM.FileSystemAdmin;
import EOM.ObjectNotFound;
import EOM.PermissionDenied;
import EOM.RepositoryError;
import EOM.Session;

import com.ft.methodeapi.service.methode.MethodeException;
import com.ft.methodeapi.service.methode.connection.MethodeObjectFactory;
import com.ft.methodeapi.service.methode.templates.MethodeSessionOperationTemplate.SessionCallback;

public class MethodeFileSystemAdminOperationTemplate<T> {
	
	private static final String FILE_SYSTEM_ADMIN = "FileSystemAdmin";
	
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
					fileSystemAdmin = EOM.FileSystemAdminHelper.narrow(session.resolve_initial_references(FILE_SYSTEM_ADMIN));
					
					return fileSystemAdminCallback.doOperation(fileSystemAdmin);
				
				} catch (ObjectNotFound | RepositoryError | PermissionDenied e) {
					
					throw new MethodeException(e);
				
				}finally{
				
					if(fileSystemAdmin !=null)
						fileSystemAdmin._release();
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
