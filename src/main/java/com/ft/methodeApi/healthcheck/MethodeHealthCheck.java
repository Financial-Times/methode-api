package com.ft.methodeApi.healthcheck;

import EOM.FileSystemAdmin;
import com.yammer.metrics.core.HealthCheck;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

import java.util.Properties;

public class MethodeHealthCheck extends HealthCheck {

	private final String methodeHostName;
	private final int methodePort;
	private final String methodeUserName;
	private final String methodePassword;

	public MethodeHealthCheck(String methodeHostName, int methodePort, String methodeUserName, String methodePassword) {
		super("Methode Health Check");

		this.methodeHostName = methodeHostName;
		this.methodePort = methodePort;
		this.methodeUserName = methodeUserName;
		this.methodePassword = methodePassword;
	}

	@Override
	protected Result check() throws Exception {
		ORB orb = null;
		EOM.Session eomSession = null;

		String corbaLocation = "corbaloc:iiop:"
				+ methodeHostName + ":"
				+ methodePort + "/NameService";

		String[] orbInits = {"-ORBInitRef", "NS=" + corbaLocation};
		orb = ORB.init(orbInits, new Properties());

		NamingContextExt namingService = NamingContextExtHelper.narrow(orb
				.resolve_initial_references("NS"));
		EOM.Repository eomRepo = EOM.RepositoryHelper.narrow(namingService
				.resolve_str("EOM/Repositories/cms"));

		eomSession = eomRepo.login(methodeUserName,
				methodePassword, "", null);

		FileSystemAdmin fileSystemAdmin = EOM.FileSystemAdminHelper
				.narrow(eomSession
						.resolve_initial_references("FileSystemAdmin"));

		return Result.healthy();
	}
}
