/*
 * Copyright 2019 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.reportportal.base.infrastructure.model.settings;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * Settings for OAuth provider registration.
 *
 * @author Anton Machulski
 */
@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode
public class OAuthRegistrationResource implements Serializable {

  public static final String URL_PATTERN = "^(http://|https://)?(www\\.)?([a-zA-Z0-9-]+)(\\.[a-zA-Z0-9-]+)*(:[0-9]+)?(/[a-z_-]+)*$";

  @JsonProperty(value = "id")
  private String id;

  @NotBlank
  @JsonProperty(value = "clientId")
  private String clientId;

  @NotBlank
  @JsonProperty(value = "clientSecret")
  private String clientSecret;

  @JsonProperty(value = "clientAuthMethod")
  private String clientAuthMethod;

  @JsonProperty(value = "authGrantType")
  private String authGrantType;

  @JsonProperty(value = "redirectUrlTemplate")
  private String redirectUrlTemplate;

  @Pattern(regexp = URL_PATTERN)
  @JsonProperty(value = "authorizationUri")
  @Schema(type = "string", pattern = URL_PATTERN, example = "string")
  private String authorizationUri;

  @Pattern(regexp = URL_PATTERN)
  @JsonProperty(value = "tokenUri")
  @Schema(type = "string", pattern = URL_PATTERN, example = "string")
  private String tokenUri;

  @JsonProperty(value = "userInfoEndpointUri")
  private String userInfoEndpointUri;

  @JsonProperty(value = "userInfoEndpointNameAttribute")
  private String userInfoEndpointNameAttribute;

  @JsonProperty(value = "jwkSetUri")
  private String jwkSetUri;

  @JsonProperty(value = "clientName")
  private String clientName;

  @JsonProperty(value = "scopes")
  private Set<String> scopes;

  @JsonProperty(value = "restrictions")
  private Map<String, String> restrictions;

}
