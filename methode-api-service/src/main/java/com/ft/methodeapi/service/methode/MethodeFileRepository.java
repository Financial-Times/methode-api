package com.ft.methodeapi.service.methode;

import static com.ft.methodeapi.service.methode.PathHelper.folderIsAncestor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import EOM.File;
import EOM.FileSystemAdmin;
import EOM.FileSystemAdminHelper;
import EOM.FileSystemObject;
import EOM.InvalidURI;
import EOM.ObjectLocked;
import EOM.ObjectNotFound;
import EOM.PermissionDenied;
import EOM.Repository;
import EOM.RepositoryError;
import EOM.Session;
import EOM.Utils;

import com.ft.methodeapi.model.EomFile;
import com.ft.methodeapi.service.methode.templates.MethodeRepositoryOperationTemplate;
import com.ft.methodeapi.service.methode.templates.MethodeSessionOperationTemplate;
import com.google.common.base.Optional;
import com.yammer.metrics.annotation.Timed;

public class MethodeFileRepository {

    private final MethodeObjectFactory client;
    private final MethodeObjectFactory testClient;

    public MethodeFileRepository(MethodeObjectFactory client, MethodeObjectFactory testClient) {
        this.client = client;
        this.testClient = testClient;
    }

    @Timed
    public void ping() {
        new MethodeRepositoryOperationTemplate<>(client).doOperation(new MethodeRepositoryOperationTemplate.RepositoryCallback<Void>() {
            @Override
            public Void doOperation(Repository repository) {
                repository.ping();
                return null;
            }
        });
    }

    @Timed
    public Optional<EomFile> findFileByUuid(final String uuid) {

        final MethodeSessionOperationTemplate<Optional<EomFile>> template = new MethodeSessionOperationTemplate<>(client);

        MethodeSessionOperationTemplate.SessionCallback<Optional<EomFile>> callback;

        callback=new MethodeSessionOperationTemplate.SessionCallback<Optional<EomFile>>() {
            @Override
            public Optional<EomFile> doOperation(Session session, Repository repository) {
				final FileSystemAdmin fileSystemAdmin;
				try {
					fileSystemAdmin = EOM.FileSystemAdminHelper.narrow(session.resolve_initial_references("FileSystemAdmin"));
				} catch (ObjectNotFound | RepositoryError | PermissionDenied e) {
					throw new MethodeException(e);
				}

				String uri = "eom:/uuids/" + uuid;

				FileSystemObject fso;
				Optional<EomFile> foundContent;
				try {
					fso = fileSystemAdmin.get_object_with_uri(uri);

					EOM.File eomFile = EOM.FileHelper.narrow(fso);

					final String typeName = eomFile.get_type_name();
					final byte[] bytes = eomFile.read_all();
					final String attributes = eomFile.get_attributes();
					EomFile content = new EomFile(uuid, typeName, bytes, attributes);
					foundContent = Optional.of(content);

					eomFile._release();
					fileSystemAdmin._release();

				} catch (InvalidURI invalidURI) {
					return Optional.absent();
				} catch (RepositoryError | PermissionDenied e) {
					throw new MethodeException(e);
				}
				return foundContent;
            }
        };

       return template.doOperation(callback);
    }

    private static final String TEST_FOLDER = "/FT Website Production/Z_Test/dyn_pub_test";
    private static final String[] PATH_TO_TEST_FOLDER = Utils.stringToPath(TEST_FOLDER);

    /**
     * WARNING
     * This method is used by smoke tests in every environment including production.
     * It is very important that creating and deleting of content in production methode
     * is restricted to the TEST_FOLDER. If you make changes to the code below (or the
     * methods it calls), please ensure that you do not allow writing or deleting outside this folder.
     */
    public EomFile createNewTestFile(final String filename, final EomFile eomFile) {
        final MethodeSessionOperationTemplate<EomFile> template = new MethodeSessionOperationTemplate<>(testClient);
        final EomFile createdEomFile = template.doOperation(new CreateFileCallback(TEST_FOLDER, filename, eomFile));
        return createdEomFile;
    }

