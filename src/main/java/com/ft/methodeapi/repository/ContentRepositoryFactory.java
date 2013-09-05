package com.ft.methodeapi.repository;

import EOM.RepositoryError;
import EOM.RepositoryPackage.InvalidLogin;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.NotFound;

public interface ContentRepositoryFactory {
    ContentRepository newRepository();
}
