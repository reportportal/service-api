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

package com.epam.ta.reportportal.model.user;

import static com.epam.reportportal.model.ValidationConstraints.USER_PASSWORD_REGEXP;

import com.epam.reportportal.model.ValidationConstraints;
import com.epam.ta.reportportal.ws.annotations.In;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Create User request for admin user creation functionality
 *
 * @author Andrei_Ramanchuk
 */
@Data
@JsonInclude(Include.NON_NULL)
public class CreateUserRQFull {

  @JsonProperty(value = "active", defaultValue = "true")
  @Schema(requiredMode = RequiredMode.NOT_REQUIRED, defaultValue = "true")
  private boolean active = true;

  @JsonProperty(value = "externalId")
  @Schema(requiredMode = RequiredMode.NOT_REQUIRED)
  private String externalId;

  @In(allowedValues = {"INTERNAL", "SCIM"})
  @JsonProperty(value = "accountType")
  @Schema(requiredMode = RequiredMode.NOT_REQUIRED, allowableValues = "INTERNAL, SCIM")
  private String accountType;

  @NotBlank
  @Pattern(regexp = "[a-zA-Z0-9-_.]+")
  @Size(min = ValidationConstraints.MIN_LOGIN_LENGTH, max = ValidationConstraints.MAX_LOGIN_LENGTH)
  @JsonProperty(value = "login", required = true)
  @Schema(requiredMode = RequiredMode.REQUIRED, example = "string")
  private String login;

  @Size(max = ValidationConstraints.MAX_PASSWORD_LENGTH)
  @JsonProperty(value = "password")
  @Schema(requiredMode = RequiredMode.NOT_REQUIRED)
  @Pattern(regexp = USER_PASSWORD_REGEXP)
  private String password;

  @NotBlank
  @Pattern(regexp = "[\\pL0-9-_ \\.]+")
  @Size(min = ValidationConstraints.MIN_USER_NAME_LENGTH, max = ValidationConstraints.MAX_USER_NAME_LENGTH)
  @JsonProperty(value = "fullName", required = true)
  @Schema(requiredMode = RequiredMode.REQUIRED, example = "string")
  private String fullName;

  @NotBlank
  @JsonProperty(value = "email", required = true)
  @Schema(requiredMode = RequiredMode.REQUIRED)
  private String email;

  @NotNull
  @JsonProperty(value = "accountRole", required = true)
  @In(allowedValues = {"user", "administrator"})
  @Schema(requiredMode = RequiredMode.NOT_REQUIRED, allowableValues = "USER, ADMINISTRATOR")
  private String accountRole;

  @JsonProperty(value = "projectRole")
  @In(allowedValues = {"editor", "viewer"})
  @Schema(requiredMode = RequiredMode.NOT_REQUIRED, allowableValues = "EDITOR, VIEWER")
  private String projectRole;

  @JsonProperty(value = "defaultProject")
  @Schema(requiredMode = RequiredMode.NOT_REQUIRED)
  private String defaultProject;
}
