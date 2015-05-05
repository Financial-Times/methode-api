package com.ft.methodeapi.integration;

import com.ft.methodeapi.SetUpHelper;
import com.ft.methodeapi.acceptance.AcceptanceTestConfiguration;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.URI;
import java.util.Map;

import static com.jayway.restassured.RestAssured.given;
import static junit.framework.Assert.assertNotNull;
import static org.hamcrest.Matchers.notNullValue;

/**
 * The "ATC" is supposed to know where active systems are and help ensure they are monitored.
 *
 * Also known as "Where is Methode".
 *
 * @author Simon.Gibbs
 */
public class AirTrafficControllerTest {

    private static AcceptanceTestConfiguration configuration;

    @BeforeClass
    public static void  preSetUp() {
        SetUpHelper.configureRestAssuredEncoding();
        configuration = SetUpHelper.readConfiguration("int-acceptance-test.yaml");
    }

    @Test
    public void shouldReportLocationsOfMethodeInstances() {
        URI uri = URI.create(String.format("http://%s:%d/where-is-methode",
                configuration.getMethodeApiConfig().getHost(),
                configuration.getMethodeApiConfig().getPort()
            ));

        Response response =
                given().contentType(ContentType.JSON)
                       .expect().statusCode(200)
                       .body("active", notNullValue())
                       .body("methodeIps", notNullValue())
                       .log().ifError()
                       .when()
                       .get(uri.toString()).andReturn();

        // At least assert that the active DC has an IP.
        String activeDc = response.jsonPath().getString("active");
        Map<String,String> ips = response.jsonPath().getMap("methodeIps");

        String activeIp = ips.get(activeDc);

        assertNotNull(activeIp);

    }



}
