package com.ft.methodeapi.service.methode;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ft.methodeapi.service.methode.connection.PoolConfiguration;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.yammer.dropwizard.validation.PortRange;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Min;

public class MethodeConnectionConfiguration {

    private final String methodeHostName;
    private final int methodePort;
    private final String methodeUserName;
    private final String methodePassword;
	private final int connectTimeout;
    private final String orbClass;
    private final String orbSingletonClass;
    private final Optional<PoolConfiguration> pool;

    public MethodeConnectionConfiguration(@JsonProperty("hostName") String methodeHostName,
                                          @JsonProperty("nsPort") int methodePort,
                                          @JsonProperty("userName") String methodeUserName,
                                          @JsonProperty("password") String methodePassword,
										  @JsonProperty("connectTimeout") int connectTimeout,
                                          @JsonProperty("orbClass") Optional<String> orbClass,
                                          @JsonProperty("orbSingletonClass") Optional<String> orbSingletonClass,
                                          @JsonProperty("pool") Optional<PoolConfiguration> pool) {
        this.methodeHostName = methodeHostName;
        this.methodePort = methodePort;
        this.methodeUserName = methodeUserName;
        this.methodePassword = methodePassword;
		this.connectTimeout = connectTimeout;
        this.orbClass = orbClass.or("org.jacorb.orb.ORB");
        this.orbSingletonClass = orbSingletonClass.or("org.jacorb.orb.ORBSingleton");
        this.pool = pool;
    }

    @NotEmpty
    public String getMethodeHostName() {
        return methodeHostName;
    }

    @PortRange
    public int getMethodePort() {
        return methodePort;
    }

    @NotEmpty
    public String getMethodeUserName() {
        return methodeUserName;
    }

    @NotEmpty
    public String getMethodePassword() {
        return methodePassword;
    }

	@Min(1)
	public int getConnectTimeout() {
		return connectTimeout;
	}

	@NotEmpty
    public String getOrbClass() {
        return orbClass;
    }

    @NotEmpty
    public String getOrbSingletonClass() {
        return orbSingletonClass;
    }

    public Optional<PoolConfiguration> getPool() {
        return pool;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("methodeHostName", methodeHostName)
                .add("methodePort", methodePort)
                .add("methodeUserName", methodeUserName)
                // OBFUSCATE PASSWORD!
                .add("methodePassword", Strings.repeat("*",methodePassword.length()))
				.add("connectTimeout", connectTimeout)
                .add("orbClass", orbClass)
                .add("orbSingletonClass", orbSingletonClass)
                .add("pool",pool)
                .toString();
    }
}
