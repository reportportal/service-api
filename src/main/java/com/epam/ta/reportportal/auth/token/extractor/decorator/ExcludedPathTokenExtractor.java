package com.epam.ta.reportportal.auth.token.extractor.decorator;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.authentication.TokenExtractor;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

public class ExcludedPathTokenExtractor implements TokenExtractor {

	private final TokenExtractor delegate;
	private final String[] EXCLUDED_PATHS = new String[] {
			"v1/plugin/public"
	};

	public ExcludedPathTokenExtractor(TokenExtractor defaultExtractor) {
		this.delegate = defaultExtractor;
	}

	@Override
	public Authentication extract(HttpServletRequest request) {
		String requestURI = request.getRequestURI();
		if (Arrays.stream(EXCLUDED_PATHS).noneMatch(requestURI::contains)) {
			return delegate.extract(request);
		}
		return null;
	}
}
