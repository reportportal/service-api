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

package com.epam.ta.reportportal.model.log;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * JSON Representation of Report Portal's Log domain object
 *
 * @author Andrei Varabyeu
 */
@JsonInclude(Include.NON_NULL)
@Getter
@Setter
@ToString
public class LogResource {

  @JsonProperty(value = "id", required = true)
  private Long id;

  @JsonProperty(value = "uuid", required = true)
  private String uuid;

  @JsonProperty(value = "time")
  private LocalDateTime logTime;

  @JsonProperty(value = "message")
  private String message;

  @JsonProperty(value = "binaryContent")
  private BinaryContent binaryContent;

  @JsonProperty(value = "thumbnail")
  private String thumbnail;

  @JsonProperty(value = "level")
  @ApiModelProperty(allowableValues = "error, warn, info, debug, trace, fatal, unknown")
  private String level;

  @JsonProperty(value = "itemId")
  private Long itemId;

  @JsonProperty(value = "launchId")
  private Long launchId;

  @JsonInclude(Include.NON_NULL)
  @Getter
  @Setter
  @ToString
  public static class BinaryContent {

    @NotNull
    @JsonProperty(value = "id", required = true)
    private String binaryDataId;

    @JsonProperty(value = "thumbnailId", required = true)
    private String thumbnailId;

    @JsonProperty(value = "contentType", required = true)
    private String contentType;
  }

}
