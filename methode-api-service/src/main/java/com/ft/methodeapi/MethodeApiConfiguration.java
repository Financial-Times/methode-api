package com.ft.methodeapi;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ft.methodeapi.service.methode.MethodeConnectionConfiguration;
import com.ft.ws.lib.swagger.SwaggerConfiguration;
import com.ft.ws.lib.swagger.SwaggerConfigurationStrategy;
import com.google.common.base.Objects;
import com.yammer.dropwizard.config.Configuration;

public class MethodeApiConfiguration extends Configuration implements SwaggerConfigurationStrategy {

    private final MethodeConnectionConfiguration methodeConnectionConfiguration;
    private final MethodeConnectionConfiguration methodeTestConnectionConfiguration;
    private final long maxPingMillis;
	@NotNull
	@JsonProperty
	private SwaggerConfiguration swaggerConfiguration;

    public MethodeApiConfiguration(@JsonProperty("methodeConnection") MethodeConnectionConfiguration methodeConnectionConfiguration,
                                   @JsonProperty("methodeTestConnection") MethodeConnectionConfiguration methodeTestConnectionConfiguration,
                                   @JsonProperty("maxPingMillis") long maxPingMillis,
								   @JsonProperty("swaggerConfiguration") SwaggerConfiguration swaggerConfiguration) {
        this.methodeConnectionConfiguration = methodeConnectionConfiguration;
        this.methodeTestConnectionConfiguration = methodeTestConnectionConfiguration;
        this.maxPingMillis = maxPingMillis;
		this.swaggerConfiguration = swaggerConfiguration;
    }

    @Valid
    @NotNull
    public MethodeConnectionConfiguration getMethodeConnectionConfiguration() {
        return methodeConnectionConfiguration;
    }

    @Valid
    @NotNull
    public MethodeConnectionConfiguration getMethodeTestConnectionConfiguration() {
        return methodeTestConnectionConfiguration;
    }

    @Min(1L)
    public long getMaxPingMillis() {
        return maxPingMillis;
    }

	@Override
	public SwaggerConfiguration getSwaggerConfiguration() {
	    return swaggerConfiguration;
	}
    
    protected Objects.ToStringHelper toStringHelper() {
        return Objects.toStringHelper(this)
                .add("super", super.toString())
                .add("methodeConnectionConfiguration", methodeConnectionConfiguration)
                .add("methodeTestConnectionConfiguration", methodeTestConnectionConfiguration)
                .add("maxPingMillis", maxPingMillis);
    }
    
    @Override
    public String toString() {
        return toStringHelper().toString();
    }
}
