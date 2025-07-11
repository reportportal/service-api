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

package com.epam.ta.reportportal.ws.converter.converters;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;

import com.epam.ta.reportportal.core.analyzer.auto.impl.AnalyzerStatusCache;
import com.epam.ta.reportportal.dao.UserRepository;
import com.epam.ta.reportportal.entity.ItemAttribute;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.model.activity.LaunchActivityResource;
import com.epam.ta.reportportal.ws.converter.resource.handler.attribute.ItemAttributeType;
import com.epam.ta.reportportal.ws.converter.resource.handler.attribute.ResourceAttributeHandler;
import com.epam.ta.reportportal.ws.converter.resource.handler.attribute.resolver.ItemAttributeTypeResolver;
import com.epam.ta.reportportal.ws.reporting.LaunchResource;
import com.epam.ta.reportportal.ws.reporting.LaunchResourceOld;
import com.epam.ta.reportportal.ws.reporting.Mode;
import com.epam.ta.reportportal.ws.reporting.RetentionPolicy;
import com.google.common.base.Preconditions;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Pavel Bortnik
 */
@Service
public class LaunchConverter {

  @Autowired
  private AnalyzerStatusCache analyzerStatusCache;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private ItemAttributeTypeResolver itemAttributeTypeResolver;

  @Autowired
  private Map<ItemAttributeType, ResourceAttributeHandler<LaunchResource>>
      resourceAttributeUpdaterMapping;

  private static final String DELETED_USER = "deleted_user";

  public static final Function<Launch, LaunchActivityResource> TO_ACTIVITY_RESOURCE = launch -> {
    LaunchActivityResource resource = new LaunchActivityResource();
    resource.setId(launch.getId());
    resource.setProjectId(launch.getProjectId());
    resource.setName(launch.getName() + " #" + launch.getNumber());
    return resource;
  };

  public Function<Launch, LaunchResource> TO_RESOURCE = db -> {

    Preconditions.checkNotNull(db);

    LaunchResource resource = new LaunchResource();
    resource.setLaunchId(db.getId());
    resource.setUuid(db.getUuid());
    resource.setName(db.getName());
    resource.setNumber(db.getNumber());
    resource.setDescription(db.getDescription());
    resource.setStatus(db.getStatus() == null ? null : db.getStatus().toString());
    resource.setStartTime(db.getStartTime() == null ? null : db.getStartTime());
    resource.setEndTime(db.getEndTime() == null ? null : db.getEndTime());
    ofNullable(db.getLastModified()).ifPresent(resource::setLastModified);
    ofNullable(db.getAttributes()).ifPresent(attributes -> updateAttributes(resource, attributes));
    ofNullable(resource.getAttributes()).ifPresentOrElse(a -> {
    }, () -> resource.setAttributes(Collections.emptySet()));
    resource.setMode(db.getMode() == null ? null : Mode.valueOf(db.getMode().name()));
    resource.setRetentionPolicy(db.getRetentionPolicy() == null ? null :
        RetentionPolicy.valueOf(db.getRetentionPolicy().name()));
    resource.setAnalyzers(analyzerStatusCache.getStartedAnalyzers(db.getId()));
    resource.setStatisticsResource(StatisticsConverter.TO_RESOURCE.apply(db.getStatistics()));
    resource.setApproximateDuration(db.getApproximateDuration());
    resource.setHasRetries(db.isHasRetries());
    //TODO replace with single select on higher level to prevent selection for each launch
    ofNullable(db.getUserId()).flatMap(id -> userRepository.findLoginById(id))
        .ifPresentOrElse(resource::setOwner, () -> resource.setOwner(DELETED_USER));
    resource.setRerun(db.isRerun());
    return resource;
  };

  public Function<LaunchResource, LaunchResourceOld> TO_RESOURCE_OLD = resource -> {
    LaunchResourceOld old = new LaunchResourceOld();
    old.setLaunchId(resource.getLaunchId());
    old.setUuid(resource.getUuid());
    old.setName(resource.getName());
    old.setNumber(resource.getNumber());
    old.setDescription(resource.getDescription());
    old.setStatus(resource.getStatus());
    old.setStartTime(resource.getStartTime());
    old.setEndTime(resource.getEndTime());
    old.setLastModified(resource.getLastModified());
    old.setAttributes(resource.getAttributes());
    old.setMode(resource.getMode());
    old.setRetentionPolicy(resource.getRetentionPolicy());
    old.setAnalyzers(resource.getAnalyzers());
    old.setStatisticsResource(resource.getStatisticsResource());
    old.setApproximateDuration(resource.getApproximateDuration());
    old.setHasRetries(resource.isHasRetries());
    old.setOwner(resource.getOwner());
    old.setRerun(resource.isRerun());
    return old;
  };


  private void updateAttributes(LaunchResource resource, Set<ItemAttribute> attributes) {
    final Map<ItemAttributeType, Set<ItemAttribute>> attributeMapping =
        attributes.stream().collect(groupingBy(
            attr -> itemAttributeTypeResolver.resolve(attr).orElse(ItemAttributeType.UNRESOLVED),
            toSet()
        ));
    attributeMapping.forEach(
        (type, attr) -> resourceAttributeUpdaterMapping.get(type).handle(resource, attr));
  }
}