    /**
     * WARNING
     * This method is used by smoke tests in every environment including production.
     * It is very important that creating and deleting of content in production methode
     * is restricted to the TEST_FOLDER. If you make changes to the code below (or the
     * methods it calls), please ensure that you do not allow writing or deleting outside this folder.
     */
    public void deleteTestFileByUuid(final String uuid) {
        final MethodeSessionOperationTemplate<Void> template = new MethodeSessionOperationTemplate<>(testClient);
        template.doOperation(new MethodeSessionOperationTemplate.SessionCallback<Void>() {
            @Override
            public Void doOperation(Session session, Repository repository) {
                final FileSystemAdmin fileSystemAdmin;
                try {
                    fileSystemAdmin = FileSystemAdminHelper.narrow(session.resolve_initial_references("FileSystemAdmin"));
                } catch (ObjectNotFound | RepositoryError | PermissionDenied e) {
                    throw new MethodeException(e);
                }

                final String uri = "eom:/uuids/" + uuid;

                final FileSystemObject fso;
                try {
                    fso = fileSystemAdmin.get_object_with_uri(uri);
                    try {
                        final File eomFile = EOM.FileHelper.narrow(fso);
                        final String[] pathToFile = eomFile.get_path();

                        if(folderIsAncestor(PATH_TO_TEST_FOLDER, pathToFile)) {
                            eomFile.discard();
                        } else {
                            throw new ActionNotPermittedException(String.format("cannot delete %s, it's not in the test folder %s", uuid, TEST_FOLDER));
                        }

                    } finally {
                        fso._release();
                    }
                } catch (InvalidURI e) {
                    throw new NotFoundException(uuid);
                } catch (PermissionDenied | RepositoryError | ObjectLocked e) {
                    throw new MethodeException(e);
                } finally {
                    fileSystemAdmin._release();
                }

                return null;
            }
        });
    }
    
    public Map<String, String> getAssetTypes(final Set<String> assetIdentifiers){
    	final MethodeSessionOperationTemplate<Map<String, String>> template = new MethodeSessionOperationTemplate<>(testClient);
        MethodeSessionOperationTemplate.SessionCallback<Map<String, String>> callback = new MethodeSessionOperationTemplate.SessionCallback<Map<String, String>>() {
        	final Map<String, String> assetTypes = new HashMap<>();
            @Override
            public Map<String, String>  doOperation(Session session, Repository repository) {
                final FileSystemAdmin fileSystemAdmin;
                try {
                    fileSystemAdmin = FileSystemAdminHelper.narrow(session.resolve_initial_references("FileSystemAdmin"));
                } catch (ObjectNotFound | RepositoryError | PermissionDenied e) {
                    throw new MethodeException(e);
                }

                for(String assetId : assetIdentifiers){
        			
                	if(!assetTypes.containsKey(assetId)){
		                final String uri = "eom:/uuids/" + assetId;
		
		                final FileSystemObject fso;
		                try {
		                	fso = fileSystemAdmin.get_object_with_uri(uri);
	                        final File eomFile = EOM.FileHelper.narrow(fso);
	                        final String typeName = eomFile.get_type_name();
	                        assetTypes.put(assetId, typeName);
	                        eomFile._release();
		                } catch (InvalidURI e) {
		                    throw new NotFoundException(assetId);
		                } catch (PermissionDenied | RepositoryError e) {
		                    throw new MethodeException(e);
		                } finally {
		                    fileSystemAdmin._release();
		                }
                	}
                }

                return assetTypes;
            }
        };
		return template.doOperation(callback);
		
	}
    


	public String getClientRepositoryInfo() {
		return String.format("hostname: %s, nsPort: %d, userName: %s", client.getHostname(), client.getPort(), client.getUsername());
	}
}
