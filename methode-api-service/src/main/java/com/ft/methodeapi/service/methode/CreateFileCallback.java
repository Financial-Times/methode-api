package com.ft.methodeapi.service.methode;

import EOM.*;
import com.ft.methodeapi.model.EomFile;
import com.ft.methodeapi.service.methode.connection.MethodeObjectFactory;
import com.ft.methodeapi.service.methode.templates.MethodeSessionOperationTemplate;

/**
 * WARNING
 * This class is used by smoke tests in every environment including production.
 * It is very important that creating and deleting of content in production methode
 * is restricted to the TEST_FOLDER. If you make changes to the code below (or the
 * methods it calls), please ensure that you do not allow writing or deleting outside this folder.
 */

public class CreateFileCallback implements MethodeSessionOperationTemplate.SessionCallback<EomFile> {

    private final String path;
    private final String filename;
    private final EomFile eomFile;
    private final MethodeObjectFactory methodeObjectFactory;


    public CreateFileCallback(MethodeObjectFactory methodeObjectFactory, String path, String filename, EomFile eomFile) {
        this.methodeObjectFactory = methodeObjectFactory;
        this.path = path;
        this.filename = filename;
        this.eomFile = eomFile;
    }

    @Override
    public EomFile doOperation(Session session) {

        ObjectTypeAdmin objectTypeAdmin = null;
        FileSystemAdmin fileSystemAdmin = null;
        try {
            objectTypeAdmin = ObjectTypeAdminHelper.narrow(session.resolve_initial_references("ObjectTypeAdmin"));
            fileSystemAdmin = methodeObjectFactory.createFileSystemAdmin(session);
        } catch (ObjectNotFound | RepositoryError | PermissionDenied e) {
            methodeObjectFactory.maybeCloseFileSystemAdmin(fileSystemAdmin);
            if(objectTypeAdmin!=null) {
                objectTypeAdmin._release();
            }
            throw new MethodeException(e);
        }

        try {
            final ObjectType objectType = objectTypeAdmin.get_object_type(eomFile.getType());
            final Folder rootFolder = fileSystemAdmin.get_root();

            final Folder folder = findOrCreateFolder(rootFolder, path);

            final File file = folder.create_file(filename, objectType);

            file.write_all(eomFile.getValue());
            file.set_attributes(eomFile.getAttributes());
			file.set_system_attributes(eomFile.getSystemAttributes());

			ObjectAdmin oa = ObjectAdminHelper.narrow(session.resolve_initial_references("ObjectAdmin"));
			oa.set_status_name(file, eomFile.getWorkflowStatus());
			EOM.EventAdmin ea = EOM.EventAdminHelper.narrow(session.resolve_initial_references("EventAdmin"));
			ea.fire_event(file, "set_status");

			final boolean keepCheckedOut = false;
			file.check_in("", keepCheckedOut);

            EomFile eomFile = new EomFile(file.get_uuid_string(), file.get_type_name(), file.read_all(), file.get_attributes(),
                    file.get_status_name(), file.get_system_attributes());

            file._release();
            folder._release();
            ea._release();

			return eomFile;

		} catch (TypeNotFound | RepositoryError | PermissionDenied | InvalidName | InvalidForContainer | ObjectLocked
				| DuplicatedName | ObjectNotLocked | ObjectNotCheckedOut | ObjectNotFound e) {
			throw new MethodeException(e);
		} catch (InvalidAttributes | InvalidType e) {
			throw new InvalidEomFileException("cannot create requested file", e);
		} catch (InvalidStatus invalidStatus) {
			throw new InvalidEomFileException("Invalid workflow status.", invalidStatus);
		} finally {
            methodeObjectFactory.maybeCloseFileSystemAdmin(fileSystemAdmin);
            objectTypeAdmin._release();
        }

	}

    private Folder findOrCreateFolder(final Folder rootFolder, final String path) throws RepositoryError, PermissionDenied, InvalidName, InvalidForContainer, ObjectLocked, DuplicatedName {
        final String[] pathSegments = Utils.stringToPath(path);

        try {
            return FolderHelper.narrow(rootFolder.get_object_with_path(pathSegments));
        } catch (InvalidPath invalidPath) {
            Folder currentFolder = rootFolder;
            for (String pathSegment : pathSegments) {
                Folder parent = currentFolder;
                try {
                    currentFolder = FolderHelper.narrow(parent.get_object_with_path(new String[]{pathSegment}));
                } catch (InvalidPath invalidPath1) {
                    currentFolder = parent.create_folder(pathSegment);
                }

                if(parent!=rootFolder) {
                    parent._release();
                }
            }
            return currentFolder;
        }
    }
}
