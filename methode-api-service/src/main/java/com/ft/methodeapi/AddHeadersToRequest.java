package com.ft.methodeapi;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.*;

public class AddHeadersToRequest extends HttpServletRequestWrapper {

	private Map<String, String> additionalHeaders;

	public AddHeadersToRequest(HttpServletRequest request) {
		super(request);
		additionalHeaders = new HashMap<>();
	}

	public void addHeader(String name, String value) {
		additionalHeaders.put(name, value);
	}

	@Override
	public String getHeader(String name) {
		String header = super.getHeader(name);
		return (header != null) ? header : additionalHeaders.get(name);
	}

	@Override
	public Enumeration<String> getHeaderNames() {
		List<String> names = Collections.list(super.getHeaderNames());
		names.addAll(additionalHeaders.keySet());
		return Collections.enumeration(names);
	}

	@Override
	public Enumeration<String> getHeaders(String name) {
		Enumeration<String> headers = super.getHeaders(name);
		if (headers != null && headers.hasMoreElements()) {
			return headers;
		} else {
			return Collections.enumeration(Arrays.asList(additionalHeaders.get(name)));
		}
	}
}
