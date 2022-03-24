package com.epam.ta.reportportal.auth.token.extractor;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.authentication.TokenExtractor;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import javax.servlet.http.HttpServletRequest;

import java.util.Arrays;

import static java.util.Optional.ofNullable;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class CookieTokenExtractor implements TokenExtractor {

	public static final String TOKEN = "token";

	@Override
	public Authentication extract(HttpServletRequest request) {
		return ofNullable(request.getCookies()).flatMap(cookies -> Arrays.stream(cookies)
				.filter(cookie -> TOKEN.equals(cookie.getName()))
				.findFirst()).map(cookie -> new PreAuthenticatedAuthenticationToken(cookie.getValue(), "")).orElse(null);
	}
}
