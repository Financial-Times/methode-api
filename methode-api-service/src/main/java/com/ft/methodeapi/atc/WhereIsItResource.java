package com.ft.methodeapi.atc;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Map;

/**
 * WhereIsItResource
 *
 * @author Simon.Gibbs
 */
@Path("/whereisit")
@Produces(MediaType.APPLICATION_JSON)
public class WhereIsItResource {

    private AirTrafficController atc;


    public WhereIsItResource(AirTrafficController atc) {
        this.atc = atc;
    }

    @GET
    public WhereIsItResponse whereIsIt() {

        Map<DataCentre,String> methodeIps = atc.reportIps();

        String activeIp = methodeIps.get(DataCentre.ACTIVE);

        DataCentre activeDc = atc.whois(activeIp, methodeIps);

        return new WhereIsItResponse(activeDc,methodeIps);
    }

}
