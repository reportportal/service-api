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

package com.epam.ta.reportportal.model;

import static com.epam.ta.reportportal.ws.resolver.PagingHandlerMethodArgumentResolver.CUT_DEFAULT_OFFSET;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Data;

/**
 * Request for retrieving test items by list of IDs.
 *
 * @author Pavel Bortnik
 */
@Data
public class BulkItemsRQ {

  @NotEmpty
  @Size(max = CUT_DEFAULT_OFFSET, message = "Maximum 300 IDs are allowed")
  @JsonProperty(value = "ids", required = true)
  private List<Long> ids;

}

