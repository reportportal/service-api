/*
 * Copyright 2024 EPAM Systems
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

package com.epam.reportportal.auth.model;

import com.epam.reportportal.base.infrastructure.model.integration.auth.AbstractAuthResource;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */

@Setter
@Getter
@Schema
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SamlResource extends AbstractAuthResource {

  private Long id;

  /**
   * Provider name associated with IDP
   */
  @NotEmpty
  private String identityProviderName;
  /**
   * Alias associated with IDP
   */
  private String identityProviderAlias;
  /**
   * IDP metadata URL
   */
  @NotEmpty
  private String identityProviderMetadataUrl;
  /**
   * Attribute Name Format Id associated with IDP for user identification
   */
  private String identityProviderNameId;
  /**
   * IDP URL
   */
  private String identityProviderUrl;
  /**
   * Attribute name associated with full name of user in SAML response
   */
  private String fullNameAttribute;
  /**
   * Attribute name associated with first name of user in SAML response
   */
  private String firstNameAttribute;
  /**
   * Attribute name associated with last name of user in SAML response
   */
  private String lastNameAttribute;
  /**
   * Attribute name associated with email of user in SAML response
   */
  @NotEmpty
  private String emailAttribute;
  /**
   * Attribute name associated with roles of user in SAML response
   */
  private String rolesAttribute;
  /**
   * Indicates IDP availability for authentication
   */
  private boolean enabled;

}
