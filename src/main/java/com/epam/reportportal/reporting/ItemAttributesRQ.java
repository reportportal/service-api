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

package com.epam.reportportal.reporting;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Setter
@Getter
@NoArgsConstructor
public class ItemAttributesRQ extends ItemAttributeResource implements Serializable {

  @Schema(example = "false")
  private boolean system;

  public ItemAttributesRQ(String value) {
    super(null, value);
  }

  public ItemAttributesRQ(String key, String value) {
    super(key, value);
  }

  public ItemAttributesRQ(String key, String value, boolean system) {
    super(key, value);
    this.system = system;
  }

  @Override
  public String toString() {
    return "ItemAttributesRQ{" + "system=" + system + "} " + super.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }

    ItemAttributesRQ that = (ItemAttributesRQ) o;

    return system == that.system;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (system ? 1 : 0);
    return result;
  }
}
