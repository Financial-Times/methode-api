package com.ft.methodeapi.smoke;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.ft.methodeapi.acceptance.ApiConfig;
import com.ft.methodeapi.acceptance.MethodeContent;
import com.ft.methodeapi.acceptance.ReferenceArticles;
import com.ft.methodeapi.acceptance.Xml;
import com.ft.methodeapi.model.EomFile;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.config.DecoderConfig.decoderConfig;
import static com.jayway.restassured.config.EncoderConfig.encoderConfig;
import static com.jayway.restassured.config.RestAssuredConfig.newConfig;
import static com.jayway.restassured.path.json.JsonPath.from;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

public class MethodeApiSmokeTests {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodeApiSmokeTests.class);

    private final String methodeApiServiceUrl;

    /**
     * Used to identify XML that is not significant for comparison purposes when considering
     * two examples of Methode attributes XML.
     */
    private static final String[] INSIGNIFICANT_XPATHS = {
            "/ObjectMetadata/OutputChannels/DIFTcom/DIFTcomSafeToSyndicate" ,
            "/ObjectMetadata/EditorialDisplayIndexing//DIBylineCopy", /* TODO we can resume testing this if the methode bug goes away */
            "/ObjectMetadata/EditorialDisplayIndexing/DIFirstParCopy", //TODO remove once the drop and drag new promo box component makes it through Methode environments to live
            "/ObjectMetadata/EditorialDisplayIndexing/DIMasterImgFileRef", //TODO remove once the drop and drag new promo box component makes it through Methode environments to live"/ObjectMetadata/EditorialDisplayIndexing/DIMasterImgFileRef" //TODO remove once the drop and drag new promo box component makes it through Methode environments to live
            "/ObjectMetadata/EditorialDisplayIndexing/DIFTNPSections[2]" //TODO remove once the drop and drag new promo box component makes it through Methode environments to live"/ObjectMetadata/EditorialDisplayIndexing/DIMasterImgFileRef" //TODO remove once the drop and drag new promo box component makes it through Methode environments to live
    };

    private MethodeApiSmokeTestConfiguration methodeApiSmokeTestConfiguration;
    private Optional<EomFile> eomFile = Optional.absent();


    public MethodeApiSmokeTests() {
        final String configFileName = System.getProperty("test.methodeApi.configFile");
        LOGGER.debug("Environment configuration file name: {}", configFileName);

        Preconditions.checkNotNull(configFileName, "System property test.methodeApi.configFile is null");

        final ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        try {
            final File file = new File(configFileName).getCanonicalFile();
            methodeApiSmokeTestConfiguration = objectMapper.readValue(file, MethodeApiSmokeTestConfiguration.class);
            ApiConfig sampleApiConfig = methodeApiSmokeTestConfiguration.getMethodeApiConfigs().get(0);
            methodeApiServiceUrl = String.format("http://%s:%s/eom-file/", sampleApiConfig.getHost(), sampleApiConfig.getPort());
            LOGGER.debug("Configuration used : {}", methodeApiSmokeTestConfiguration);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        RestAssured.config= newConfig()
            .decoderConfig((decoderConfig().defaultContentCharset("UTF-8")))
            .encoderConfig((encoderConfig().defaultContentCharset("UTF-8")));
    }

    @Before
    public void ensureServicesAreRunning(){
        ensureMethodeApiIsRunning();
    }


    public void ensureMethodeApiIsRunning() {
        for(ApiConfig methodeApiConfig: methodeApiSmokeTestConfiguration.getMethodeApiConfigs()){
            String healthcheckUrl = String.format("http://%s:%s/healthcheck", methodeApiConfig.getHost(), methodeApiConfig.getAdminPort());
            givenServiceIsRunning(healthcheckUrl);
        }
    }


    public UUID ensureArticleExistsInMethode() {
        prepareTheArticle();

        EomFile methodeArticle = eomFile.get();
        LOGGER.debug("Calling Methode API: url= {} with data= {}", methodeApiServiceUrl, methodeArticle);

        Response response =
            given()
                .contentType(ContentType.JSON)
                .request().body(methodeArticle)
            .expect()
                .statusCode(200)
                .body("uuid", notNullValue())
                .log().ifError()
            .when()
                .post(methodeApiServiceUrl)
                .andReturn();

		return UUID.fromString(response.jsonPath().getString("uuid"));
    }

    private void prepareTheArticle() {
        String stampedHeadline = String.format("Proudly tested with robotic consistency [Build %s]", buildNo());
        EomFile kitchenSinkArticle = ReferenceArticles.publishedKitchenSinkArticle()
                .withHeadline(stampedHeadline)
                .withWorkflowStatus(MethodeContent.WEB_READY)
                .build()
                .getEomFile();
        eomFile = Optional.fromNullable(kitchenSinkArticle);

        LOGGER.debug("Test article headline={}, articleXml={}, attributeXml={}", stampedHeadline, kitchenSinkArticle.getValue(), kitchenSinkArticle.getAttributes());
    }

    private String buildNo() {
        return Objects.firstNonNull(System.getProperty("BUILD_NUMBER"),"LOCAL BUILD");
    }

    @Test
    public void shouldReturnTheArticleWhenAttemptedtoAccessItFromMethode() throws Throwable {
        for(ApiConfig methodeApiConfig : methodeApiSmokeTestConfiguration.getMethodeApiConfigs()){

            UUID uuid = ensureArticleExistsInMethode();
            EomFile methodeTestArticle = eomFile.get();

            String url = getEomFileUrl(methodeApiConfig, uuid.toString());
            LOGGER.debug("Calling Methode API: url=" + url);
            Response theResponse =
                given()
                    .contentType(ContentType.JSON)
                .expect().statusCode(200)
                    .log().ifError()
                .when()
                    .get(url);

            String responseAsString = theResponse.asString();
            assertThat("no body returned", responseAsString, notNullValue());
            assertThat("uuid didn't match", from(responseAsString).getString("uuid"), Matchers.equalTo(uuid.toString()));

            String significantXmlSource = Xml.removeInsignificantXml(from(responseAsString).getString("attributes"), INSIGNIFICANT_XPATHS);
            String expectedSignificantXmlSource = Xml.removeInsignificantXml(methodeTestArticle.getAttributes(), INSIGNIFICANT_XPATHS);
            assertThat("significant XML in attributes differed", significantXmlSource, Matchers.equalTo(expectedSignificantXmlSource));

            assertThat("workflow statuses didn't match", from(responseAsString).getString("workflowStatus"), Matchers.equalTo(methodeTestArticle.getWorkflowStatus()));

            byte[] retreivedContent =  from(responseAsString).getObject("", EomFile.class).getValue();
            assertThat("bytes in file differed", retreivedContent, Matchers.equalTo(methodeTestArticle.getValue()));
        }
    }

    private String getEomFileUrl(ApiConfig methodeApiConfig, String uuid) {
        return String.format("http://%s:%d/eom-file/%s", methodeApiConfig.getHost(), methodeApiConfig.getPort(), uuid);
    }

    private void givenServiceIsRunning(final String url) {
        LOGGER.debug("Checking service is running: url=" + url);
        expect()
            .statusCode(200)
            .log().ifError()
        .when()
             .get(url);
    }
}