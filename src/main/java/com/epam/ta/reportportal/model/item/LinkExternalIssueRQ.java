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

package com.epam.ta.reportportal.model.item;

import com.epam.ta.reportportal.ws.reporting.Issue;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

/**
 * Request model for add link to external system issue
 *
 * @author Dzmitry_Kavalets
 * @author Andrei_Ramanchuk
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LinkExternalIssueRQ extends ExternalIssueRQ {

  @NotEmpty
  @Valid
  @Size(max = 300)
  @JsonProperty(value = "issues")
  @Schema(implementation = Issue.ExternalSystemIssue.class)
  private List<Issue.ExternalSystemIssue> issues;

  public void setIssues(List<Issue.ExternalSystemIssue> values) {
    this.issues = values;
  }

  public List<Issue.ExternalSystemIssue> getIssues() {
    return issues;
  }

  @Override
  public String toString() {
    return "LinkExternalIssueRQ{" + "issues=" + issues + '}';
  }
}