package com.epam.ta.reportportal.auth.token.extractor.decorator;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.authentication.TokenExtractor;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Objects;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class DelegatingTokenExtractor implements TokenExtractor {

	private final List<TokenExtractor> extractors;

	public DelegatingTokenExtractor(List<TokenExtractor> extractors) {
		this.extractors = extractors;
	}

	@Override
	public Authentication extract(HttpServletRequest request) {
		return extractors.stream().map(ex -> ex.extract(request)).filter(Objects::nonNull).findFirst().orElse(null);
	}
}
