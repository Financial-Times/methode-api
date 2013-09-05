package com.ft.methodeApi.healthcheck;

import com.yammer.metrics.core.HealthCheck;

public class MethodeHealthCheck extends HealthCheck {

	public MethodeHealthCheck() {
		super("Methode Health Check");
	}

	@Override
	protected Result check() throws Exception {
		return Result.unhealthy("Not implemented");
	}
}
