package com.epam.ta.reportportal.auth.token.extractor.decorator;

import java.util.Collections;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.authentication.TokenExtractor;

public class ExcludedPathTokenExtractor implements TokenExtractor {

  private final TokenExtractor delegate;
  private final List<String> excludedPaths;

  public ExcludedPathTokenExtractor(List<String> excludedPaths, TokenExtractor defaultExtractor) {
    this.delegate = defaultExtractor;
    this.excludedPaths = excludedPaths;
  }

  public ExcludedPathTokenExtractor(String excludedPath, TokenExtractor defaultExtractor) {
    this(Collections.singletonList(excludedPath), defaultExtractor);
  }

  @Override
  public Authentication extract(HttpServletRequest request) {
    String requestURI = request.getRequestURI();
    if (excludedPaths.stream().noneMatch(requestURI::contains)) {
      return delegate.extract(request);
    }
    return null;
  }
}
