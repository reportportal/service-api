/*
 * Copyright 2025 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.reportportal.model.project.email;

import com.epam.reportportal.infrastructure.annotations.In;
import com.epam.reportportal.infrastructure.annotations.NotBlankStringCollection;
import com.epam.reportportal.reporting.ItemAttributeResource;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Cases object for notifications sending declarations
 *
 * @author Andrei_Ramanchuk
 */
@Data
@AllArgsConstructor
public class SenderCaseDTO implements Serializable {

  /**
   * Generated SVUID
   */
  private static final long serialVersionUID = -3546546654348861010L;

  @JsonProperty("id")
  private Long id;

  @NotEmpty
  @JsonProperty("ruleName")
  private String ruleName;

  @JsonProperty(value = "recipients")
  private List<String> recipients;

  @NotBlank
  @JsonProperty(value = "sendCase")
  @In(allowedValues = {"always", "failed", "toInvestigate", "more10", "more20", "more50"})
  @Schema(allowableValues = {"always", "failed", "toInvestigate", "more10", "more20", "more50"})
  private String sendCase;

  @NotBlankStringCollection
  @JsonProperty(value = "launchNames")
  private List<String> launchNames;

  @Valid
  @JsonProperty(value = "attributes")
  private Set<ItemAttributeResource> attributes;

  @JsonProperty(value = "enabled")
  private boolean enabled;

  @JsonProperty(value = "type")
  private String type;

  @JsonProperty(value = "ruleDetails")
  private Map<String, Object> ruleDetails;

  @NotBlank
  @JsonProperty(value = "attributesOperator")
  @In(allowedValues = {"and", "or"})
  @Schema(allowableValues = "AND, OR")
  private String attributesOperator;

  public SenderCaseDTO() {
  }

}
