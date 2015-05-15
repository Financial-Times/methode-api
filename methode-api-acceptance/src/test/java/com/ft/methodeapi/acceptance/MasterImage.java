//package com.ft.methodeapi.acceptance;
//
//import com.ft.methodeapi.acceptance.util.FileUtil;
//import com.ft.methodeapi.model.EomFile;
//
//import com.ft.methodeapi.model.LinkedObject;
//import com.google.common.collect.ImmutableList;
//import sun.misc.IOUtils;
//
//import java.util.List;
//import java.util.UUID;
//
//public class MasterImage {
//
//    public static final String EXPECTED_CAPTION = "Fruits of the soul";
//    public static final String EXPECTED_ALT_TEXT = "Picture with fruits";
//    public static final String EXPECTED_METHODE_ORIGINATING_URI = "http://api.ft.com/system/FTCOM-METHODE";
//    private static final String TEST_IMAGE_UUID = UUID.randomUUID().toString();
//    private static final String MASTER_IMAGE_SAMPLE_IMAGE = "images/Master_2048x1152/mi-sample-image.jpg";
//    private static final String MASTER_IMAGE_ATTRIBUTES_XML = "images/Master_2048x1152/mi-sample-attributes.xml";
//    private static final String MASTER_IMAGE_SYSTEM_ATTRIBUTES_XML = "images/Master_2048x1152/mi-sample-system-attributes.xml";
//    private static final String MASTER_IMAGE_USAGE_TICKETS_XML = "images/Master_2048x1152/mi-sample-usage-tickets.xml";
//    private static final String EOM_COMPOUND_STORY_TYPE = "EOM::CompoundStory";
//    protected byte[] imageBinary;
//    protected String imagePath, attributes, systemAttributes, usageTickets;
//
//    public EomFile buildImage() throws Exception {
//        return buildImageWithUuid(TEST_IMAGE_UUID);
//    }
//
//    public EomFile buildImageWithUuid(String uuid) throws Exception {
//        imagePath = MASTER_IMAGE_SAMPLE_IMAGE;
//        attributes = String.format(FileUtil.loadFile(MASTER_IMAGE_ATTRIBUTES_XML), EXPECTED_CAPTION, EXPECTED_ALT_TEXT);
//        systemAttributes = FileUtil.loadFile(MASTER_IMAGE_SYSTEM_ATTRIBUTES_XML);
//        usageTickets = FileUtil.loadFile(MASTER_IMAGE_USAGE_TICKETS_XML);
//        List<LinkedObject> linkedObjects = ImmutableList.of(new LinkedObject("bd527556-d7c5-11e4-849b-00144feab7de", EOM_COMPOUND_STORY_TYPE),
//                new LinkedObject("fc2b257a-d90f-11e4-b907-00144feab7de", EOM_COMPOUND_STORY_TYPE));
//        imageBinary = loadImage();
//        return new EomFile(uuid, "Image", imageBinary, attributes, "", systemAttributes, usageTickets, linkedObjects);
//    }
//
//    protected byte[] loadImage() throws Exception {
//        return IOUtils.toByteArray(MasterImage.class.getResourceAsStream("/" + imagePath));
//    }
//
//}
