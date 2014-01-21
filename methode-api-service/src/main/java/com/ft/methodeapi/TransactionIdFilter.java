package com.ft.methodeapi;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class TransactionIdFilter implements Filter {

	private static final Logger LOGGER = LoggerFactory.getLogger(TransactionIdFilter.class);
	private static final String TRANSACTION_ID_HEADER = "X-Request-Id";

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
		HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
		AddHeadersToRequest requestWithTransactionId = new AddHeadersToRequest(httpServletRequest);
		String transactionId = getOrGenerateTransactionIdAsWellAsSetItOnTheRequestForFutureOperationsToUse(requestWithTransactionId);

		filterChain.doFilter(requestWithTransactionId , servletResponse);

		HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
		httpServletResponse.setHeader(TRANSACTION_ID_HEADER, transactionId);
	}

	private String getOrGenerateTransactionIdAsWellAsSetItOnTheRequestForFutureOperationsToUse(AddHeadersToRequest request) {
		String transactionId = request.getHeader(TRANSACTION_ID_HEADER);
		if (StringUtils.isEmpty(transactionId)) {
			LOGGER.warn("Transaction ID ({} header) not provided. It will be generated.", TRANSACTION_ID_HEADER);
			transactionId = generateTransactionId();

			request.addHeader(TRANSACTION_ID_HEADER, transactionId);
		}

		LOGGER.info("message=\"Publish request.\" transaction_id={}.", transactionId);
		return transactionId;
	}

	private String generateTransactionId() {
		return "tid_" + randomChars(10);
	}

	private String randomChars(int howMany) {
		return RandomStringUtils.randomAlphanumeric(howMany).toLowerCase();
	}

	@Override
	public void destroy() {
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}
}
