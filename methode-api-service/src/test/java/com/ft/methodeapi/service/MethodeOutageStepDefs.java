package com.ft.methodeapi.service;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.UUID;

import javax.ws.rs.core.MediaType;

import EOM.PermissionDenied;

import com.ft.methodeapi.model.EomFile;
import com.ft.methodeapi.service.http.EomFileResource;
import com.ft.methodeapi.service.methode.MethodeException;
import com.ft.methodeapi.service.methode.MethodeFileRepository;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.yammer.dropwizard.testing.ResourceTest;

import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

public class MethodeOutageStepDefs extends ResourceTest {

	private static final String TRANSACTION_ID_HEADER = "X-Request-Id";

	private MethodeFileRepository methodeFileRepository;
	private ClientResponse response;

    @Before
    public void setup() throws Exception {
    	// forces the @Before behaviour that is marked with a Junit @Before in the super class not a Cucumber one
    	super.setUpJersey();
    }
	
	@Override
    protected void setUpResources() throws Exception {
        methodeFileRepository = mock(MethodeFileRepository.class);
        addResource(new EomFileResource(methodeFileRepository));
    }
    
    @Given("^that Methode is down$")
    public void that_methode_is_down() {
    	when(methodeFileRepository.findFileByUuid(anyString())).thenThrow(new org.omg.CORBA.TRANSIENT("Synthetic exception"));
    }
    
    @Given("^that Methode throws an Exception$")
    public void that_methode_throws_an_exception() {
    	when(methodeFileRepository.findFileByUuid(anyString())).thenThrow(new MethodeException("Synthetic exception", new PermissionDenied()));
    }
    
    @When("^I attempt to access an article$")
    public void i_attempt_to_access_an_article() {
    	// avoid dependency cycles by NOT using the official client
        try {
            client().resource("/eom-file/").path(UUID.randomUUID().toString()).header(TRANSACTION_ID_HEADER, "tid_test").accept(MediaType.APPLICATION_JSON_TYPE).get(EomFile.class);
        } catch(UniformInterfaceException uie) {
        	response = uie.getResponse();
        }
    }
    
    @Then("^I should get a service unavailable error$")
    public void i_should_get_a_service_unavailable_error() {
    	assertThat(response.getStatus(),is(503));
    }
	
}
