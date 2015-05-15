//package com.ft.methodeapi.acceptance;
//
//
//import com.ft.methodeapi.acceptance.util.FileUtil;
//import com.ft.methodeapi.model.EomFile;
//
//import java.io.IOException;
//import java.nio.charset.Charset;
//import java.text.DateFormat;
//import java.text.SimpleDateFormat;
//import java.util.Calendar;
//import java.util.Date;
//import java.util.TimeZone;
//
//public class ImageArticles {
//
//    private static final String STORY_TYPE = "EOM::CompoundStory";
//    private static final String SOURCE_CODE = "FT";
//    private static final String CHANEL = "FTcom";
//    private static final String DATE_FORMAT = "yyyyMMddHHmmss";
//    private static final String STORY_TEMPLATE = "/SysConfig/Templates/FT/Base-Story.xml";
//    private static final Charset UTF_8 = Charset.forName("UTF-8");
//    public static final String IMAGE_SET_ONTOLOGY = "http://www.ft.com/ontology/content/ImageSet";
//    public static final String DEFAULT_MAIN_IMAGE_SIZE = "Primary size";
//    private static final String simpleArticleXmlTemplate = FileUtil.loadFile("article/simple_article_value.xml");
//    private static final String articleWithMainImageXmlTemplate = FileUtil.loadFile("article/article_value_with_main_image.xml");
//    private static final String articleWithInlineImageXmlTemplate = FileUtil.loadFile("article/article_value_with_inline_image.xml");
//    private static final String articleWithMainAndInlineImageXmlTemplate = FileUtil.loadFile("article/article_value_with_main_and_inline_image.xml");
//    private static final String articleAttributesXml = FileUtil.loadFile("article/article_attributes.xml");
//    private static final String articleSystemAttributesXml = FileUtil.loadFile("article/article_system_attributes.xml");
//
//    public static EomFile buildSimpleArticle(String articleUuid, String markedDeleted, String workflowStatus) throws IOException {
//
//        return new EomFile.Builder()
//                .withUuid(articleUuid)
//                .withType(STORY_TYPE)
//                .withValue(simpleArticleXmlTemplate.getBytes(UTF_8))
//                .withAttributes(String.format(articleAttributesXml, methodeFormatDate(), markedDeleted, DEFAULT_MAIN_IMAGE_SIZE, SOURCE_CODE))
//                .withSystemAttributes(String.format(articleSystemAttributesXml, CHANEL, STORY_TEMPLATE))
//                .withWorkflowStatus(workflowStatus)
//                .build();
//    }
//
//    public static EomFile buildArticleWithMainImage(String articleUuid, String mainImageUuid, String markedDeleted, String mainImageSize, String workflowStatus) throws IOException {
//
//        return new EomFile.Builder()
//                .withUuid(articleUuid)
//                .withType(STORY_TYPE)
//                .withValue(String.format(articleWithMainImageXmlTemplate, mainImageUuid).getBytes(UTF_8))
//                .withAttributes(String.format(articleAttributesXml, methodeFormatDate(), markedDeleted, mainImageSize, SOURCE_CODE))
//                .withSystemAttributes(String.format(articleSystemAttributesXml, CHANEL, STORY_TEMPLATE))
//                .withWorkflowStatus(workflowStatus)
//                .build();
//    }
//
//    public static EomFile buildArticleWithInlineImage(String articleUuid, String inlineImageUuid, String markedDeleted, String mainImageSize, String workflowStatus) throws IOException {
//
//        return new EomFile.Builder()
//                .withUuid(articleUuid)
//                .withType(STORY_TYPE)
//                .withValue(String.format(articleWithInlineImageXmlTemplate, inlineImageUuid).getBytes(UTF_8))
//                .withAttributes(String.format(articleAttributesXml, methodeFormatDate(), markedDeleted, mainImageSize, SOURCE_CODE))
//                .withSystemAttributes(String.format(articleSystemAttributesXml, CHANEL, STORY_TEMPLATE))
//                .withWorkflowStatus(workflowStatus)
//                .build();
//    }
//
//    public static EomFile buildArticleWithMainImageAndInlineImage(String articleUuid, String mainImageUuid, String inlineImageUuid, String markedDeleted, String mainImageSize, String workflowStatus) throws IOException {
//
//        return new EomFile.Builder()
//                .withUuid(articleUuid)
//                .withType(STORY_TYPE)
//                .withValue(String.format(articleWithMainAndInlineImageXmlTemplate, mainImageUuid, inlineImageUuid).getBytes(UTF_8))
//                .withAttributes(String.format(articleAttributesXml, methodeFormatDate(), markedDeleted, mainImageSize, SOURCE_CODE))
//                .withSystemAttributes(String.format(articleSystemAttributesXml, CHANEL, STORY_TEMPLATE))
//                .withWorkflowStatus(workflowStatus)
//                .build();
//    }
//
//    private static String methodeFormatDate() {
//        Date currentDate = new Date(System.currentTimeMillis());
//        Calendar cal = Calendar.getInstance();
//        cal.setTime(currentDate);
//
//        DateFormat methodeDateFormat = new SimpleDateFormat(DATE_FORMAT);
//        methodeDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
//        return methodeDateFormat.format(cal.getTime());
//    }
//}
