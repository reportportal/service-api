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

package com.epam.reportportal.base.infrastructure.persistence.entity;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;

/**
 * Free-form key/value store on entities, persisted as jsonb.
 *
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Setter
@Getter
public class Metadata implements Serializable {

  private Map<String, Object> metadata;

  public Metadata() {
  }

  public Metadata(Map<String, Object> metadata) {
    this.metadata = metadata;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Metadata comparing = (Metadata) o;
    return Objects.equals(metadata, comparing.metadata);
  }

  @Override
  public int hashCode() {
    return Objects.hash(metadata);
  }
}
