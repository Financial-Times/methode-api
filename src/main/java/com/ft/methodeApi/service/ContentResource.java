package com.ft.methodeApi.service;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import EOM.FileSystemAdmin;
import EOM.FileSystemObject;
import EOM.Repository;
import EOM.Session;
import com.ft.methodeApi.connectivity.EomRepositoryFactory;
import com.yammer.metrics.annotation.Timed;

@Path("content")
@Produces(MediaType.APPLICATION_JSON)
public class ContentResource {

	private final String methodeHostName;
	private final int methodePort;
	private final String methodeUserName;
	private final String methodePassword;

	public ContentResource(String methodeHostName, int methodePort, String methodeUserName, String methodePassword) {
		this.methodeHostName = methodeHostName;
		this.methodePort = methodePort;
		this.methodeUserName = methodeUserName;
		this.methodePassword = methodePassword;
	}

	@GET
    @Timed
    @Path("{uuid}")
    public Content getContent(@PathParam("uuid") String uuid) throws Exception {
		try (EomRepositoryFactory orbResource = new EomRepositoryFactory(methodeHostName, methodePort)) {
			Repository eomRepo = orbResource.createRepository();
			Session eomSession = eomRepo.login(methodeUserName,
					methodePassword, "", null);

			FileSystemAdmin fileSystemAdmin = EOM.FileSystemAdminHelper
					.narrow(eomSession
							.resolve_initial_references("FileSystemAdmin"));

			String uri = "eom:/uuids/" + uuid;

			FileSystemObject fso = fileSystemAdmin.get_object_with_uri(uri);
			String type = fso.get_type_name();

			if ("EOM::Story".equals(type) || "EOM::CompoundStory".equals(type)) {
				EOM.File eomFile = EOM.FileHelper.narrow(fso);
				Content content = new Content(eomFile.read_all());
				return content;
			} else {
				return null;
			}
		}
	}
}
