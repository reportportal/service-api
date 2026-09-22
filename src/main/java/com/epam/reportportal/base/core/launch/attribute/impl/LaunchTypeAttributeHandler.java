/*
 * Copyright 2026 EPAM Systems
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

package com.epam.reportportal.base.core.launch.attribute.impl;


import com.epam.reportportal.base.core.launch.attribute.AttributeHandler;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.entity.ItemAttribute;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.LaunchTypeEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.launch.Launch;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * Sets the {@link Launch#getLaunchType()} at launch start based on a launch-type system attribute such as
 * {@code isAgentic} or {@code isPipeline}.
 *
 * <p>A {@code true} value applies the mapped type; {@code false} falls back to
 * {@link LaunchTypeEnum#AUTOMATION}. At most one such attribute is allowed. Launch type is immutable on update.
 *
 * <p>To support a new launch type, add an entry to {@link #TYPE_BY_ATTRIBUTE_KEY}.
 */
@Component
public class LaunchTypeAttributeHandler implements AttributeHandler {

  private static final Map<String, LaunchTypeEnum> TYPE_BY_ATTRIBUTE_KEY = Map.of(
      "isAgentic", LaunchTypeEnum.AGENTIC,
      "isPipeline", LaunchTypeEnum.PIPELINE);

  @Override
  public void handleLaunchStart(Launch launch) {
    if (launch == null || CollectionUtils.isEmpty(launch.getAttributes())) {
      return;
    }
    resolveLaunchType(launch.getAttributes()).ifPresent(launch::setLaunchType);
  }

  @Override
  public void handleLaunchUpdate(Launch launch, ReportPortalUser user) {
    // launch_type is immutable after creation; launch-type attributes on update are ignored.
  }

  private Optional<LaunchTypeEnum> resolveLaunchType(Collection<ItemAttribute> attributes) {
    List<ItemAttribute> typeAttributes = attributes.stream()
        .filter(this::isLaunchTypeAttribute)
        .toList();

    if (typeAttributes.size() > 1) {
      throw new IllegalArgumentException(
          "Launch cannot have more than one launch-type system attribute %s. Specify only one."
              .formatted(typeAttributes.stream().map(ItemAttribute::getKey).toList()));
    }

    return typeAttributes.stream()
        .findFirst()
        .map(this::toLaunchType);
  }

  private boolean isLaunchTypeAttribute(ItemAttribute attribute) {
    return Boolean.TRUE.equals(attribute.isSystem()) && findType(attribute).isPresent();
  }

  private LaunchTypeEnum toLaunchType(ItemAttribute attribute) {
    return Boolean.parseBoolean(attribute.getValue())
        ? findType(attribute).orElseThrow()
        : LaunchTypeEnum.AUTOMATION;
  }

  private Optional<LaunchTypeEnum> findType(ItemAttribute attribute) {
    return TYPE_BY_ATTRIBUTE_KEY.entrySet().stream()
        .filter(entry -> entry.getKey().equalsIgnoreCase(attribute.getKey()))
        .map(Map.Entry::getValue)
        .findFirst();
  }
}
