package com.ft.methodeApi.connectivity;

import EOM.Repository;
import EOM.RepositoryError;
import EOM.RepositoryPackage.InvalidLogin;
import EOM.Session;

public class EomSessionFactory {

	private final String methodeUserName;
	private final String methodePassword;
	private final Repository repository;

	public EomSessionFactory(String methodeUserName, String methodePassword, Repository repository) {
		this.methodeUserName = methodeUserName;
		this.methodePassword = methodePassword;
		this.repository = repository;
	}

	public EomSessionWrapper createSession() throws InvalidLogin, RepositoryError {
		return new EomSessionWrapper(repository.login(methodeUserName,
							methodePassword, "", null));
	}
}
