package com.ft.methodeapi.service;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import java.net.URI;
import java.util.UUID;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import com.ft.api.util.transactionid.TransactionIdUtils;
import com.ft.methodeapi.model.EomFile;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;

import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;


public class MethodeOutageStepDefs {
	private ClientResponse response;
    
    private Client client;

    @Before
    public void setup() {
        client = Client.create();
        client.setReadTimeout(50000);
    }

    @Given("^that Methode is down$")
    public void that_methode_is_down() {
        assertThat(MethodeOutageTest.appRule.getAdminPort(), not(equalTo(9091)));
    }
    
    @Given("^that Methode throws an Exception$")
    public void that_methode_throws_an_exception() {
       // no-op
    }
    
    @When("^I attempt to access an article$")
    public void i_attempt_to_access_an_article() {
    	// avoid dependency cycles by NOT using the official client
        
        try {
            URI uri = UriBuilder.fromPath("/eom-file/{uuid}")
                                .scheme("http")
                                .host("localhost")
                                .port(MethodeOutageTest.appRule.getLocalPort())
                                .build(UUID.randomUUID());
            
            client.resource(uri).header(TransactionIdUtils.TRANSACTION_ID_HEADER, "tid_test").accept(MediaType.APPLICATION_JSON_TYPE).get(EomFile.class);
        } catch(UniformInterfaceException uie) {
        	response = uie.getResponse();
        }
    }
    
    @Then("^I should get a service unavailable error$")
    public void i_should_get_a_service_unavailable_error() {
    	assertThat(response.getStatus(),is(503));
    }
	
}
