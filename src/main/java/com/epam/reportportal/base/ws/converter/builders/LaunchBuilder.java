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

package com.epam.reportportal.base.ws.converter.builders;

import static com.epam.reportportal.base.ws.converter.converters.AttributeConverter.FROM_LAUNCH_RESOURCE;
import static java.util.Optional.ofNullable;

import com.epam.reportportal.base.infrastructure.persistence.entity.enums.LaunchModeEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.StatusEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.launch.Launch;
import com.epam.reportportal.base.infrastructure.persistence.entity.launch.LaunchAttribute;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.reporting.AttributeResource;
import com.epam.reportportal.base.reporting.ItemAttributesRQ;
import com.epam.reportportal.base.reporting.Mode;
import com.epam.reportportal.base.reporting.StartLaunchRQ;
import com.google.common.base.Preconditions;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

public class LaunchBuilder implements Supplier<Launch> {

  private static final int LAUNCH_DESCRIPTION_LENGTH_LIMIT = 2048;
  private static final int DESCRIPTION_START_SYMBOL_INDEX = 0;

  private Launch launch;

  public LaunchBuilder() {
    this.launch = new Launch();
  }

  public LaunchBuilder(Launch launch) {
    this.launch = launch;
  }

  public LaunchBuilder addStartRQ(StartLaunchRQ request) {
    Preconditions.checkNotNull(request, ErrorType.BAD_REQUEST_ERROR);
    launch.setStartTime(request.getStartTime());
    launch.setName(request.getName().trim());
    launch.setStatus(StatusEnum.IN_PROGRESS);
    launch.setUuid(Optional.ofNullable(request.getUuid()).orElse(UUID.randomUUID().toString()));
    addDescription(request.getDescription());
    LaunchModeEnum.findByName(
            ofNullable(request.getMode()).map(Enum::name).orElse(LaunchModeEnum.DEFAULT.name()))
        .ifPresent(it -> launch.setMode(it));
    return this;
  }

  public LaunchBuilder addDescription(String description) {
    ofNullable(description).ifPresent(it -> launch.setDescription(
        StringUtils.substring(it.trim(), DESCRIPTION_START_SYMBOL_INDEX,
            LAUNCH_DESCRIPTION_LENGTH_LIMIT
        )));
    return this;
  }

  public LaunchBuilder addUserId(Long userId) {
    launch.setUserId(userId);
    return this;
  }

  public LaunchBuilder addProject(Long projectId) {
    launch.setProjectId(projectId);
    return this;
  }

  public LaunchBuilder addAttribute(
      AttributeResource attributeResource) {
    LaunchAttribute launchAttribute = FROM_LAUNCH_RESOURCE.apply(attributeResource);
    launchAttribute.setLaunch(launch);
    launch.getAttributes().add(launchAttribute);
    return this;
  }

  public LaunchBuilder addAttributes(Set<ItemAttributesRQ> attributes) {
    ofNullable(attributes).ifPresent(it -> launch.getAttributes().addAll(it.stream().map(val -> {
      LaunchAttribute launchAttribute = FROM_LAUNCH_RESOURCE.apply(val);
      launchAttribute.setLaunch(launch);
      return launchAttribute;
    }).collect(Collectors.toSet())));
    return this;
  }

  public LaunchBuilder overwriteAttributes(Set<AttributeResource> attributes) {
    if (attributes != null) {
      final Set<LaunchAttribute> overwrittenAttributes =
          launch.getAttributes().stream().filter(LaunchAttribute::isSystem)
              .collect(Collectors.toSet());

      attributes.stream().map(val -> {
        LaunchAttribute launchAttribute = FROM_LAUNCH_RESOURCE.apply(val);
        launchAttribute.setLaunch(launch);
        return launchAttribute;
      }).forEach(overwrittenAttributes::add);

      launch.setAttributes(overwrittenAttributes);
    }
    return this;
  }

  public LaunchBuilder addMode(Mode mode) {
    ofNullable(mode).ifPresent(it -> launch.setMode(LaunchModeEnum.valueOf(it.name())));
    return this;
  }

  public LaunchBuilder addStatus(String status) {
    launch.setStatus(StatusEnum.fromValue(status)
        .orElseThrow(() -> new ReportPortalException(ErrorType.INCORRECT_FINISH_STATUS)));
    return this;
  }

  public LaunchBuilder addEndTime(Instant date) {
    launch.setEndTime(date);
    return this;
  }

  @Override
  public Launch get() {
    return launch;
  }
}
