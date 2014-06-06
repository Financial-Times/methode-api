package com.ft.methodeapi.acceptance;

import com.ft.methodeapi.model.EomFile;
import com.ft.methodetesting.MethodeArticle;
import com.ft.methodetesting.xml.Xml;
import com.google.common.base.Objects;
import com.ft.methodetesting.ReferenceArticles;
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
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.config.DecoderConfig.decoderConfig;
import static com.jayway.restassured.config.EncoderConfig.encoderConfig;
import static com.jayway.restassured.config.RestAssuredConfig.newConfig;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static com.jayway.restassured.path.json.JsonPath.from;

public class StepDefs {

    private static final Logger LOGGER = LoggerFactory.getLogger(StepDefs.class);

    /**
     * Used to identify XML that is not significant for comparison purposes when considering
     * two examples of Methode attributes XML.
     */
    private static final String[] INSIGNIFICANT_XPATHS = {
            "/ObjectMetadata/OutputChannels/DIFTcom/DIFTcomSafeToSyndicate" ,
            "/ObjectMetadata/EditorialDisplayIndexing//DIBylineCopy" /* TODO we can resume testing this if the methode bug goes away */
    };

    private AcceptanceTestConfiguration acceptanceTestConfiguration;

	private Response theResponseForNotFoundRequest;
    private String theResponseEntityForSuccessfulRequest;
    private UUID uuidForArticleInMethode;
    private UUID uuidForNonExistentArticle;

    private EomFile theExpectedArticle;

    private List<Long> requestTimings;


    public StepDefs(AcceptanceTestConfiguration acceptanceTestConfiguration) throws IOException {
    	this.acceptanceTestConfiguration = acceptanceTestConfiguration;

		RestAssured.config= newConfig()
                .decoderConfig((decoderConfig().defaultContentCharset("UTF-8")))
                .encoderConfig((encoderConfig().defaultContentCharset("UTF-8")));
    }

    @Before
    public void setup() {
        requestTimings = new ArrayList<>(1000);
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

        uuidForArticleInMethode = UUID.fromString(response.jsonPath().getString("uuid"));
	}

	@Given("^an article does not exist in Methode$")
	public void an_article_does_not_exist_in_Methode() throws Throwable {
		uuidForNonExistentArticle = UUID.randomUUID();
	}

    private void prepareTheArticle() throws IOException {

        String stampedHeadline = String.format("Proudly tested with robotic consistency [Build %s]", buildNo());
        theExpectedArticle = ReferenceArticles.publishedKitchenSinkArticle()
                .withHeadline(stampedHeadline)
                .withWorkflowStatus(MethodeArticle.WEB_READY).build().getEomFile();

        LOGGER.debug("Test article headline={}, articleXml={}, attributeXml={}",stampedHeadline, theExpectedArticle.getValue(),theExpectedArticle.getAttributes());
    }

    private String buildNo() {
        return Objects.firstNonNull(System.getProperty("BUILD_NUMBER"),"LOCAL BUILD");
    }

    @When("^I attempt to access the article$")
	public void i_attempt_to_access_the_article() throws Throwable {
		String url = acceptanceTestConfiguration.getMethodeApiServiceUrl() + uuidForArticleInMethode.toString();
		LOGGER.info("Calling Methode API: url=" + url);
		Response theResponse =
			given()
				.contentType(ContentType.JSON)
			.expect().statusCode(200)
				.log().ifError()
			.when()
				.get(url);
		
		theResponseEntityForSuccessfulRequest = theResponse.asString();
	}

