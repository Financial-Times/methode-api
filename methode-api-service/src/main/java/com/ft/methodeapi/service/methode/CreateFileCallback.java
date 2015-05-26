package com.ft.methodeapi.service.methode;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import EOM.DuplicatedName;
import EOM.EventAdmin;
import EOM.File;
import EOM.FileSystemAdmin;
import EOM.Folder;
import EOM.FolderHelper;
import EOM.InvalidAttributes;
import EOM.InvalidForContainer;
import EOM.InvalidName;
import EOM.InvalidPath;
import EOM.InvalidStatus;
import EOM.InvalidType;
import EOM.ObjectAdmin;
import EOM.ObjectAdminHelper;
import EOM.ObjectLocked;
import EOM.ObjectNotCheckedOut;
import EOM.ObjectNotFound;
import EOM.ObjectNotLocked;
import EOM.ObjectType;
import EOM.ObjectTypeAdmin;
import EOM.ObjectTypeAdminHelper;
import EOM.PermissionDenied;
import EOM.RepositoryError;
import EOM.Session;
import EOM.TypeNotFound;
import EOM.Utils;
import EOM._Object;

import com.eidosmedia.wa.render.EomDbHelper;
import com.eidosmedia.wa.render.EomDbHelperFactory;
import com.eidosmedia.wa.render.WebTypes;
import com.eidosmedia.wa.util.Dwp;
import com.eidosmedia.wa.util.DwpLink;
import com.eidosmedia.wa.util.EomDb;
import com.eidosmedia.wa.util.EomDbObject;


import com.ft.methodeapi.model.EomFile;
import com.ft.methodeapi.model.LinkedObject;
import com.ft.methodeapi.service.methode.connection.MethodeObjectFactory;
import com.ft.methodeapi.service.methode.templates.MethodeSessionOperationTemplate;
import com.google.common.collect.Lists;
import org.omg.CORBA.Object;
import org.omg.CORBA.UserException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * WARNING
 * This class is used by smoke tests in every environment including production.
 * It is very important that creating and deleting of content in production methode
 * is restricted to the TEST_FOLDER. If you make changes to the code below (or the
 * methods it calls), please ensure that you do not allow writing or deleting outside this folder.
 */

public class CreateFileCallback implements MethodeSessionOperationTemplate.SessionCallback<EomFile> {

    /** The path to the web types configuration file in Methode */
    private static final String WEB_TYPES_PATH = "/SysConfig/webTypes.cfg";
    private static final Logger LOGGER = LoggerFactory.getLogger(CreateFileCallback.class);

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

