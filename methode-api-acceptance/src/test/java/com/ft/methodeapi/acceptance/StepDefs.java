package com.ft.methodeapi.acceptance;

import com.ft.methodeapi.SetUpHelper;
import com.ft.methodeapi.acceptance.xml.Xml;
import com.ft.methodeapi.model.EomFile;
import com.ft.methodeapi.model.LinkedObject;
import com.google.common.base.MoreObjects;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;

import cucumber.api.Scenario;
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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static com.jayway.restassured.path.json.JsonPath.from;
import static com.ft.methodeapi.acceptance.LinkedObjectsVerifier.*;

public class StepDefs {

    private static final Logger LOGGER = LoggerFactory.getLogger(StepDefs.class);

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
    
    private static final String METHODE_ARTICLE_TYPE = "EOM::CompoundStory";
    
    private AcceptanceTestConfiguration acceptanceTestConfiguration;
    
    private Scenario scenario;
    
	private Response theResponseForNotFoundRequest;
    private String theResponseEntityForSuccessfulRequest;
    private UUID uuidForArticleInMethode;
    private UUID uuidForNonExistentContent;
    private UUID uuidForListInMethode;

    private EomFile theExpectedArticle;
    private EomFile theExpectedList;
    private List<LinkedObject> theActualLinkedObjects;

    private List<Long> requestTimings;


    public StepDefs(AcceptanceTestConfiguration acceptanceTestConfiguration) throws IOException {
    	this.acceptanceTestConfiguration = acceptanceTestConfiguration;

        SetUpHelper.configureRestAssuredEncoding();
    }

    @Before
    public void setup(Scenario scenario) {
        requestTimings = new ArrayList<>(1000);
        this.scenario = scenario;
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
        scenario.write(String.format("using article: %s", uuidForArticleInMethode));
	}

    @Given("^a list exists in Methode$")
    public void a_list_exists_in_Methode() throws Throwable {
        an_article_exists_in_Methode();
        prepareTheList();

        LOGGER.info("Calling Methode API: url=" + acceptanceTestConfiguration.getMethodeApiServiceUrl()
                + " with data=" + theExpectedList);

        Response response =
            given()
                .contentType(ContentType.JSON)
                .request().body(theExpectedList)
            .expect().statusCode(200)
                .body("uuid", notNullValue())
                .log().ifError()
            .when()
                .post(this.acceptanceTestConfiguration.getMethodeApiServiceUrl()).andReturn();

        uuidForListInMethode = UUID.fromString(response.jsonPath().getString("uuid"));
        scenario.write(String.format("using list: %s", uuidForListInMethode));
    }

	@Given("^an? (article|list) does not exist in Methode$")
	public void content_does_not_exist_in_Methode(String contentType) throws Throwable {
		uuidForNonExistentContent = UUID.randomUUID();
	}

    private void prepareTheArticle() throws IOException {

        String stampedHeadline = String.format("Proudly tested with robotic consistency [Build %s]", buildNo());
        theExpectedArticle = ReferenceArticles.publishedKitchenSinkArticle()
                .withHeadline(stampedHeadline)
                .withWorkflowStatus(MethodeContent.WEB_READY)
                .build().getEomFile();

        LOGGER.debug("Test article headline={}, articleXml={}, attributeXml={}", stampedHeadline,
                theExpectedArticle.getValue(), theExpectedArticle.getAttributes());
    }

    private void prepareTheList() throws IOException {

        List<LinkedObject> linkedObjects = Collections.singletonList(
                    new LinkedObject(uuidForArticleInMethode.toString(), METHODE_ARTICLE_TYPE)
                );
        
        theExpectedList = ReferenceLists.publishedList(linkedObjects)
                            .build().getEomFile();
        LOGGER.debug("articleXml={}, attributeXml={}, linkedObjects={}",theExpectedList.getValue(),
                theExpectedList.getAttributes(), theExpectedList.getLinkedObjects());
    }

