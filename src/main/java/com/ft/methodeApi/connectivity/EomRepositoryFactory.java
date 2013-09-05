package com.ft.methodeApi.connectivity;

import EOM.Repository;
import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.NotFound;

import java.util.Properties;

public class EomRepositoryFactory implements AutoCloseable {

	private final ORB orb;

	public EomRepositoryFactory(String methodeHostName, int methodePort) {
		String corbaLocation = "corbaloc:iiop:"
				+ methodeHostName + ":"
				+ methodePort + "/NameService";

		String[] orbInits = {"-ORBInitRef", "NS=" + corbaLocation};
		orb = ORB.init(orbInits, new Properties());
	}

	public Repository createRepository() throws InvalidName, org.omg.CosNaming.NamingContextPackage.InvalidName, NotFound, CannotProceed {
		NamingContextExt namingService = NamingContextExtHelper.narrow(orb
							.resolve_initial_references("NS"));
		EOM.Repository eomRepo = EOM.RepositoryHelper.narrow(namingService
				.resolve_str("EOM/Repositories/cms"));
		return eomRepo;
	}

	@Override
	public void close() throws Exception {
		if (orb != null) {
			orb.shutdown(true);
			orb.destroy();
		}
	}
}
