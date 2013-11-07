package com.ft.methodeapi.acceptance;

import com.ft.methodeapi.model.EomFile;
import com.google.common.base.Charsets;
import com.google.common.base.Objects;
import com.google.common.io.Resources;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.config.DecoderConfig.decoderConfig;
import static com.jayway.restassured.config.RestAssuredConfig.newConfig;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static com.jayway.restassured.path.json.JsonPath.from;

public class StepDefs {

    private static final Logger LOGGER = LoggerFactory.getLogger(StepDefs.class);
    public static final String HEADLINE_FROM_TEST_FILE = "Eurozone collapses ...! Kaboom.";


    private AcceptanceTestConfiguration acceptanceTestConfiguration;

	private Response theResponse;
    private String theResponseEntity;
    private UUID theUuid;

    private List<UUID> createdArticles;
    private EomFile theExpectedArticle;


    public StepDefs(AcceptanceTestConfiguration acceptanceTestConfiguration) throws IOException {
    	this.acceptanceTestConfiguration = acceptanceTestConfiguration;

		RestAssured.config= newConfig().decoderConfig((decoderConfig().defaultContentCharset("UTF-8")));
    }

    @Before
    public void createCreatedArticleList() {
        createdArticles = new ArrayList<>();
    }

    @After
    public void cleanUpCreatedArticles() {
        for(UUID uuid : createdArticles) {

            String deleteUrl = acceptanceTestConfiguration.getMethodeApiServiceUrl() + uuid;
            LOGGER.info("Calling DELETE to clean up url={}",deleteUrl);
            expect().statusCode(either(is(204)).or(is(404)))
                    .when().delete(deleteUrl);
        }
    }


	@Given("^the MethodeAPI service is running and connected to Methode$")
	public void the_methode_api_service_is_running() throws Throwable {
		given_service_is_running(acceptanceTestConfiguration.getMethodeApiHealthcheckUrl());
	}


	@Given("^an article exists in Methode$")
	public void an_article_exists_in_Methode() throws Throwable {

        prepareTheArticle();

		LOGGER.info("Calling Methode API: url=" + acceptanceTestConfiguration.getMethodeApiServiceUrl()
				+ " with data=" + theExpectedArticle);

        Response response =
            given()
                .contentType(ContentType.JSON)
                .request().body(theExpectedArticle)
            .expect().statusCode(200)
                .body("uuid", notNullValue())
                .log().ifError()
            .when()
                .post(this.acceptanceTestConfiguration.getMethodeApiServiceUrl()).andReturn();

        theUuid = UUID.fromString(response.jsonPath().getString("uuid"));

        createdArticles.add(theUuid);
	}

    private void prepareTheArticle() throws IOException {
        String exampleArticleXml = readFromFile("exampleArticleData.txt");
        String attributesXml = readFromFile("exampleArticleAttributes.txt");

        String stampedHeadline = String.format("Proudly tested with robotic consistency [Build %s]", buildNo());
        exampleArticleXml = exampleArticleXml.replace(HEADLINE_FROM_TEST_FILE,stampedHeadline);
        attributesXml = attributesXml.replace(HEADLINE_FROM_TEST_FILE,stampedHeadline);

        LOGGER.debug("Test article headline={}, articleXml={}, attributeXml={}",stampedHeadline, exampleArticleXml,attributesXml);

        theExpectedArticle = new EomFile("","EOM::CompoundStory",
                exampleArticleXml.getBytes(Charsets.UTF_8),
                attributesXml
            );
    }

    private String buildNo() {
        return Objects.firstNonNull(System.getProperty("BUILD_NUMBER"),"LOCAL BUILD");
    }

    @Then("^I attempt to access the article$")
	public void i_attempt_to_access_the_article() throws Throwable {
		String url = acceptanceTestConfiguration.getMethodeApiServiceUrl() + theUuid.toString();
		LOGGER.info("Calling Methode API: url=" + url);
		theResponse =
			given()
				.contentType(ContentType.JSON)
			.expect().statusCode(200)
				.log().ifError()
			.when()
				.get(url);

        theResponseEntity = theResponse.asString();
	}
	
	@Then("^the article should be available from the MethodeAPI$")
	public void the_article_should_be_available_from_the_MethodeAPI() throws Throwable {
        assertThat("no body returned", theResponseEntity, notNullValue());
	}
	
	@Then("^the article should have the expected metadata$")
	public void the_article_should_have_the_expected_metadata() throws Throwable {
		assertThat("uuid didn't match", from(theResponseEntity).getString("uuid"), equalTo(theUuid.toString()));
        // TODO this needs uncommenting, or replacing with something more subtle.
        //assertThat("text in attributes differed", from(theResponseEntity).getString("attributes"), equalTo(theExpectedArticle.getAttributes()));
	}
	
	@Then("^the article should have the expected content$")
	public void the_article_should_have_the_expected_content() throws Throwable {
        byte[] retreivedContent =  from(theResponseEntity).getObject("", EomFile.class).getValue();
        assertThat("bytes in file differed", retreivedContent, equalTo(theExpectedArticle.getValue()));
	}


    private byte[] readBytesFromFile(String resourceName) throws IOException {
        return Resources.asByteSource(Resources.getResource(resourceName)).read();
    }

	private String readFromFile(String resourceName) throws IOException {
		String bodyFromFile = Resources.toString(Resources.getResource(resourceName), Charsets.UTF_8);
		// because what we get back from the API uses UNIX line encodings, but when working locally on Windows, the expected file will have \r\n
		if (System.getProperty("line.separator").equals("\r\n")) {
			bodyFromFile = bodyFromFile.replace("\r", "");
		}
		return bodyFromFile;
	}

	private void given_service_is_running(final String url) throws Throwable {
		LOGGER.info("Checking service is running: url=" + url);
		expect().statusCode(200)
			.log().ifError()
			.when().get(url);
	}
}
