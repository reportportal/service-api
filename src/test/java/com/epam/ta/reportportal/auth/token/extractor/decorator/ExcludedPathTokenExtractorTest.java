package com.epam.ta.reportportal.auth.token.extractor.decorator;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.authentication.TokenExtractor;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ExcludedPathTokenExtractorTest {
	private final String PUBLIC_PATH = "/v1/plugin/public";
	private final TokenExtractor delegate = mock(TokenExtractor.class);
	private final List<String> exludedPaths = List.of(PUBLIC_PATH, "some/excluded/path");
	private final ExcludedPathTokenExtractor excludedPathsTokenExtractor = new ExcludedPathTokenExtractor(exludedPaths, delegate);
	private final ExcludedPathTokenExtractor excludedPathTokenExtractor = new ExcludedPathTokenExtractor(PUBLIC_PATH, delegate);

	@Test
	public void extractShouldReturnNullForExcludedPaths() {
		HttpServletRequest request1 = mock(HttpServletRequest.class);
		when(request1.getRequestURI()).thenReturn("/v1/plugin/public/public_executeCommand");

		HttpServletRequest request2 = mock(HttpServletRequest.class);
		when(request2.getRequestURI()).thenReturn("/some/excluded/path/someCommand");

		Authentication resultForRequest1 = excludedPathsTokenExtractor.extract(request1);
		Authentication resultForRequest2 = excludedPathsTokenExtractor.extract(request2);

		Authentication resultForRequest3 = excludedPathTokenExtractor.extract(request1);
		Authentication resultForRequest4 = excludedPathTokenExtractor.extract(request2);

		assertNull(resultForRequest1);
		assertNull(resultForRequest2);

		assertNull(resultForRequest3);
		assertNull(resultForRequest4);
	}

	@Test
	public void extractShouldReturnNonNullForNotExcludedPaths() {
		Authentication authentication = mock(Authentication.class);

		HttpServletRequest request1 = mock(HttpServletRequest.class);
		when(request1.getRequestURI()).thenReturn("/v1/plugin/executeCommand");
		when(delegate.extract(eq(request1))).thenReturn(authentication);

		HttpServletRequest request2 = mock(HttpServletRequest.class);
		when(request2.getRequestURI()).thenReturn("/some/path/someCommand");
		when(delegate.extract(eq(request2))).thenReturn(authentication);

		Authentication resultForRequest1 = excludedPathsTokenExtractor.extract(request1);
		Authentication resultForRequest2 = excludedPathsTokenExtractor.extract(request2);

		Authentication resultForRequest3 = excludedPathTokenExtractor.extract(request1);
		Authentication resultForRequest4 = excludedPathTokenExtractor.extract(request2);

		assertNotNull(resultForRequest1);
		assertNotNull(resultForRequest2);

		assertNotNull(resultForRequest3);
		assertNotNull(resultForRequest4);

		verify(delegate, times(4)).extract(any(HttpServletRequest.class));
	}

}
