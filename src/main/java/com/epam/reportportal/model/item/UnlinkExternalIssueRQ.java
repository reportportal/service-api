/*
 * Copyright 2025 EPAM Systems
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

package com.epam.reportportal.model.item;

import com.epam.reportportal.infrastructure.annotations.NotBlankStringCollection;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Pavel Bortnik
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UnlinkExternalIssueRQ extends ExternalIssueRQ {

  @Valid
  @NotEmpty
  @NotBlankStringCollection
  @Size(max = 300)
  @JsonProperty(value = "ticketIds")
  private List<String> ticketIds;

}
