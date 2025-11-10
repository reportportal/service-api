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

package com.epam.reportportal.infrastructure.persistence.entity.item;

import com.epam.reportportal.infrastructure.persistence.commons.querygen.Queryable;
import java.io.Serializable;
import java.util.Objects;
import org.springframework.data.domain.Pageable;

/**
 * Entity for query
 * {@link com.epam.reportportal.infrastructure.persistence.dao.LogRepository#findNestedItems(Long, boolean, boolean,
 * Queryable, Pageable)}, consists from either
 * {@link com.epam.reportportal.infrastructure.persistence.entity.log.Log#id} or {@link TestItem#itemId} as
 * {@link NestedItem#id} and {@link NestedItem#type} to identify what kind of entity is it and either
 * {@link com.epam.reportportal.infrastructure.persistence.entity.log.Log#logLevel} or 0 in case if it is an item
 * <p>
 * Required for applying filters and sorting on the db level for different entity types
 *
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class NestedItem implements Serializable {

  private Long id;

  private String type;

  private Integer logLevel;

  public NestedItem() {
  }

  public NestedItem(Long id, String type, Integer logLevel) {
    this.id = id;
    this.type = type;
    this.logLevel = logLevel;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public Integer getLogLevel() {
    return logLevel;
  }

  public void setLogLevel(Integer logLevel) {
    this.logLevel = logLevel;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    NestedItem that = (NestedItem) o;
    return Objects.equals(id, that.id) && Objects.equals(type, that.type) && Objects.equals(
        logLevel, that.logLevel);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, type, logLevel);
  }
}
