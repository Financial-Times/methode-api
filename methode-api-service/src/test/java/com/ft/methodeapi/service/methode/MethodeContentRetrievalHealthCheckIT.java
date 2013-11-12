package com.ft.methodeapi.service.methode;

import com.ft.methodeapi.MethodeApiConfiguration;
import com.ft.methodeapi.MethodeApiService;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.yammer.dropwizard.testing.junit.DropwizardServiceRule;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class MethodeContentRetrievalHealthCheckIT {

	private boolean methodReturned = false;
	private Exception exception = null;

	@ClassRule
	public static final DropwizardServiceRule<MethodeApiConfiguration> serviceRule = new DropwizardServiceRule<>(MethodeApiService.class, "methode-api-wrong-nsport.yaml");

	/**
	 * If there is no timeout set in JacORB, this test will hang forever.
	 */
	@Test
	public void shouldTimeOutWhenInvalidPort() {
		Thread thread = new Thread(new Runnable() {
			public void run() {
				try {
					final Client client = Client.create();
					final URI uri = buildHealthCheckUri();
					final ClientResponse clientResponse = client.resource(uri).get(ClientResponse.class);
					assertThat("response", clientResponse, hasProperty("status", equalTo(500)));
					methodReturned = true;
				} catch (Exception e) {
					exception = e;
				} finally {
					synchronized (this) {
						this.notify();
					}
				}
			}
		});
		thread.setDaemon(true);
		try {
			synchronized (this) {
				thread.start();
				this.wait(10000);
			}
		} catch (InterruptedException _ex) {
			fail("Unexpected failure: " + _ex);
		}

		if (exception == null) {
			assertThat("Call to Methode should have timed out, but didn't.", methodReturned, is(true));
		} else {
			fail("Unexpected failure: " + exception);
		}
	}

	private URI buildHealthCheckUri() {
		return UriBuilder
				.fromPath("healthcheck")
				.scheme("http")
				.host("localhost")
				.port(adminPort())
				.build();
	}

	private int adminPort() {
		return serviceRule.getConfiguration().getHttpConfiguration().getAdminPort();
	}
}
