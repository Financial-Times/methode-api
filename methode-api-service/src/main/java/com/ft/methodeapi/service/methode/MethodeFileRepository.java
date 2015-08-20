package com.ft.methodeapi.service.methode;

import static com.ft.methodeapi.service.methode.PathHelper.folderIsAncestor;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import EOM.*;

import com.eidosmedia.wa.render.EomDbHelperFactory;
import com.eidosmedia.wa.render.WebObject;
import com.ft.methodeapi.model.EomAssetType;
import com.ft.methodeapi.model.EomFile;
import com.ft.methodeapi.model.LinkedObject;
import com.ft.methodeapi.service.methode.connection.MethodeObjectFactory;
import com.ft.methodeapi.service.methode.templates.MethodeFileSystemAdminOperationTemplate;
import com.ft.methodeapi.service.methode.templates.MethodeSessionFileOperationTemplate;
import com.ft.methodeapi.service.methode.templates.MethodeSessionOperationTemplate;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;

public class MethodeFileRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodeFileRepository.class);

    private static final Charset METHODE_ENCODING = Charset.forName("iso-8859-1");
    private static final Charset UTF8 = Charset.forName("UTF8");
    
    private final MethodeObjectFactory client;
    private final MethodeObjectFactory testClient;

    public MethodeFileRepository(MethodeObjectFactory client, MethodeObjectFactory testClient) {
        this.client = client;
        this.testClient = testClient;
    }

    public Optional<EomFile> findFileByUuid(final String uuid) {
        final MethodeSessionFileOperationTemplate<Optional<EomFile>> template =
                new MethodeSessionFileOperationTemplate<>(client, MethodeFileRepository.class, "findFileByUuid");
        
        MethodeSessionFileOperationTemplate.SessionFileOperationCallback<Optional<EomFile>> callback = new MethodeSessionFileOperationTemplate.SessionFileOperationCallback<Optional<EomFile>>() {
            @Override
            public Optional<EomFile> doOperation(FileSystemAdmin fileSystemAdmin, Session session) {
				String uri = "eom:/uuids/" + uuid;

				FileSystemObject fso;
				Optional<EomFile> foundContent;
				try {
					fso = fileSystemAdmin.get_object_with_uri(uri);

					EOM.File eomFile = EOM.FileHelper.narrow(fso);

					final String typeName = eomFile.get_type_name();
                    
                    //this reads the article contents of the latest version, which may not be the published version
					//final byte[] bytes = eomFile.read_all();
                    final String attributes = new String(eomFile.get_attributes().getBytes(METHODE_ENCODING), UTF8);
					final String workflowStatus = eomFile.get_status_name();
					final String systemAttributes = eomFile.get_system_attributes();
                    final String usageTickets = eomFile.get_usage_tickets("");

                    // get the last modified date of the article
                    // modified may mean just copied for moved, not necessarily changes in the article content
                    final Instant fromUnixTimestamp = Instant.ofEpochSecond(eomFile.get_modification_time());
                    final LocalDateTime lastModifiedLocalDateTime = LocalDateTime.ofInstant(fromUnixTimestamp, ZoneOffset.UTC);

                    final LocalDateTime latestPublishdate = getLastPublishDateFromUsageTickets(usageTickets);
                    LOGGER.info(">>> UUID: " + uuid);
                    LOGGER.debug(">>> Last Modified   :" + lastModifiedLocalDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    LOGGER.debug(">>> Latest Published:" + latestPublishdate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

                    if (latestPublishdate.isAfter(lastModifiedLocalDateTime) || latestPublishdate.isEqual(lastModifiedLocalDateTime)) {
                        LOGGER.info(">>> Publish date after/equal to last modified!\n");
                    } else {
                        LOGGER.info(">>> Publish date before last modified date\n");
                    }

                    //replace article content with the content of the latest version
                    //this PROBABLY is the last published version
                    Version lastVersion = getLastVersion(eomFile);
                    final byte[] bytes = lastVersion.read_all();
                    LOGGER.debug("Replacing original bytes with bytes from last version:");
                    LOGGER.debug("Original: \n" + new String(eomFile.read_all()));
                    LOGGER.debug("Last Version: \n" + new String(bytes));
                    
                    try {
                        List<LinkedObject> links = new ArrayList<>();
                        
                        try {
                            WebObject webObject = EomDbHelperFactory.create(session).getWebObjectByUuid(uuid);
                            
                            if ("EOM::WebContainer".equals(typeName)) {
                                /* zonesMap is a Map of zone name <-> another map.
                                 * The nested map contains a "linkedObjects" key, whose value is an array of WebObject ...
                                 * We need the links from these WebObjects. 
                                 */
                                @SuppressWarnings("unchecked")
                                Collection<Map<String,Object>> zones = webObject.getZonesMap().values();
                                
                                for (Map zone : zones) {
                                    WebObject[] linkedObjects = (WebObject[])zone.get("linkedObjects");
                                    for (WebObject linked : linkedObjects) {
                                        links.add(new LinkedObject(
                                                linked.getUuid(),
                                                linked.getEomFile().get_type_name()
                                            ));
                                    }
                                }
                            }
                        } catch (Exception e) {
                            throw new MethodeException("Failed to load zones data", e);
                        }

                        EomFile content = new EomFile(uuid, typeName, bytes, attributes, workflowStatus, systemAttributes,
                                usageTickets, links);
                        foundContent = Optional.of(content);

                    } finally {
                        eomFile._release();
                    }

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

    public Map<String, EomAssetType> getAssetTypes(final Set<String> assetIdentifiers){
        Preconditions.checkNotNull(assetIdentifiers);
        long methodStart = System.currentTimeMillis();
        try {
            final MethodeFileSystemAdminOperationTemplate<Map<String, EomAssetType>> template =
                    new MethodeFileSystemAdminOperationTemplate<>(client, MethodeFileRepository.class, "getAssetTypes");
            
            return template.doOperation(new GetAssetTypeFileSystemAdminCallback(assetIdentifiers));
        } finally {
            long duration = System.currentTimeMillis() - methodStart;
            LOGGER.info("Obtained types. assetCount={}, elapsedTimeMs={}",assetIdentifiers.size(), duration);
        }
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
        final MethodeSessionOperationTemplate<EomFile> template = new MethodeSessionOperationTemplate<>(testClient, MethodeFileRepository.class, "createNewTestFile");
        final EomFile createdEomFile = template.doOperation(new CreateFileCallback(testClient, TEST_FOLDER + dateStamp(), filename, eomFile));
        return createdEomFile;
    }

	private String dateStamp() {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
		return "/" + simpleDateFormat.format(new Date());
	}

	/**
     * WARNING
     * This method is used by smoke tests in every environment including production.
     * It is very important that creating and deleting of content in production methode
     * is restricted to the TEST_FOLDER. If you make changes to the code below (or the
     * methods it calls), please ensure that you do not allow writing or deleting outside this folder.
     */
    public void deleteTestFileByUuid(final String uuid) {
    	final MethodeFileSystemAdminOperationTemplate<Void> template = new MethodeFileSystemAdminOperationTemplate<>(testClient, MethodeFileRepository.class, "deleteTestFileByUuid");

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

    private Version getLastVersion(File eomFile) {
        Version lastVersion = null;
        try {
            lastVersion = eomFile.get_nth_version(eomFile.history().length);

        } catch (Exception e) {
            LOGGER.error("Error while getting latest version of article contents" + e.getMessage());
        }
        return lastVersion;
    }

    private LocalDateTime getLastPublishDateFromUsageTickets(String xml) {
        LocalDateTime latestPublishDate = null;
        try {
            final NodeList publishDates = parseUsageTickets(getUsageTicketsDocument(xml));
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE MMM dd kk:mm:ss zzz uuuu");
            
            for (int i = 0; i < publishDates.getLength(); i++) {
                Node publishDateNode = publishDates.item(i);
                String textContent = publishDateNode.getTextContent();
                ZonedDateTime zonedPublishDate = ZonedDateTime.parse(textContent, formatter);
                LocalDateTime publishDate = zonedPublishDate.toLocalDateTime();

                LOGGER.debug(">>> Publish date: " + zonedPublishDate.format(DateTimeFormatter.ISO_DATE_TIME));
                
                if (latestPublishDate == null || publishDate.isAfter(latestPublishDate)) {
                    latestPublishDate = publishDate;
                } else {
                    if (publishDate.isAfter(latestPublishDate)) {
                        latestPublishDate = publishDate;
                    }
                }
            }

            return latestPublishDate;
        } catch (Exception e) {
            LOGGER.error("Error while getting last publish date from usage tickets: " + e.getMessage());
            return latestPublishDate;
        }
    }

    private NodeList parseUsageTickets(Document usageTicketsDocument) throws XPathExpressionException {
        final XPath xpath = XPathFactory.newInstance().newXPath();
        XPathExpression xPathExpression = xpath.compile("/tl/t/dt/publishedDate");
        return (NodeList) xPathExpression.evaluate(usageTicketsDocument, XPathConstants.NODESET);
    }

    private Document getUsageTicketsDocument(String xml) throws ParserConfigurationException, SAXException, IOException {
        final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        return documentBuilder.parse(new InputSource(new StringReader(xml)));
    }

}
