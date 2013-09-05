package com.ft.methodeApi.healthcheck;

import com.yammer.metrics.core.HealthCheck;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

import java.util.Properties;

public class MethodePingHealthCheck extends HealthCheck {

	private final String methodeHostName;
	private final int methodePort;

	public MethodePingHealthCheck(String methodeHostName, int methodePort) {
		super("Methode Health Check");

		this.methodeHostName = methodeHostName;
		this.methodePort = methodePort;
	}

	@Override
	protected Result check() throws Exception {
		ORB orb = null;

		try {
			String corbaLocation = "corbaloc:iiop:"
					+ methodeHostName + ":"
					+ methodePort + "/NameService";

			String[] orbInits = {"-ORBInitRef", "NS=" + corbaLocation};
			orb = ORB.init(orbInits, new Properties());

			NamingContextExt namingService = NamingContextExtHelper.narrow(orb
					.resolve_initial_references("NS"));
			EOM.Repository eomRepo = EOM.RepositoryHelper.narrow(namingService
					.resolve_str("EOM/Repositories/cms"));

			eomRepo.ping();

			return Result.healthy();
		} finally {
			if (orb != null) {
				close(orb);
			}
		}

	}

	private void close(ORB orb) {
		orb.shutdown(true);
		orb.destroy();
	}
}
