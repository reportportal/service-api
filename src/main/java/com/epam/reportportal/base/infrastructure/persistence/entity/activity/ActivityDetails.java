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

package com.epam.reportportal.base.infrastructure.persistence.entity.activity;

import com.epam.reportportal.base.infrastructure.persistence.commons.JsonbUserType;
import com.google.common.collect.Lists;
import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Structured list of field-level diffs for an {@link Activity} log row.
 *
 * @author Ihar Kahadouski
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ActivityDetails extends JsonbUserType<ActivityDetails> implements Serializable {

  private List<HistoryField> history = Lists.newArrayList();

  @Override
  public Class<ActivityDetails> returnedClass() {
    return ActivityDetails.class;
  }

  public void addHistoryField(HistoryField historyField) {
    history.add(historyField);
  }

  @Override
  public String toString() {
    return "ActivityDetails{" + "history=" + history + '}';
  }

}
