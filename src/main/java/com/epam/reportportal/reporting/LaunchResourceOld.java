/*
 * Copyright 2024 EPAM Systems
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

import static com.fasterxml.jackson.annotation.JsonFormat.Feature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS;

import com.epam.reportportal.reporting.databind.MultiFormatDateDeserializer;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

/**
 * Serializes dates as long timestamp
 *
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Getter
@Setter
@JsonInclude(Include.NON_NULL)
public class LaunchResourceOld extends LaunchResource {

  @JsonProperty(value = "startTime", required = true)
  @JsonDeserialize(using = MultiFormatDateDeserializer.class)
  @JsonFormat(shape = Shape.NUMBER, without = WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, timezone = "UTC")
  private Instant startTime;

  @JsonProperty(value = "endTime")
  @JsonDeserialize(using = MultiFormatDateDeserializer.class)
  @JsonFormat(shape = Shape.NUMBER, without = WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, timezone = "UTC")
  private Instant endTime;

  @JsonProperty(value = "lastModified")
  @JsonDeserialize(using = MultiFormatDateDeserializer.class)
  @JsonFormat(shape = Shape.NUMBER, without = WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, timezone = "UTC")
  private Instant lastModified;
}

