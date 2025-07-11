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

import com.epam.ta.reportportal.ws.annotations.In;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request model for user creation (confirmation will be send on email)
 *
 * @author Andrei_Ramanchuk
 */
@Data
@JsonInclude(Include.NON_NULL)
public class CreateUserRQ {

  @NotBlank
  @Email(message = "Invalid email format")
  @JsonProperty(value = "email", required = true)
  @Schema(requiredMode = RequiredMode.REQUIRED)
  private String email;

  @NotBlank
  @JsonProperty(value = "role", required = true)
  @In(allowedValues = {"editor", "viewer"})
  @Schema(requiredMode = RequiredMode.REQUIRED)
  private String role;

  @NotBlank
  @JsonProperty(value = "defaultProject", required = true)
  @Schema(requiredMode = RequiredMode.NOT_REQUIRED)
  private String defaultProject;
}
