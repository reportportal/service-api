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
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author Dzmitry_Kavalets
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResetPasswordRQ {

  @NotBlank
  @Size(min = ValidationConstraints.MIN_PASSWORD_LENGTH, max = ValidationConstraints.MAX_PASSWORD_LENGTH)
  @JsonProperty(value = "password")
  @Schema(requiredMode = RequiredMode.REQUIRED)
  @Pattern(regexp = USER_PASSWORD_REGEXP)
  private String password;

  @NotBlank
  @JsonProperty(value = "uuid")
  @Schema(requiredMode = RequiredMode.REQUIRED)
  private String uuid;

}