    @When("^(\\d+) users access the article a total of (\\d+) times$")
    public void i_attempt_to_access_the_article_count_times(int users, int count) throws Throwable {
        final String url = acceptanceTestConfiguration.getMethodeApiServiceUrl() + uuidForArticleInMethode.toString();
        LOGGER.info("Calling Methode API: url=" + url);

        ExecutorService userPool = Executors.newFixedThreadPool(users);
        List<Future<Long>> futureTimings = new ArrayList<>(count);

        for(int i=0; i<count; i++) {

            futureTimings.add(userPool.submit(new Callable<Long>() {

                @Override
                public Long call() throws Exception {
                    long startTime = System.currentTimeMillis();
                    Response theResponse =
                            given()
                                    .contentType(ContentType.JSON)
                                    .expect().statusCode(200)
                                    .log().ifError()
                                    .when()
                                    .get(url);

                    theResponseEntityForSuccessfulRequest = theResponse.asString();

                    return System.currentTimeMillis() - startTime;
                }
            }));

        }

        for(Future<Long> timing : futureTimings) {
            requestTimings.add(timing.get());
        }

        userPool.shutdown();

    }


    @When("^I attempt to access the non-existent article$")
	public void i_attempt_to_access_the_non_existent_article() throws Throwable {
		String url = acceptanceTestConfiguration.getMethodeApiServiceUrl() + uuidForNonExistentArticle.toString();
		LOGGER.info("Calling Methode API: url=" + url);
		theResponseForNotFoundRequest =
			given()
				.contentType(ContentType.JSON)
			.get(url);
	}
	
	@Then("^the article should be available from the MethodeAPI$")
	public void the_article_should_be_available_from_the_MethodeAPI() throws Throwable {
        assertThat("no body returned", theResponseEntityForSuccessfulRequest, notNullValue());
	}
	
	@Then("^the article should not be available from the MethodeAPI$")
	public void the_article_should_not_be_available_from_the_MethodeAPI() throws Throwable {
        assertThat("didn't get 404 not found", theResponseForNotFoundRequest.statusCode(), equalTo(404));
	}
	
	@Then("^the article should have the expected metadata$")
	public void the_article_should_have_the_expected_metadata() throws Throwable {
		assertThat("uuid didn't match", from(theResponseEntityForSuccessfulRequest).getString("uuid"), equalTo(uuidForArticleInMethode.toString()));

        String significantXmlSource = Xml.removeInsignificantXml(from(theResponseEntityForSuccessfulRequest).getString("attributes"), INSIGNIFICANT_XPATHS);
        String expectedSignificantXmlSource = Xml.removeInsignificantXml(theExpectedArticle.getAttributes(), INSIGNIFICANT_XPATHS);

        assertThat("significant XML in attributes differed", significantXmlSource, equalTo(expectedSignificantXmlSource));
	}

	@Then("^the article should have the expected workflow status$")
	public void the_article_should_have_the_expected_wokflow_status() throws Throwable {
		assertThat("workflow statuses didn't match", from(theResponseEntityForSuccessfulRequest).getString("workflowStatus"), equalTo(theExpectedArticle.getWorkflowStatus()));
	}
    @Then("^the article should have the expected content$")
	public void the_article_should_have_the_expected_content() throws Throwable {
        byte[] retreivedContent =  from(theResponseEntityForSuccessfulRequest).getObject("", EomFile.class).getValue();
        assertThat("bytes in file differed", retreivedContent, equalTo(theExpectedArticle.getValue()));
	}


    @Then("^it is returned within (\\d+)ms at least (\\d+)% of the time$")
    public void it_is_returned_within_MAX_ms_at_least_PERCENT_of_the_time(long max, double percent) {
        long numberOfFastRequests = 0;
        for(Long duration : requestTimings) {
            if(duration < max) {
                numberOfFastRequests++;
            }
        }

        double percentageOfFastRequests = (((double) numberOfFastRequests) / ((double) requestTimings.size())) * 100d;

        assertThat("Too many slow requests",percentageOfFastRequests,greaterThan(percent));
    }

	private void given_service_is_running(final String url) throws Throwable {
		LOGGER.info("Checking service is running: url=" + url);
		expect().statusCode(200)
			.log().ifError()
			.when().get(url);
	}
}
