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

package com.epam.reportportal.infrastructure.model.integration.auth;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class AbstractLdapResource extends AbstractAuthResource {

  @JsonProperty(value = "id")
  private Long id;

  @Valid
  @JsonProperty(value = "ldapAttributes")
  private LdapAttributes ldapAttributes;

  @Override
  public String toString() {
    return "AbstractLdapResource{" + "id=" + id + ", ldapAttributes=" + ldapAttributes + '}';
  }
}
