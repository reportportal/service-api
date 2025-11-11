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

package com.epam.reportportal.reporting;

import com.epam.reportportal.reporting.databind.MultiFormatDateDeserializer;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Test item issue
 *
 * @author Dzianis Shlychkou
 */
@JsonInclude(Include.NON_NULL)
@Data
@NoArgsConstructor
public class Issue {

  @NotBlank
  @JsonProperty(value = "issueType", required = true)
  @JsonAlias({"issueType", "issue_type"})
  private String issueType;

  @JsonProperty(value = "comment")
  @Size(max = ValidationConstraints.MAX_DESCRIPTION_LENGTH)
  private String comment;

  @JsonProperty(value = "autoAnalyzed")
  private boolean autoAnalyzed;

  @JsonProperty(value = "ignoreAnalyzer")
  private boolean ignoreAnalyzer;

  @Valid
  @Size(max = 300)
  @JsonProperty(value = "externalSystemIssues")
  private Set<ExternalSystemIssue> externalSystemIssues;

  @JsonInclude(Include.NON_NULL)
  @Getter
  @Setter
  @EqualsAndHashCode
  @ToString
  public static class ExternalSystemIssue {

    @NotBlank
    @JsonProperty(value = "ticketId")
    private String ticketId;

    @JsonProperty(value = "submitDate")
    @JsonDeserialize(using = MultiFormatDateDeserializer.class)
    private Instant submitDate;

    @NotBlank
    @JsonProperty(value = "btsUrl")
    private String btsUrl;

    @NotBlank
    @JsonProperty(value = "btsProject")
    private String btsProject;

    @NotBlank
    @JsonProperty(value = "url")
    private String url;

    @JsonProperty(value = "pluginName")
    private String pluginName;

  }

}
