package com.ft.methodeapi.service.methode;

import EOM.*;
import com.ft.methodeapi.model.EomFile;
import com.ft.methodeapi.service.methode.templates.MethodeSessionOperationTemplate;

public class CreateFileCallback implements MethodeSessionOperationTemplate.SessionCallback<EomFile> {

    private final String path;
    private final String filename;
    private final EomFile eomFile;


    public CreateFileCallback(String path, String filename, EomFile eomFile) {
        this.path = path;
        this.filename = filename;
        this.eomFile = eomFile;
    }

    @Override
    public EomFile doOperation(Session session, Repository repository) {

        final ObjectTypeAdmin objectTypeAdmin;
        final FileSystemAdmin fileSystemAdmin;
        try {
            objectTypeAdmin = ObjectTypeAdminHelper.narrow(session.resolve_initial_references("ObjectTypeAdmin"));
            fileSystemAdmin = FileSystemAdminHelper.narrow(session.resolve_initial_references("FileSystemAdmin"));

        } catch (ObjectNotFound | RepositoryError | PermissionDenied e) {
            throw new MethodeException(e);
        }

        try {
            final ObjectType objectType = objectTypeAdmin.get_object_type(eomFile.getType());
            final Folder rootFolder = fileSystemAdmin.get_root();

            final Folder folder = findOrCreateFolder(rootFolder, path);

            final File file = folder.create_file(filename, objectType);

            file.write_all(eomFile.getValue());
            file.set_attributes(eomFile.getAttributes());

            final boolean keepCheckedOut = false;
            file.check_in("", keepCheckedOut);

            return new EomFile(file.get_uuid_string(), file.get_type_name(), file.read_all(), file.get_attributes());
        } catch (TypeNotFound typeNotFound) {
            typeNotFound.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (RepositoryError repositoryError) {
            repositoryError.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (PermissionDenied permissionDenied) {
            permissionDenied.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InvalidName invalidName) {
            invalidName.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InvalidForContainer invalidForContainer) {
            invalidForContainer.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (ObjectLocked objectLocked) {
            objectLocked.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (DuplicatedName duplicatedName) {
            duplicatedName.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InvalidType invalidType) {
            invalidType.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (ObjectNotLocked objectNotLocked) {
            objectNotLocked.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InvalidAttributes invalidAttributes) {
            invalidAttributes.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (ObjectNotCheckedOut objectNotCheckedOut) {
            objectNotCheckedOut.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return null;

    }

    private Folder findOrCreateFolder(final Folder rootFolder, final String path) throws RepositoryError, PermissionDenied, InvalidName, InvalidForContainer, ObjectLocked, DuplicatedName {
        final String[] pathSegments = Utils.stringToPath(path);

        Folder folder;
        try {
            folder = FolderHelper.narrow(rootFolder.get_object_with_path(pathSegments));
        } catch (InvalidPath invalidPath) {
            Folder currentFolder = rootFolder;
            for (String pathSegment : pathSegments) {
                try {
                    currentFolder = FolderHelper.narrow(currentFolder.get_object_with_path(new String[]{pathSegment}));
                } catch (InvalidPath invalidPath1) {
                    currentFolder = currentFolder.create_folder(pathSegment);
                }
            }
            folder = currentFolder;
        }

        return folder;
    }
}
