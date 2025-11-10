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

import com.epam.reportportal.infrastructure.annotations.NotBlankWithSize;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@JsonInclude(Include.NON_NULL)
public class StartLaunchRQ extends StartRQ {

  @JsonProperty("mode")
  private Mode mode;

  @JsonProperty("rerun")
  private boolean rerun;

  @JsonProperty("rerunOf")
  @Schema(description = "UUID of desired launch to rerun")
  private String rerunOf;

  @Override
  @NotBlankWithSize(min = ValidationConstraints.MIN_LAUNCH_NAME_LENGTH, max = ValidationConstraints.MAX_NAME_LENGTH)
  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return "StartLaunchRQ{" + "mode=" + mode
        + ", rerun=" + rerun
        + ", rerunOf='" + rerunOf + '\''
        + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }

    StartLaunchRQ that = (StartLaunchRQ) o;

    if (rerun != that.rerun) {
      return false;
    }
    if (mode != that.mode) {
      return false;
    }
    return rerunOf != null ? rerunOf.equals(that.rerunOf) : that.rerunOf == null;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (mode != null ? mode.hashCode() : 0);
    result = 31 * result + (rerun ? 1 : 0);
    result = 31 * result + (rerunOf != null ? rerunOf.hashCode() : 0);
    return result;
  }
}
