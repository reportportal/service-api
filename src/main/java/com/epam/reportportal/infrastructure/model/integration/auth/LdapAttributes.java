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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LdapAttributes implements Serializable {

  @NotNull
  @JsonProperty(value = "enabled")
  private Boolean enabled;

  @NotBlank
  @Pattern(regexp = "^ldaps?://.*")
  @JsonProperty(value = "url")
  private String url;

  @NotBlank
  @JsonProperty(value = "baseDn")
  private String baseDn;

  @NotNull
  @Valid
  @JsonProperty("synchronizationAttributes")
  private SynchronizationAttributesResource synchronizationAttributes;

  @Override
  public String toString() {
    return "LdapAttributes{" + "enabled=" + enabled + ", url='" + url + '\'' + ", baseDn='" + baseDn
        + '\''
        + ", synchronizationAttributes=" + synchronizationAttributes + '}';
  }
}
