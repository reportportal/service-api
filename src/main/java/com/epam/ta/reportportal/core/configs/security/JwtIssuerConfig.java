package com.epam.ta.reportportal.core.configs.security;

import lombok.Data;

@Data
public class JwtIssuerConfig {
  
  private String issuerUri;
  private String jwkSetUri;
  private String secretKey;
  private String algorithm = "HS256";
  private String usernameClaim = "sub";
  private String authoritiesClaim = "authorities";
  private String userDetailsService = "default";
}
