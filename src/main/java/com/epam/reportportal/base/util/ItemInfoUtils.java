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

package com.epam.reportportal.base.util;

import static com.epam.reportportal.base.infrastructure.rules.exception.ErrorType.INCORRECT_REQUEST;
import static com.epam.reportportal.base.util.Predicates.ITEM_ATTRIBUTE_EQUIVALENCE;

import com.epam.reportportal.base.infrastructure.persistence.entity.ItemAttribute;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.reporting.BulkInfoUpdateRQ;
import com.epam.reportportal.base.reporting.ItemAttributeResource;
import com.epam.reportportal.base.reporting.UpdateItemAttributeRQ;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;

/**
 * Bulk updates to launch and test item attributes and descriptions.
 *
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public class ItemInfoUtils {

  private ItemInfoUtils() {
    //static only
  }

  public static Optional<String> updateDescription(BulkInfoUpdateRQ.Description descriptionRq,
      String existDescription) {
    if (!Objects.isNull(descriptionRq) && !Objects.isNull(descriptionRq.getComment())) {
      switch (descriptionRq.getAction()) {
        case UPDATE: {
          return Optional.of(existDescription + " " + descriptionRq.getComment());
        }
        case CREATE: {
          return Optional.of(descriptionRq.getComment());
        }
        default: {
          return Optional.empty();
        }
      }
    } else {
      return Optional.empty();
    }
  }

  public static ItemAttribute findAttributeByResource(Set<ItemAttribute> attributes,
      ItemAttributeResource resource) {
    return attributes.stream()
        .filter(attr -> ITEM_ATTRIBUTE_EQUIVALENCE.test(attr, resource))
        .findAny()
        .orElseThrow(() -> new ReportPortalException(INCORRECT_REQUEST,
            "Cannot delete not common attribute"));
  }

  public static void updateAttribute(Set<ItemAttribute> attributes,
      UpdateItemAttributeRQ updateItemAttributeRQ) {
    ItemAttribute itemAttribute = attributes.stream()
        .filter(attr -> ITEM_ATTRIBUTE_EQUIVALENCE.test(attr, updateItemAttributeRQ.getFrom()))
        .findAny()
        .orElseThrow(() -> new ReportPortalException(INCORRECT_REQUEST,
            "Cannot update not common attribute"));
    attributes.remove(itemAttribute);
    itemAttribute.setKey(updateItemAttributeRQ.getTo().getKey());
    itemAttribute.setValue(updateItemAttributeRQ.getTo().getValue());
    attributes.add(itemAttribute);
  }

  public static boolean containsAttribute(Set<ItemAttribute> attributes,
      ItemAttributeResource resource) {
    return attributes.stream().noneMatch(attr -> ITEM_ATTRIBUTE_EQUIVALENCE.test(attr, resource));
  }

  public static Optional<ItemAttribute> extractAttribute(Collection<ItemAttribute> collection,
      String key) {
    return CollectionUtils.isEmpty(collection) ?
        Optional.empty() :
        collection.stream().filter(it -> key.equalsIgnoreCase(it.getKey())).findAny();
  }

  public static Optional<ItemAttributeResource> extractAttributeResource(
      Collection<ItemAttributeResource> collection, String key) {
    return CollectionUtils.isEmpty(collection) ?
        Optional.empty() :
        collection.stream().filter(it -> key.equalsIgnoreCase(it.getKey())).findAny();
  }

}
