package com.ft.methodeapi.service.methode;

import static com.ft.methodeapi.service.methode.PathHelper.folderIsAncestor;

import java.util.Map;
import java.util.Set;

import EOM.File;
import EOM.FileSystemAdmin;
import EOM.FileSystemObject;
import EOM.InvalidURI;
import EOM.ObjectLocked;
import EOM.PermissionDenied;
import EOM.Repository;
import EOM.RepositoryError;
import EOM.Utils;

import com.ft.methodeapi.model.EomAssetType;
import com.ft.methodeapi.model.EomFile;
import com.ft.methodeapi.service.methode.connection.MethodeObjectFactory;
import com.ft.methodeapi.service.methode.templates.MethodeFileSystemAdminOperationTemplate;
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
        new MethodeRepositoryOperationTemplate<>(client).doOperation(new MethodeRepositoryOperationTemplate.RepositoryCallback<Object>() {
            @Override
            public Object doOperation(Repository repository) {
                repository.ping();
                return null;
            }
        });
    }

    @Timed
    public Optional<EomFile> findFileByUuid(final String uuid) {

        final MethodeFileSystemAdminOperationTemplate<Optional<EomFile>> template = new MethodeFileSystemAdminOperationTemplate<>(client);

        MethodeFileSystemAdminOperationTemplate.FileSystemAdminCallback<Optional<EomFile>> callback = new MethodeFileSystemAdminOperationTemplate.FileSystemAdminCallback<Optional<EomFile>>() {
            @Override
            public Optional<EomFile> doOperation(FileSystemAdmin fileSystemAdmin) {
				String uri = "eom:/uuids/" + uuid;

				FileSystemObject fso;
				Optional<EomFile> foundContent;
				try {
					fso = fileSystemAdmin.get_object_with_uri(uri);

					EOM.File eomFile = EOM.FileHelper.narrow(fso);

					final String typeName = eomFile.get_type_name();
					final byte[] bytes = eomFile.read_all();
					final String attributes = eomFile.get_attributes();
					final String workflowStatus = eomFile.get_status_name();
					final String systemAttributes = eomFile.get_system_attributes();
					EomFile content = new EomFile(uuid, typeName, bytes, attributes, workflowStatus, systemAttributes);
					foundContent = Optional.of(content);

					eomFile._release();
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
    
    @Timed
    public Map<String, EomAssetType> getAssetTypes(final Set<String> assetIdentifiers){
    	final MethodeFileSystemAdminOperationTemplate<Map<String, EomAssetType>> template = new MethodeFileSystemAdminOperationTemplate<>(client);
		return template.doOperation(new GetAssetTypeFileSystemAdminCallback(assetIdentifiers));
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
        final EomFile createdEomFile = template.doOperation(new CreateFileCallback(testClient, TEST_FOLDER, filename, eomFile));
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
    	final MethodeFileSystemAdminOperationTemplate<Void> template = new MethodeFileSystemAdminOperationTemplate<>(testClient);

        template.doOperation(new MethodeFileSystemAdminOperationTemplate.FileSystemAdminCallback<Void>() {
            @Override
            public Void doOperation(final FileSystemAdmin fileSystemAdmin) {

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
                }

                return null;
            }
        });
    }
    
    

	public String getClientRepositoryInfo() {
		return client.getDescription();
	}
}
