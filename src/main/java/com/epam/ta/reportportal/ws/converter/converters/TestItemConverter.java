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
import static java.util.stream.Collectors.toSet;

import com.epam.ta.reportportal.entity.item.NestedStep;
import com.epam.ta.reportportal.entity.item.PathName;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.item.issue.IssueEntity;
import com.epam.ta.reportportal.model.NestedStepResource;
import com.epam.ta.reportportal.model.activity.TestItemActivityResource;
import com.epam.ta.reportportal.ws.reporting.ItemAttributeResource;
import com.epam.ta.reportportal.ws.reporting.ItemPathName;
import com.epam.ta.reportportal.ws.reporting.LaunchPathName;
import com.epam.ta.reportportal.ws.reporting.PathNameResource;
import com.epam.ta.reportportal.ws.reporting.TestItemResource;
import com.epam.ta.reportportal.ws.reporting.TestItemResourceOld;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

/**
 * Converts internal DB model to DTO
 *
 * @author Pavel Bortnik
 */
public final class TestItemConverter {

  private TestItemConverter() {
    //static only
  }

  public static final Function<TestItem, TestItemResource> TO_RESOURCE = item -> {
    TestItemResource resource = new TestItemResource();
    resource.setDescription(item.getDescription());
    resource.setUniqueId(item.getUniqueId());
    resource.setTestCaseId(item.getTestCaseId());
    resource.setTestCaseHash(item.getTestCaseHash());
    resource.setUuid(item.getUuid());
    resource.setAttributes(item.getAttributes()
        .stream()
        .filter(it -> !it.isSystem())
        .map(it -> new ItemAttributeResource(it.getKey(), it.getValue()))
        .collect(toSet()));
    resource.setEndTime(item.getItemResults().getEndTime());
    resource.setItemId(item.getItemId());
    if (null != item.getParameters()) {
      resource.setParameters(item.getParameters().stream().map(ParametersConverter.TO_RESOURCE)
          .collect(Collectors.toList()));
    }
    ofNullable(item.getItemResults().getIssue()).ifPresent(i -> {
      if (!Objects.isNull(i.getIssueId())) {
        resource.setIssue(IssueConverter.TO_MODEL.apply(i));
      }
    });
    resource.setName(item.getName());
    resource.setStartTime(item.getStartTime());
    resource.setStatus(
        item.getItemResults().getStatus() != null ? item.getItemResults().getStatus().toString()
            : null);
    resource.setType(item.getType() != null ? item.getType().name() : null);
    resource.setHasChildren(item.isHasChildren());
    resource.setHasStats(item.isHasStats());
    resource.setCodeRef(item.getCodeRef());

    ofNullable(item.getParentId()).ifPresent(resource::setParent);
    ofNullable(item.getLaunchId()).ifPresent(resource::setLaunchId);
    resource.setPatternTemplates(item.getPatternTemplateTestItems()
        .stream()
        .map(patternTemplateTestItem -> patternTemplateTestItem.getPatternTemplate().getName())
        .collect(toSet()));
    resource.setPath(item.getPath());
    resource.setStatisticsResource(
        StatisticsConverter.TO_RESOURCE.apply(item.getItemResults().getStatistics()));
    return resource;
  };

  public static final Function<TestItemResource, TestItemResourceOld> TO_RESOURCE_TIMESTAMP = item -> {
    TestItemResourceOld resource = new TestItemResourceOld();
    resource.setDescription(item.getDescription());
    resource.setUniqueId(item.getUniqueId());
    resource.setTestCaseId(item.getTestCaseId());
    resource.setTestCaseHash(item.getTestCaseHash());
    resource.setUuid(item.getUuid());
    resource.setAttributes(item.getAttributes());
    resource.setEndTime(item.getEndTime());
    resource.setItemId(item.getItemId());
    resource.setParameters(item.getParameters());
    resource.setIssue(item.getIssue());
    resource.setName(item.getName());
    resource.setStartTime(item.getStartTime());
    resource.setStatus(item.getStatus());
    resource.setType(item.getType());
    resource.setHasChildren(item.isHasChildren());
    resource.setHasStats(item.isHasStats());
    resource.setCodeRef(item.getCodeRef());
    resource.setParent(item.getParent());
    resource.setLaunchId(item.getLaunchId());
    resource.setPatternTemplates(item.getPatternTemplates());
    resource.setPath(item.getPath());
    resource.setStatisticsResource(item.getStatisticsResource());
    resource.setRetries(item.getRetries());
    return resource;
  };

  public static final Function<PathName, PathNameResource> PATH_NAME_TO_RESOURCE = pathName -> {

    PathNameResource pathNameResource = new PathNameResource();
    ofNullable(pathName.getLaunchPathName()).ifPresent(
        lpn -> pathNameResource.setLaunchPathName(new LaunchPathName(lpn.getName(),
            lpn.getNumber()
        )));
    ofNullable(pathName.getItemPaths()).ifPresent(itemPaths -> {
      if (CollectionUtils.isNotEmpty(itemPaths)) {
        pathNameResource.setItemPaths(itemPaths.stream()
            .map(path -> new ItemPathName(path.getId(), path.getName()))
            .collect(Collectors.toList()));
      }
    });

    return pathNameResource;

  };

  public static final Function<NestedStep, NestedStepResource> TO_NESTED_STEP_RESOURCE = item -> {
    NestedStepResource resource = new NestedStepResource();
    resource.setId(item.getId());
    resource.setName(item.getName());
    resource.setUuid(item.getUuid());
    resource.setStartTime(item.getStartTime());
    resource.setEndTime(item.getEndTime());
    resource.setStatus(item.getStatus() != null ? item.getStatus().toString() : null);
    resource.setType(item.getType() != null ? item.getType().name() : null);
    resource.setHasContent(item.isHasContent());
    resource.setAttachmentsCount(item.getAttachmentsCount());
    resource.setDuration(item.getDuration());

    return resource;
  };

  public static final BiFunction<TestItem, Long, TestItemActivityResource> TO_ACTIVITY_RESOURCE = (testItem, projectId) -> {
    TestItemActivityResource resource = new TestItemActivityResource();
    resource.setId(testItem.getItemId());
    resource.setName(testItem.getName());
    resource.setStatus(testItem.getItemResults().getStatus().name());
    resource.setProjectId(projectId);
    IssueEntity issue = testItem.getItemResults().getIssue();
    if (issue != null) {
      resource.setAutoAnalyzed(issue.getAutoAnalyzed());
      resource.setIgnoreAnalyzer(issue.getIgnoreAnalyzer());
      resource.setIssueDescription(issue.getIssueDescription());
      resource.setIssueTypeLongName(issue.getIssueType().getLongName());
      ofNullable(issue.getTickets()).ifPresent(it -> resource.setTickets(it.stream()
          .map(ticket -> ticket.getTicketId().concat(":").concat(ticket.getUrl()))
          .collect(Collectors.joining(", "))));
    }
    return resource;
  };
}