        List<org.omg.CORBA.Object> forRelease = Lists.newArrayList();
        FileSystemAdmin fileSystemAdmin = null;
        try {
            fileSystemAdmin = methodeObjectFactory.createFileSystemAdmin(session);

            File file = createFile(fileSystemAdmin, session, forRelease);

            List<LinkedObject> linkedObjects = null;

            if(eomFile.getLinkedObjects() !=null && !eomFile.getLinkedObjects().isEmpty()) {

                linkedObjects = createLinks(session, file, forRelease);
            }

            setStatusAndFireStatusEvent(file, session, forRelease);

            return new EomFile(file.get_uuid_string(), file.get_type_name(), file.read_all(),
                    file.get_attributes(), file.get_status_name(), file.get_system_attributes(),
                    file.get_usage_tickets(""), linkedObjects);

        } catch (TypeNotFound | RepositoryError | PermissionDenied | InvalidName | InvalidForContainer | ObjectLocked
                | DuplicatedName | ObjectNotLocked | ObjectNotCheckedOut | ObjectNotFound e) {
            throw new MethodeException(e);
        } catch (InvalidAttributes | InvalidType e) {
            throw new InvalidEomFileException("cannot create requested file", e);
        } catch (InvalidStatus invalidStatus) {
            throw new InvalidEomFileException("Invalid workflow status.", invalidStatus);
        } finally {
            for (org.omg.CORBA.Object corbaObject: forRelease) {
                corbaObject._release();
            }
            methodeObjectFactory.maybeCloseFileSystemAdmin(fileSystemAdmin);
        }
    }

    private File createFile(FileSystemAdmin fileSystemAdmin, Session session, List<Object> forRelease)
            throws TypeNotFound, RepositoryError, PermissionDenied,
            InvalidName, InvalidForContainer, ObjectLocked, DuplicatedName, InvalidType, ObjectNotLocked,
            InvalidAttributes, ObjectNotFound, ObjectNotCheckedOut {

        final ObjectTypeAdmin objectTypeAdmin = ObjectTypeAdminHelper.narrow(session.resolve_initial_references("ObjectTypeAdmin"));

        final ObjectType objectType = objectTypeAdmin.get_object_type(eomFile.getType());

        final Folder rootFolder = fileSystemAdmin.get_root();

        final Folder folder = findOrCreateFolder(rootFolder, path);

        final File file = folder.create_file(filename, objectType);

        forRelease.add(objectTypeAdmin);
        forRelease.add(objectType);
        forRelease.add(rootFolder);
        forRelease.add(folder);
        forRelease.add(file);

        file.write_all(eomFile.getValue());
        file.set_attributes(eomFile.getAttributes());
        file.set_system_attributes(eomFile.getSystemAttributes());
        file.check_in("", false);
        try {
            String now = new Date().toString();
            file.append_usage_ticket("web_publication","<dt><publishedDate>" + now + "</publishedDate></dt>", 0);
            file.append_usage_ticket("Publisher","<dt><publishedDate>" + now + "</publishedDate></dt>", 0);
        } catch (UserException ignored) {
            //just carry on
        }
        return file;
    }

    private List<LinkedObject> createLinks(Session session, File file, List<Object> forRelease){
        List<LinkedObject> linkedObjects = new ArrayList<>();
        try {
            EomDbHelper helper = EomDbHelperFactory.create(session);
            EomDb eomDb = helper.getEomDb();
            EomDbObject parentDbObject = eomDb.getEomDbObjectByUuid(file.get_uuid_string());
            _Object parentObject = parentDbObject.getEomObject();
            WebTypes webTypes;

            forRelease.add(parentObject);
            try {
                webTypes = helper.loadWebTypes(WEB_TYPES_PATH);
            } catch (Exception e) {
                throw new MethodeException("Could not load Methode web types", e);
            }
            for (LinkedObject child : eomFile.getLinkedObjects()) {
                EomDbObject childDbObject = eomDb.getEomDbObjectByUuid(child.getUuid());
                _Object childObject = childDbObject.getEomObject();

                forRelease.add(childObject);
                try {
                    LOGGER.error(childObject.get_creator());

                    try {
                        Dwp dwp = new Dwp(eomDb, parentDbObject, webTypes);
                        dwp.load(true);
                        DwpLink link = dwp.createLink(childDbObject);
                        if (!dwp.appendLink(link, "zone1")) {
                            throw new MethodeException("Failed linking objects");
                        }
                        if (!dwp.saveLinks()) {
                            throw new MethodeException("Failed saving links");
                        }
                    } catch (Exception e) {
                        throw new MethodeException("Failure to create link", e);
                    }
                    linkedObjects.add(new LinkedObject(childDbObject.getUuid(), childDbObject.getTypeName()));
                } catch (Exception e) {
                    LOGGER.info("", e);
                    throw new MethodeException("Cannot link to locked object " + child.getUuid(), e);
                }
            }
            return linkedObjects;
        } catch (PermissionDenied | RepositoryError | ObjectNotFound e) {
            throw new MethodeException("Failed to create links", e);
        }
    }
    
    private void setStatusAndFireStatusEvent(File file, Session session, List<Object> forRelease) 
    		throws ObjectNotFound, RepositoryError, PermissionDenied, InvalidStatus {
    	final ObjectAdmin oa = ObjectAdminHelper.narrow(session.resolve_initial_references("ObjectAdmin"));
		oa.set_status_name(file, eomFile.getWorkflowStatus());
		final EventAdmin ea = EOM.EventAdminHelper.narrow(session.resolve_initial_references("EventAdmin"));
		ea.fire_event(file, "set_status");
		
		forRelease.add(oa);
		forRelease.add(ea);
	}

    private Folder findOrCreateFolder(final Folder rootFolder, final String path) throws RepositoryError, PermissionDenied, InvalidName, InvalidForContainer, ObjectLocked, DuplicatedName, ObjectNotLocked {
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
			currentFolder.unlock();
            return currentFolder;
        }
    }
}
