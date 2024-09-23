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

import com.epam.reportportal.model.ValidationConstraints;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import lombok.Data;

/**
 * Confirmation resource of user creation with user-data
 *
 * @author Andrei_Ramanchuk
 */
@Data
@JsonInclude(Include.NON_NULL)
public class CreateUserRQConfirm {

  @NotBlank
  @Pattern(regexp = "[a-zA-Z0-9-_.]+")
  @Size(min = ValidationConstraints.MIN_LOGIN_LENGTH, max = ValidationConstraints.MAX_LOGIN_LENGTH)
  @JsonProperty(value = "login", required = true)
  @Schema(requiredMode = RequiredMode.REQUIRED, example = "string")
  private String login;

  @NotBlank
  @Size(min = ValidationConstraints.MIN_PASSWORD_LENGTH, max = ValidationConstraints.MAX_PASSWORD_LENGTH)
  @JsonProperty(value = "password", required = true)
  @Schema(requiredMode = RequiredMode.REQUIRED)
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
}