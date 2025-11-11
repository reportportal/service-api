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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Common response for operation that return just message about completion
 *
 * @author Henadzi_Vrubleuski
 */
@Setter
@Getter
@JsonInclude(Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
public class OperationCompletionRS {

  @JsonProperty("message")
  private String resultMessage;

  @Override
  public String toString() {
    return "OperationCompletionRS{" + "resultMessage='" + resultMessage + '\''
        + '}';
  }
}
