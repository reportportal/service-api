/*
 * Copyright 2022 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
		return new MatchedPathTokenExtractor("/v1/plugin", "/v1/plugin/public", cookieTokenExtractor());
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
