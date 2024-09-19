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

package com.epam.ta.reportportal.model.issue;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Request for test items issue types definition (defect block)
 *
 * @author Dzianis Shlychkou
 */
@JsonInclude(Include.NON_NULL)
public class DefineIssueRQ {

  @NotNull
  @Valid
  @Size(max = 300)
  @JsonProperty(value = "issues", required = true)
  private List<IssueDefinition> issues;

  public List<IssueDefinition> getIssues() {
    return issues;
  }

  public void setIssues(List<IssueDefinition> issues) {
    this.issues = issues;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("DefineIssueRQ{");
    sb.append("issues=").append(issues);
    sb.append('}');
    return sb.toString();
  }
}