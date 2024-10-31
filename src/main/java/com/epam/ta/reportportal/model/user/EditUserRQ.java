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

import com.epam.reportportal.annotations.In;
import com.epam.reportportal.annotations.NotBlankString;
import com.epam.reportportal.model.ValidationConstraints;
import com.epam.ta.reportportal.entity.user.UserType;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import lombok.Data;

/**
 * Edit User request model
 *
 * @author Aliaksandr_Kazantsau
 * @author Andrei_Ramanchuk
 */
@Data
@JsonInclude(Include.NON_NULL)
public class EditUserRQ {

  @NotBlankString
  @JsonProperty(value = "email")
  private String email;

  @JsonProperty(value = "externalId")
  private String externalId;

  @JsonProperty(value = "active")
  private Boolean active;

  @In(allowedValues = {"user", "administrator"})
  @JsonProperty(value = "role")
  private String role;

  @In(allowedValues = {"INTERNAL", "SCIM"})
  @JsonProperty(value = "accountType")
  @Schema(requiredMode = RequiredMode.NOT_REQUIRED, allowableValues = "INTERNAL, SCIM")
  private String accountType;

  @NotBlankString
  @Size(min = ValidationConstraints.MIN_USER_NAME_LENGTH, max = ValidationConstraints.MAX_USER_NAME_LENGTH)
  @Pattern(regexp = "(\\s*[\\pL0-9-_\\.]+\\s*)+")
  @JsonProperty(value = "fullName")
  @Schema(requiredMode = RequiredMode.REQUIRED, example = "string")
  private String fullName;

}