    private String buildNo() {
        return MoreObjects.firstNonNull(System.getProperty("BUILD_NUMBER"), "LOCAL BUILD");
    }

    @When("^I attempt to access the (article|list)$")
	public void i_attempt_to_access_the_content(String contentType) throws Throwable {
		String url = getUrlForContent(contentType);

		LOGGER.info("Calling Methode API: url=" + url);
		Response theResponse =
			given()
				.contentType(ContentType.JSON)
			.expect().statusCode(200)
				.log().ifError()
			.when()
				.get(url);
		
		theResponseEntityForSuccessfulRequest = theResponse.asString();
        theActualLinkedObjects = extractLinkedObjects(theResponseEntityForSuccessfulRequest);
	}

    private String getUrlForContent(String contentType) {
        return acceptanceTestConfiguration.getMethodeApiServiceUrl()
                + ("article".equals(contentType) ? uuidForArticleInMethode : uuidForListInMethode).toString();
    }

    @When("^(\\d+) users access the (article|list) a total of (\\d+) times$")
    public void i_attempt_to_access_the_content_count_times(int users,String contentType, int count) throws Throwable {
        final String url = getUrlForContent(contentType);
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

    @When("^I attempt to access the non-existent (article|list)$")
	public void i_attempt_to_access_the_non_existent_content(String contentType) throws Throwable {
        String url = acceptanceTestConfiguration.getMethodeApiServiceUrl() + uuidForNonExistentContent.toString();
		LOGGER.info("Calling Methode API: url=" + url);
		theResponseForNotFoundRequest =
			given()
				.contentType(ContentType.JSON)
			.get(url);
	}
	
	@Then("^the (article|list) should be available from the MethodeAPI$")
	public void the_article_should_be_available_from_the_MethodeAPI(String contentType) throws Throwable {
        assertThat("no body returned", theResponseEntityForSuccessfulRequest, notNullValue());
	}
	
	@Then("^the (article|list) should not be available from the MethodeAPI$")
	public void the_content_should_not_be_available_from_the_MethodeAPI(String contentType) throws Throwable {
        assertThat("didn't get 404 not found", theResponseForNotFoundRequest.statusCode(), equalTo(404));
	}
	
	@Then("^the article should have the expected metadata$")
	public void the_article_should_have_the_expected_metadata() throws Throwable {
        checkMetadataForContent(uuidForArticleInMethode, theExpectedArticle);
	}

    @Then("^the list should have the expected metadata$")
    public void the_list_should_have_the_expected_metadata() throws Throwable {
        checkMetadataForContent(uuidForListInMethode, theExpectedList);
    }

    private void checkMetadataForContent(UUID uuid, EomFile eomFile) throws Exception {
        assertThat("uuid didn't match", from(theResponseEntityForSuccessfulRequest).getString("uuid"), equalTo(uuid.toString()));

        String significantXmlSource = Xml.removeInsignificantXml(from(theResponseEntityForSuccessfulRequest).getString("attributes"), INSIGNIFICANT_XPATHS);
        String expectedSignificantXmlSource = Xml.removeInsignificantXml(eomFile.getAttributes(), INSIGNIFICANT_XPATHS);

        System.out.println("significantXmlSource=" + significantXmlSource);

        assertThat("significant XML in attributes differed", significantXmlSource, equalTo(expectedSignificantXmlSource));
    }

	@Then("^the article should have the expected workflow status$")
	public void the_article_should_have_the_expected_wokflow_status() throws Throwable {
        assertThat("workflow statuses didn't match", from(theResponseEntityForSuccessfulRequest).getString("workflowStatus"),
                 equalTo(theExpectedArticle.getWorkflowStatus()));
	}

    @Then("^the list should have the expected workflow status$")
    public void the_list_should_have_the_expected_wokflow_status() throws Throwable {
        assertThat("workflow statuses didn't match", from(theResponseEntityForSuccessfulRequest).getString("workflowStatus"),
                 equalTo(theExpectedList.getWorkflowStatus()));
    }

    @Then("^the article should have the expected content$")
	public void the_article_should_have_the_expected_content() throws Throwable {
        byte[] retreivedContent =  from(theResponseEntityForSuccessfulRequest).getObject("", EomFile.class).getValue();
        assertThat("bytes in file differed", retreivedContent, equalTo(theExpectedArticle.getValue()));
	}

    @Then("^the list should have the expected content$")
    public void the_list_should_have_the_expected_content() throws Throwable {
        byte[] retreivedContent =  from(theResponseEntityForSuccessfulRequest).getObject("", EomFile.class).getValue();
        assertThat("bytes in file differed", retreivedContent, equalTo(theExpectedList.getValue()));
    }

    @Then("^the article should have the expected type value$")
    public void the_article_should_have_the_expected_type() throws Throwable {
        assertThat("file extension didn't match expected", from(theResponseEntityForSuccessfulRequest).getString("type"),
                   equalTo(theExpectedArticle.getType()));
    }

    @Then("^the list should have the expected type value$")
    public void the_list_should_have_the_expected_type() throws Throwable {
        assertThat("file extension didn't match expected",
                  from(theResponseEntityForSuccessfulRequest).getString("type"), equalTo(theExpectedList.getType()));
    }

    @Then("^each linked item in the list should have the expected uuid$")
    public void the_list_should_have_the_expected_linked_items_content() throws Throwable {
        assertThat("uuid of objects in list differed from expected", mapUuid(theActualLinkedObjects), equalTo(mapUuid(theExpectedList.getLinkedObjects())));
    }

    @Then("^each linked item in the list should have the expected type$")
    public void each_linked_item_in_the_list_should_have_the_expected_type() {
        assertThat("type of objects in list differed from expected", mapType(theActualLinkedObjects), equalTo(mapType(theExpectedList.getLinkedObjects())));
    }

    @Then("^each linked item in the list should have the expected workflow status$")
    public void each_linked_item_in_the_list_should_have_the_expected_workflow_status() {
        assertThat("Did not find status property", hasWorkflowStatusProperty(theActualLinkedObjects), equalTo(true));
    }

    @Then("^each linked item in the list should have the expected attributes$")
    public void each_linked_item_in_the_list_should_have_the_expected_attributes() {
        assertThat("Did not find attributes property", hasAttributesProperty(theActualLinkedObjects), equalTo(true));
    }

    @Then("^each linked item in the list should have the expected systemAttributes$")
    public void each_linked_item_in_the_list_should_have_the_expected_systemAttributes() {
        assertThat("Did not find systemAttributes property", hasSystemAttributesProperty(theActualLinkedObjects), equalTo(true));
    }

    @Then("^it is returned within (\\d+)ms at least (\\d+)% of the time$")
    public void it_is_returned_within_MAX_ms_at_least_PERCENT_of_the_time(long max, double percent) {
        long numberOfFastRequests = 0;
        for(Long duration : requestTimings) {
            if(duration < max) {
                numberOfFastRequests++;
            }
        }

        double percentageOfFastRequests = (((double)numberOfFastRequests) / ((double)requestTimings.size())) * 100d;

        assertThat("Too many slow requests",percentageOfFastRequests,greaterThan(percent));
    }

	private void given_service_is_running(final String url) throws Throwable {
        LOGGER.info("Checking service is running: url=" + url);
		expect().statusCode(200)
			.log().ifError()
			.when().get(url);
	}

    private List<LinkedObject> extractLinkedObjects(String theResponseEntityForSuccessfulRequest) {
        EomFile webContainer = from(theResponseEntityForSuccessfulRequest)
                .getObject("", EomFile.class);
        List<LinkedObject> linkedObjects =  webContainer.getLinkedObjects();
        return linkedObjects;
    }
}
