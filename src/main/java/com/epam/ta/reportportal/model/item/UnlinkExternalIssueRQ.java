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

import com.epam.reportportal.annotations.NotBlankStringCollection;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

/**
 * @author Pavel Bortnik
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UnlinkExternalIssueRQ extends ExternalIssueRQ {

  @Valid
  @NotEmpty
  @NotBlankStringCollection
  @Size(max = 300)
  @JsonProperty(value = "ticketIds")
  private List<String> ticketIds;

  public List<String> getTicketIds() {
    return ticketIds;
  }

  public void setTicketIds(List<String> ticketIds) {
    this.ticketIds = ticketIds;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("UnlinkExternalIssueRQ{");
    sb.append("ticketIds=").append(ticketIds);
    sb.append('}');
    return sb.toString();
  }
}
