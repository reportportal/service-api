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

package com.epam.ta.reportportal.model.project.email;

import com.epam.reportportal.annotations.In;
import com.epam.reportportal.annotations.NotBlankStringCollection;
import com.epam.ta.reportportal.ws.reporting.ItemAttributeResource;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

/**
 * Cases object for notifications sending declarations
 *
 * @author Andrei_Ramanchuk
 */
@JsonInclude(Include.NON_NULL)
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

  @NotEmpty
  @NotBlankStringCollection
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

  public SenderCaseDTO(Long id, String ruleName, List<String> recipients, String sendCase,
      List<String> launchNames, Set<ItemAttributeResource> attributes, boolean enabled, String type,
      Map<String, Object> ruleDetails, String attributesOperator) {
    this.id = id;
    this.ruleName = ruleName;
    this.recipients = recipients;
    this.sendCase = sendCase;
    this.launchNames = launchNames;
    this.attributes = attributes;
    this.enabled = enabled;
    this.type = type;
    this.ruleDetails = ruleDetails;
    this.attributesOperator = attributesOperator;
  }

  /* Getters and setters block */
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getRuleName() {
    return ruleName;
  }

  public void setRuleName(String ruleName) {
    this.ruleName = ruleName;
  }

  public void setRecipients(List<String> recipients) {
    this.recipients = recipients;
  }

  public List<String> getRecipients() {
    return recipients;
  }

  public void setSendCase(String value) {
    this.sendCase = value;
  }

  public String getSendCase() {
    return sendCase;
  }

  public void setLaunchNames(List<String> value) {
    this.launchNames = value;
  }

  public List<String> getLaunchNames() {
    return launchNames;
  }

  public Set<ItemAttributeResource> getAttributes() {
    return attributes;
  }

  public void setAttributes(Set<ItemAttributeResource> attributes) {
    this.attributes = attributes;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public String getAttributesOperator() {
    return attributesOperator;
  }

  public void setAttributesOperator(String attributesOperator) {
    this.attributesOperator = attributesOperator;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public Map<String, Object> getRuleDetails() {
    return ruleDetails;
  }

  public void setRuleDetails(Map<String, Object> ruleDetails) {
    this.ruleDetails = ruleDetails;
  }

  /* Auto generated methods */
  @Override
  public String toString() {
    return "SenderCaseDTO{" +
        "id=" + id +
        ", ruleName='" + ruleName + '\'' +
        ", recipients=" + recipients +
        ", sendCase='" + sendCase + '\'' +
        ", launchNames=" + launchNames +
        ", attributes=" + attributes +
        ", enabled=" + enabled +
        ", type='" + type + '\'' +
        ", ruleDetails='" + ruleDetails + '\'' +
        ", attributesOperator='" + attributesOperator + '\'' +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SenderCaseDTO that = (SenderCaseDTO) o;
    return enabled == that.enabled && Objects.equals(id, that.id)
        && Objects.equals(ruleName, that.ruleName) && Objects.equals(recipients,
        that.recipients) && Objects.equals(sendCase, that.sendCase)
        && Objects.equals(launchNames, that.launchNames) && Objects.equals(
        attributes, that.attributes) && Objects.equals(type, that.type)
        && Objects.equals(ruleDetails, that.ruleDetails) && Objects.equals(
        attributesOperator, that.attributesOperator);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, ruleName, recipients, sendCase, launchNames, attributes, enabled, type,
        ruleDetails, attributesOperator);
  }
}
