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

package com.epam.ta.reportportal.ws.converter;

import com.epam.ta.reportportal.entity.user.ApiKey;
import com.epam.ta.reportportal.ws.model.ApiKeyRQ;
import java.util.function.Function;

/**
 * @author Andrei Piankouski
 */
public class ApiKeyConverter {

  private ApiKeyConverter() {
    //static only
  }

  public static final Function<ApiKey, ApiKeyRQ> TO_RESOURCE = apiKey -> {
    ApiKeyRQ resource = new ApiKeyRQ();
    resource.setName(apiKey.getName());
    resource.setUserId(apiKey.getUserId());
    resource.setCreatedAt(apiKey.getCreatedAt().toLocalDate());
    return resource;
  };

}
