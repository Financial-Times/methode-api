package com.ft.methodeapi.connectivity;

import EOM.Session;

public class EomSessionWrapper implements AutoCloseable {

	private Session eomSession;

	public EomSessionWrapper(Session eomSession) {
		this.eomSession = eomSession;
	}

	@Override
	public void close() throws Exception {
		if (eomSession != null) {
			eomSession.destroy();
		}
	}

	public Session getSession() {
		return eomSession;
	}
}
