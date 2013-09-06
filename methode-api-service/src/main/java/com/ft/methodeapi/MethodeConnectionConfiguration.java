package com.ft.methodeapi;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;
import org.hibernate.validator.constraints.NotEmpty;

public class MethodeConnectionConfiguration {

    private final String methodeHostName;
    private final int methodePort;
    private final String methodeUserName;
    private final String methodePassword;
    private final String orbClass;
    private final String orbSingletonClass;

    public MethodeConnectionConfiguration(@JsonProperty("hostName") String methodeHostName,
                                          @JsonProperty("nsPort") int methodePort,
                                          @JsonProperty("userName") String methodeUserName,
                                          @JsonProperty("password") String methodePassword,
                                          @JsonProperty( "orbClass") String orbClass,
                                          @JsonProperty("orbSingletonClass") String orbSingletonClass) {
        this.methodeHostName = methodeHostName;
        this.methodePort = methodePort;
        this.methodeUserName = methodeUserName;
        this.methodePassword = methodePassword;
        this.orbClass = Optional.fromNullable(orbClass).or("org.jacorb.orb.ORB");
        this.orbSingletonClass = Optional.fromNullable(orbSingletonClass).or("org.jacorb.orb.ORBSingleton");
    }

    @NotEmpty
    public String getMethodeHostName() {
        return methodeHostName;
    }

    @Min(1) @Max(65535)
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

    @NotEmpty
    public String getOrbClass() {
        return orbClass;
    }

    @NotEmpty
    public String getOrbSingletonClass() {
        return orbSingletonClass;
    }
}
