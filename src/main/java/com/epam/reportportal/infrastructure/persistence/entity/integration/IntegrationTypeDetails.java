/*
 * Copyright 2019 EPAM Systems
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

package com.epam.reportportal.infrastructure.persistence.entity.integration;

import com.epam.reportportal.infrastructure.persistence.commons.JsonbUserType;
import java.io.Serializable;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Yauheni_Martynau
 */
@Setter
@Getter
public class IntegrationTypeDetails extends JsonbUserType<IntegrationTypeDetails> implements Serializable {

  private Map<String, Object> details;

  @Override
  public Class<IntegrationTypeDetails> returnedClass() {
    return IntegrationTypeDetails.class;
  }

}
