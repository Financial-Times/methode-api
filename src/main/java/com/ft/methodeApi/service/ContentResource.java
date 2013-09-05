package com.ft.methodeApi.service;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import com.yammer.metrics.annotation.Timed;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.NotFound;

@Path("content")
public class ContentResource {

//	private final String methodeHostName;
//	private final int methodePort;
//	private final String methodeUserName;
//	private final String methodePassword;

    @GET
    @Timed
    @Path("{uuid}")
    public Content getContent(@PathParam("uuid") String uuid) throws InvalidName, org.omg.CosNaming.NamingContextPackage.InvalidName, NotFound, CannotProceed {
//		try (EomRepositoryFactory orbResource = createOrbResource()) {
//			return new Content();
//		}
		return null;
	}
}
