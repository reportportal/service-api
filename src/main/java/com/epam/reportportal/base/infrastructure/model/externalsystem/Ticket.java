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

package com.epam.reportportal.base.infrastructure.model.externalsystem;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

@JsonInclude(Include.NON_NULL)
public class Ticket {

  @JsonProperty(value = "id")
  private String id;

  @JsonProperty(value = "summary")
  private String summary;

  @JsonProperty(value = "status")
  private String status;

  @JsonProperty(value = "url")
  private String ticketUrl;

  @JsonProperty(value = "pluginName")
  private String pluginName;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getSummary() {
    return summary;
  }

  public void setSummary(String summary) {
    this.summary = summary;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getTicketUrl() {
    return ticketUrl;
  }

  public void setTicketUrl(String ticketUrl) {
    this.ticketUrl = ticketUrl;
  }

  public String getPluginName() {
    return pluginName;
  }

  public void setPluginName(String pluginName) {
    this.pluginName = pluginName;
  }

  @Override
  public String toString() {
    return "Ticket{" + "id='" + id + '\'' + ", summary='" + summary + '\'' + ", status='" + status
        + '\'' + ", ticketUrl='" + ticketUrl
        + '\'' + ", pluginName='" + pluginName + '\'' + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Ticket ticket = (Ticket) o;
    return Objects.equals(id, ticket.id) && Objects.equals(summary, ticket.summary)
        && Objects.equals(status, ticket.status)
        && ticketUrl.equals(ticket.ticketUrl) && Objects.equals(pluginName, ticket.pluginName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, summary, status, ticketUrl, pluginName);
  }
}
