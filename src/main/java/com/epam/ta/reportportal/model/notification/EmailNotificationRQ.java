/*
 * Copyright 2023 EPAM Systems
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

package com.epam.ta.reportportal.model.notification;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import jakarta.validation.constraints.NotNull;

/**
 * @author Andrei Piankouski
 */
public class EmailNotificationRQ {

  @NotNull
  @JsonProperty(value = "recipient")
  private String recipient;

  @NotNull
  @JsonProperty(value = "template")
  private String template;

  @JsonProperty(value = "params")
  private Map<String, Object> params;


  public String getRecipient() {
    return recipient;
  }

  public void setRecipient(String recipient) {
    this.recipient = recipient;
  }

  public String getTemplate() {
    return template;
  }

  public void setTemplate(String template) {
    this.template = template;
  }

  public Map<String, Object> getParams() {
    return params;
  }

  public void setParams(Map<String, Object> params) {
    this.params = params;
  }

  @Override
  public String toString() {
    return "EmailNotificationRQ{" +
        "recipient='" + recipient + '\'' +
        ", template='" + template + '\'' +
        ", params=" + params +
        '}';
  }
}
