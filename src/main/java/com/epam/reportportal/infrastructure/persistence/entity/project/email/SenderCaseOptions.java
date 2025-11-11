/*
 * Copyright 2023 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.reportportal.infrastructure.persistence.entity.project.email;

import com.epam.reportportal.infrastructure.persistence.commons.JsonbUserType;
import java.io.Serializable;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @author <a href="mailto:andrei_piankouski@epam.com">Andrei Piankouski</a>
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class SenderCaseOptions extends JsonbUserType<SenderCaseOptions> implements Serializable {

  private Map<String, Object> options;

  @Override
  public Class<SenderCaseOptions> returnedClass() {
    return SenderCaseOptions.class;
  }
}
