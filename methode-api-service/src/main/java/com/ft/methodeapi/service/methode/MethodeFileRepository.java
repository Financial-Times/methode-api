package com.ft.methodeapi.service.methode;

import EOM.File;
import EOM.FileSystemAdmin;
import EOM.FileSystemObject;
import EOM.InvalidURI;
import EOM.ObjectLocked;
import EOM.PermissionDenied;
import EOM.RepositoryError;
import EOM.Session;
import EOM.Utils;
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
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import static com.ft.methodeapi.service.methode.PathHelper.folderIsAncestor;

public class MethodeFileRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodeFileRepository.class);

    private static final Charset METHODE_ENCODING = Charset.forName("iso-8859-1");
    private static final Charset UTF8 = Charset.forName("UTF8");
    private static final String DATE_FORMAT = "yyyyMMddHHmmss";
    private final MethodeObjectFactory client;
    private final MethodeObjectFactory testClient;

    public MethodeFileRepository(MethodeObjectFactory client, MethodeObjectFactory testClient) {
        this.client = client;
        this.testClient = testClient;
    }

    private DocumentBuilder getDocumentBuilder() throws ParserConfigurationException {
        final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        return documentBuilderFactory.newDocumentBuilder();
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
                    final byte[] bytes = eomFile.read_all();
                    final String attributes = new String(eomFile.get_attributes().getBytes(METHODE_ENCODING), UTF8);
                    final String workflowStatus = eomFile.get_status_name();

                    final String systemAttributes = eomFile.get_system_attributes();
                    String usageTickets = eomFile.get_usage_tickets("");

                    usageTickets = getModifiedUsageTicketIfPossible(systemAttributes, usageTickets);

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
                                Collection<Map<String, Object>> zones = webObject.getZonesMap().values();

                                for (Map zone : zones) {
                                    WebObject[] linkedObjects = (WebObject[]) zone.get("linkedObjects");
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

                } catch (
                        InvalidURI invalidURI
                        )

                {
                    return Optional.absent();
                } catch (RepositoryError |
                        PermissionDenied e
                        )

                {
                    throw new MethodeException(e);
                }

                return foundContent;
            }

            protected String getModifiedUsageTicketIfPossible(String systemAttributes, String usageTickets) {
                final DocumentBuilder documentBuilder;
                try {
                    documentBuilder = getDocumentBuilder();
                } catch (ParserConfigurationException e) {
                    throw new RuntimeException(e);
                }
                final XPath xpath = XPathFactory.newInstance().newXPath();
                final Document usageTicketsDocument;
                try {
                    usageTicketsDocument = documentBuilder.parse(new InputSource(new StringReader(usageTickets)));
                } catch (SAXException | IOException e) {
                    throw new RuntimeException(e);
                }
                try {
                    String publishDateString = xpath.evaluate("/tl/t[tp = 'WebCopy'][count(/tl/t[tp = 'WebCopy'])]/cd", usageTicketsDocument);
                    LOGGER.info(">>>>>>>> sandor uuid:" + uuid + ", WebCopy: " + publishDateString);
                    if (publishDateString != null && !"".equals(publishDateString)) {
                        usageTickets = insertPublishDateIntoUsageTickets(usageTickets, usageTicketsDocument, publishDateString);
                    } else {
                        publishDateString = xpath.evaluate("/tl/t[tp = 'Archive'][count(/tl/t[tp = 'Archive'])]/cd", usageTicketsDocument);
                        LOGGER.info(">>>>>>>> sandor uuid:" + uuid + ", Archive: " + publishDateString);
                        if (publishDateString != null && !"".equals(publishDateString)) {
                            usageTickets = insertPublishDateIntoUsageTickets(usageTickets, usageTicketsDocument, publishDateString);
                        } else {
                            final Document systemAttributesDocument;
                            try {
                                systemAttributesDocument = documentBuilder.parse(new InputSource(new StringReader(systemAttributes)));
                                publishDateString = xpath.evaluate("/props/productInfo/issueDate", systemAttributesDocument);
                                LOGGER.info(">>>>>>>> sandor uuid:" + uuid + ", issueDate: " + publishDateString);
                            } catch (SAXException | IOException e) {
                                //no system attributes, ignore
                            }

                            if (publishDateString != null && !"".equals(publishDateString)) {
                                //this publishDateString would look like yyyyMMdd
                                //make publishDateString to look like expected parse format: yyyyMMddHHmmss
                                publishDateString+="000000";
                                usageTickets = insertPublishDateIntoUsageTickets(usageTickets, usageTicketsDocument, publishDateString);
                            } else {
                                LOGGER.info(">>>>>>>> sandor uuid:" + uuid + ", no publishDate to extract.");
                                // Unix epoch start time (0), means GMT: Thu, 01 Jan 1970 00:00:00 GMT
                                publishDateString = "19700101000000";
                                usageTickets = insertPublishDateIntoUsageTickets(usageTickets, usageTicketsDocument, publishDateString);
                            }
                        }
                    }


                } catch (XPathExpressionException e) {
                    throw new RuntimeException(e);
                }
                return usageTickets;
            }

            private String insertPublishDateIntoUsageTickets(String usageTickets, Document usageTicketsDocument, String archiveDate) {
                Date publishDate = transformDate(uuid, archiveDate);
                if (publishDate != null) {
                    simulateWebPublishUsageTicket(usageTicketsDocument, archiveDate);
                    usageTickets = serializeBody(usageTicketsDocument);
                }
                return usageTickets;
            }

        };

        return template.doOperation(callback);
    }

    private String serializeBody(Document document) {
        final DOMSource domSource = new DOMSource(document);
        final StringWriter writer = new StringWriter();
        final StreamResult result = new StreamResult(writer);
        final TransformerFactory tf = TransformerFactory.newInstance();
        try {
            final Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty("omit-xml-declaration", "no");
//            transformer.setOutputProperty("standalone", "yes");
            transformer.transform(domSource, result);
            writer.flush();
            final String body = writer.toString();
            return body;
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }
    }

    private void simulateWebPublishUsageTicket(Document ownerDocument, String publishDate) {
        Element publishUsageTicket = ownerDocument.createElement("t");
        Element id = ownerDocument.createElement("id");
        Element tp = ownerDocument.createElement("tp");
        Element c = ownerDocument.createElement("c");
        Element cd = ownerDocument.createElement("cd");

        id.setTextContent("111");
        tp.setTextContent("web_publication");
        c.setTextContent("republish_images_with_publish_date_null");
        cd.setTextContent(publishDate);

        publishUsageTicket.appendChild(id);
        publishUsageTicket.appendChild(tp);
        publishUsageTicket.appendChild(c);
        publishUsageTicket.appendChild(cd);

        Node first = ownerDocument.getFirstChild();
        if (first != null) {
            first.appendChild(publishUsageTicket);
        } else {
            ownerDocument.appendChild(publishUsageTicket);
        }


    }

    private Date transformDate(final String uuid, final String dateString) {
        final DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        try {
            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            return dateFormat.parse(dateString);
        } catch (ParseException ex) {
            LOGGER.warn("Date couldn't be parsed for uuid {} and raw value '{}'.", uuid, dateString);
        }
        return null;
    }

    public Map<String, EomAssetType> getAssetTypes(final Set<String> assetIdentifiers) {
        Preconditions.checkNotNull(assetIdentifiers);
        long methodStart = System.currentTimeMillis();
        try {
            final MethodeFileSystemAdminOperationTemplate<Map<String, EomAssetType>> template =
                    new MethodeFileSystemAdminOperationTemplate<>(client, MethodeFileRepository.class, "getAssetTypes");

            return template.doOperation(new GetAssetTypeFileSystemAdminCallback(assetIdentifiers));
        } finally {
            long duration = System.currentTimeMillis() - methodStart;
            LOGGER.info("Obtained types. assetCount={}, elapsedTimeMs={}", assetIdentifiers.size(), duration);
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

                        if (folderIsAncestor(PATH_TO_TEST_FOLDER, pathToFile)) {
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
