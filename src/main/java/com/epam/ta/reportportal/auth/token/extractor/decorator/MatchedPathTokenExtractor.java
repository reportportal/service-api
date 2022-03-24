package com.epam.ta.reportportal.auth.token.extractor.decorator;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.authentication.TokenExtractor;

import javax.servlet.http.HttpServletRequest;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class MatchedPathTokenExtractor implements TokenExtractor {

	private final String pathMatcher;
	private final TokenExtractor tokenExtractor;

	public MatchedPathTokenExtractor(String pathMatcher, TokenExtractor tokenExtractor) {
		this.pathMatcher = pathMatcher;
		this.tokenExtractor = tokenExtractor;
	}

	@Override
	public Authentication extract(HttpServletRequest request) {
		if (request.getRequestURI().contains(pathMatcher)) {
			return tokenExtractor.extract(request);
		}
		return null;
	}
}
