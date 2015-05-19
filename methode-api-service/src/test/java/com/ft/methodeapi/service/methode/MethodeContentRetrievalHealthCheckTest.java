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
import static org.junit.Assert.assertThat;

public class MethodeContentRetrievalHealthCheckTest {

	@ClassRule
	public static final DropwizardServiceRule<MethodeApiConfiguration> serviceRule = new DropwizardServiceRule<>(MethodeApiService.class, "methode-api-wrong-nsport.yaml");
    public static final String NOT_YET_CHECKED = "Not yet checked";

    @Test
	public void shouldTimeOutWhenInvalidPort() {
        final Client client = Client.create();
        client.setReadTimeout(20000);
        final URI uri = buildHealthCheckUri();

        ClientResponse clientResponse = waitForHealthChecks(client, uri);

        assertThat("response", clientResponse, hasProperty("status", equalTo(500)));
	}

    private ClientResponse waitForHealthChecks(Client client, URI uri) {
        ClientResponse clientResponse = null;
        boolean waiting = true;

        while(waiting) {
            clientResponse = client.resource(uri).get(ClientResponse.class);
            String responseText = clientResponse.getEntity(String.class);

            waiting = responseText.contains(NOT_YET_CHECKED);
            if(waiting) {
                try { Thread.sleep(100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }

        }
        return clientResponse;
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
