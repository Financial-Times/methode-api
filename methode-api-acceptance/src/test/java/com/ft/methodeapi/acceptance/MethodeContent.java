package com.ft.methodeapi.acceptance;

import com.ft.methodeapi.acceptance.xml.Xml;
import com.ft.methodeapi.model.EomFile;
import com.ft.methodeapi.model.LinkedObject;
import com.google.common.base.Charsets;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class MethodeContent {

    public static final String HEADLINE_FROM_TEST_FILE = "Lead headline \u00a342m for S&amp;P\u2019s \u201cup 79%\u201d";
    public static final String MARK_DELETED_TRUE = "<DIFTcomMarkDeleted>True</DIFTcomMarkDeleted>";
    public static final String MARK_DELETED_FALSE = "<DIFTcomMarkDeleted>False</DIFTcomMarkDeleted>";
    public static final String WEB_REVISE = "Stories/WebRevise";
    public static final String WEB_READY = "Stories/WebReady";
    public static final String CLEARED = "AdLayers/Cleared";
    public static final String WEB_CHANNEL = "FTcom";
    public static final String NEWSPAPER_CHANNEL = "Financial Times";
    public static final String METHODE_DATE_FORMAT = "yyyyMMddHHmmss";

    private static final String SOURCE = "<Source title=\"Financial Times\"><SourceCode>FT</SourceCode><SourceDescriptor>Financial Times</SourceDescriptor>";
    private static final String SYSTEM_ATTRIBUTES_WEB = "<props><productInfo><name>FTcom</name>\n" +
            "<issueDate>20131219</issueDate>\n" +
            "</productInfo>\n" +
            "<workFolder>/FT/Companies</workFolder>\n" +
            "<templateName>/SysConfig/Templates/FT/Base-Story.xml</templateName>\n" +
            "<summary>t text text text text text text text text text text text text text\n" +
            " text text text text te...</summary><wordCount>417</wordCount></props>";

    private String articleXml;
    private String attributesXml;
    private String workflowStatus;
    private String systemAttributes;
    private String contentType;
    private List<LinkedObject> linkedObjects;

    private MethodeContent(String articleXml, String contentType, String attributesXml, String workflowStatus, String systemAttributes, List<LinkedObject> linkedObjects) {
        this.articleXml = articleXml;
        this.attributesXml = attributesXml;
        this.workflowStatus = workflowStatus;
        this.systemAttributes = systemAttributes;
        this.linkedObjects = linkedObjects;
        this.contentType = contentType;
    }

    public String getArticleXml() { return articleXml; }

    public String getAttributesXml() { return attributesXml; }

    public String getWorkflowStatus() { return workflowStatus; }

    public String getSystemAttributes() { return systemAttributes; }

    public List<LinkedObject> getLinkedObjects() { return linkedObjects; }

    public EomFile getEomFile() {
        return new EomFile("",contentType,
                articleXml.getBytes(Charsets.UTF_8),
                attributesXml, workflowStatus, systemAttributes, "usageTickets", linkedObjects);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this.getClass())
                .add("articleXml", articleXml)
                .add("attributesXml", attributesXml)
                .add("workflowStatus", workflowStatus)
                .add("systemAttributes", systemAttributes)
                .add("linkedObjects", linkedObjects)
                .toString();
    }

    public static abstract class ContentBuilder <T extends ContentBuilder>  {

        protected String articleXml;
        protected String attributesXml;
        protected String workflowStatus;
        protected String systemAttributes = SYSTEM_ATTRIBUTES_WEB;
        protected static final String EMBARGO_DATE = "<EmbargoDate/>";

        protected ContentBuilder() { }

        public T withWorkflowStatus(String workflowStatus) {
            this.workflowStatus = workflowStatus;
            return (T) this;
        }

        public T withSource(String source) {
            String newSourceXml = SOURCE.replace("Financial Times", source).replace("FT", source);
            attributesXml = attributesXml.replace(SOURCE, newSourceXml);
            return (T) this;
        }

        public T withChannel(String channel) {
            systemAttributes = SYSTEM_ATTRIBUTES_WEB.replace("FTcom", channel);
            return (T) this;
        }

        public T withEmbargoDate(Date embargoDate) {
            attributesXml = attributesXml.replace(EMBARGO_DATE, String.format("<EmbargoDate>%s</EmbargoDate>", inMethodeFormat(embargoDate)));
            return (T) this;
        }

        public T withAttributes(String attributesXml) {
            this.attributesXml = attributesXml;
            return (T) this;
        }

        public T withSystemAttributes(String systemAttributes) {
            this.systemAttributes = systemAttributes;
            return (T) this;
        }

        public T withArticleXml(String articleXml) {
            this.articleXml = articleXml;
            return (T) this;
        }

        private String inMethodeFormat(Date date) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);

            DateFormat methodeDateFormat = new SimpleDateFormat(METHODE_DATE_FORMAT);
            methodeDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            return methodeDateFormat.format(cal.getTime());
        }

        public T published() {
            Preconditions.checkArgument(!this.attributesXml.contains(MARK_DELETED_TRUE), "already deleted");
            return (T) this;
        }

        public ContentBuilder deleted() {
            attributesXml = attributesXml.replace(MARK_DELETED_FALSE, MARK_DELETED_TRUE);
            return this;
        }

        public MethodeContent buildPublishedArticle() {
            return published().build();
        }

        public MethodeContent buildDeletedArticle() {
            return deleted().build();
        }

        public abstract MethodeContent build(); //{
//            Xml.assertParseable(articleXml);
//            Xml.assertParseable(attributesXml);
//
//            return new MethodeContent(articleXml, "EOM::CompoundStory", attributesXml, workflowStatus, systemAttributes, linkedObjects);
//        }
    }

    public static class ListBuilder extends ContentBuilder<ListBuilder> {

        private List<LinkedObject> linkedObjects;

        public ListBuilder withLinkedObjects(List<LinkedObject> linkedObjects) {
            this.linkedObjects = linkedObjects;
            return this;
        }

        @Override
        public MethodeContent build() {
            Xml.assertParseable(articleXml);
            Xml.assertParseable(attributesXml);

            return new MethodeContent(articleXml, "EOM::WebContainer", attributesXml, workflowStatus, systemAttributes, linkedObjects);
        }
    }

    public static class ArticleBuilder extends ContentBuilder<ArticleBuilder> {

        public ArticleBuilder withHeadline(String expectedPublishedArticleHeadline) {
            attributesXml = attributesXml.replace(HEADLINE_FROM_TEST_FILE, expectedPublishedArticleHeadline);
            articleXml = articleXml.replace(HEADLINE_FROM_TEST_FILE, expectedPublishedArticleHeadline);
            return this;
        }

        @Override
        public MethodeContent build() {
            Xml.assertParseable(articleXml);
            Xml.assertParseable(attributesXml);

            return new MethodeContent(articleXml, "EOM::CompoundStory", attributesXml, workflowStatus, systemAttributes, null);
        }
    }


}

