package com.epam.ta.reportportal.core.configs.token.extractor;

import com.epam.ta.reportportal.auth.token.extractor.CookieTokenExtractor;
import com.epam.ta.reportportal.auth.token.extractor.decorator.DelegatingTokenExtractor;
import com.epam.ta.reportportal.auth.token.extractor.decorator.MatchedPathTokenExtractor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.provider.authentication.BearerTokenExtractor;
import org.springframework.security.oauth2.provider.authentication.TokenExtractor;

import java.util.List;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Configuration
public class TokenExtractorConfig {

	@Bean
	public TokenExtractor cookieTokenExtractor() {
		return new CookieTokenExtractor();
	}

	@Bean
	public TokenExtractor pluginTokenExtractor() {
		return new MatchedPathTokenExtractor("/v1/plugin", cookieTokenExtractor());
	}

	@Bean
	public TokenExtractor bearerTokenExtractor() {
		return new BearerTokenExtractor();
	}

	@Bean
	public TokenExtractor delegatingTokenExtractor() {
		return new DelegatingTokenExtractor(List.of(bearerTokenExtractor(), pluginTokenExtractor()));
	}
}
