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

package com.epam.reportportal.base.core.item.impl.merge.strategy;

import static com.epam.reportportal.base.infrastructure.persistence.entity.enums.StatusEnum.IN_PROGRESS;
import static com.epam.reportportal.base.infrastructure.rules.commons.validation.BusinessRule.expect;
import static com.epam.reportportal.base.infrastructure.rules.exception.ErrorType.FINISH_TIME_EARLIER_THAN_START_TIME;
import static com.epam.reportportal.base.ws.converter.converters.AttributeConverter.FROM_LAUNCH_RESOURCE;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;

import com.epam.reportportal.base.core.item.identity.IdentityUtil;
import com.epam.reportportal.base.core.item.identity.TestItemUniqueIdGenerator;
import com.epam.reportportal.base.core.item.merge.LaunchMergeStrategy;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.dao.AttachmentRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.LaunchRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.LogRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.TestItemRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.TestItemRepository.TestItemIdentity;
import com.epam.reportportal.base.infrastructure.persistence.dao.TestItemRepository.TestItemParameter;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.TestItemTypeEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.Parameter;
import com.epam.reportportal.base.infrastructure.persistence.entity.launch.Launch;
import com.epam.reportportal.base.infrastructure.persistence.entity.launch.LaunchAttribute;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.MembershipDetails;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.Project;
import com.epam.reportportal.base.infrastructure.rules.commons.validation.Suppliers;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.reporting.AttributeResource;
import com.epam.reportportal.base.reporting.MergeLaunchesRQ;
import com.epam.reportportal.base.reporting.Mode;
import com.epam.reportportal.base.reporting.StartLaunchRQ;
import com.epam.reportportal.base.ws.converter.builders.LaunchBuilder;
import com.google.common.collect.Sets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Shared steps for building a merged launch and tree.
 *
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public abstract class AbstractLaunchMergeStrategy implements LaunchMergeStrategy {

  private static final Set<String> SUITE_LEVEL_TYPES = Arrays.stream(TestItemTypeEnum.values())
      .filter(type -> type.sameLevel(TestItemTypeEnum.SUITE))
      .map(Enum::name)
      .collect(Collectors.toUnmodifiableSet());

  private static final int UNIQUE_ID_UPDATE_BATCH_SIZE = 10_000;

  protected final LaunchRepository launchRepository;

  private final TestItemRepository testItemRepository;
  private final LogRepository logRepository;
  private final AttachmentRepository attachmentRepository;
  private final TestItemUniqueIdGenerator identifierGenerator;

  /**
   * Supplies merge dependencies.
   *
   * @param launchRepository     launch persistence
   * @param testItemRepository   test item tree persistence
   * @param logRepository        log line persistence
   * @param attachmentRepository attachment storage metadata
   * @param identifierGenerator  unique test item id generator for merged items
   */
  protected AbstractLaunchMergeStrategy(LaunchRepository launchRepository,
      TestItemRepository testItemRepository, LogRepository logRepository,
      AttachmentRepository attachmentRepository, TestItemUniqueIdGenerator identifierGenerator) {
    this.launchRepository = launchRepository;
    this.testItemRepository = testItemRepository;
    this.logRepository = logRepository;
    this.attachmentRepository = attachmentRepository;
    this.identifierGenerator = identifierGenerator;
  }

  /**
   * Materializes a new launch row and re-parents child items from source launches.
   *
   * @param membershipDetails project context
   * @param user              current user
   * @param rq                merge request
   * @param launchesList      sources to merge
   * @return the new launch
   */
  protected Launch createNewLaunch(MembershipDetails membershipDetails,
      ReportPortalUser user, MergeLaunchesRQ rq, List<Launch> launchesList) {
    Launch newLaunch =
        createResultedLaunch(membershipDetails.getProjectId(), user.getUserId(), rq, launchesList);
    boolean isNameChanged = !newLaunch.getName().equals(launchesList.get(0).getName());
    updateChildrenOfLaunches(newLaunch, launchesList, rq.isExtendSuitesDescription(),
        isNameChanged
    );

    // Bulk updates clear the persistence context, so return a managed instance
    return launchRepository.findById(newLaunch.getId())
        .orElseThrow(() -> new ReportPortalException(ErrorType.LAUNCH_NOT_FOUND,
            newLaunch.getId()));
  }

  /**
   * Create launch that will be the result of merge
   *
   * @param projectId       {@link Project#getId()}
   * @param userId          {@link ReportPortalUser#getUserId()}
   * @param mergeLaunchesRQ {@link MergeLaunchesRQ}
   * @param launches        {@link List} of the {@link Launch}
   * @return launch
   */
  private Launch createResultedLaunch(Long projectId, Long userId, MergeLaunchesRQ mergeLaunchesRQ,
      List<Launch> launches) {
    Instant startTime = ofNullable(mergeLaunchesRQ.getStartTime())
        .orElse(launches.stream()
            .min(Comparator.comparing(Launch::getStartTime))
            .orElseThrow(
                () -> new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, "Invalid launches"))
            .getStartTime());
    Instant endTime = ofNullable(mergeLaunchesRQ.getEndTime())
        .orElse(launches.stream()
            .max(Comparator.comparing(Launch::getEndTime))
            .orElseThrow(
                () -> new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, "Invalid launches"))
            .getEndTime());
    expect(endTime, time -> !time.isBefore(startTime)).verify(FINISH_TIME_EARLIER_THAN_START_TIME,
        endTime, startTime, projectId
    );

    StartLaunchRQ startRQ = new StartLaunchRQ();
    startRQ.setMode(ofNullable(mergeLaunchesRQ.getMode()).orElse(Mode.DEFAULT));
    startRQ.setDescription(ofNullable(mergeLaunchesRQ.getDescription()).orElse(
        launches.stream().map(Launch::getDescription).collect(joining("\n\n"))));
    startRQ.setName(ofNullable(mergeLaunchesRQ.getName()).orElse(
        "Merged: " + launches.stream().map(Launch::getName).distinct().collect(joining(", "))));
    startRQ.setStartTime(startTime);
    Launch launch =
        new LaunchBuilder().addStartRQ(startRQ).addProject(projectId).addStatus(IN_PROGRESS.name())
            .addUserId(userId).addEndTime(endTime).get();
    launch.setHasRetries(launches.stream().anyMatch(Launch::isHasRetries));
    launch.setLaunchType(launches.getFirst().getLaunchType());

    launchRepository.save(launch);
    launchRepository.refresh(launch);
    mergeAttributes(mergeLaunchesRQ.getAttributes(), launches, launch);
    return launch;
  }

  /**
   * Merges launches attributes. Collect all system attributes from existed launches and all unique
   * not system attributes from request(if preset, or from exited launches if not) to resulted
   * launch.
   *
   * @param attributesFromRq {@link Set} of attributes from request
   * @param launchesToMerge  {@link List} of {@link Launch} to be merged
   * @param resultedLaunch   {@link Launch} - result of merge
   */

  private void mergeAttributes(Set<AttributeResource> attributesFromRq,
      List<Launch> launchesToMerge, Launch resultedLaunch) {
    Set<LaunchAttribute> mergedAttributes = Sets.newHashSet();

    if (attributesFromRq == null) {
      mergedAttributes.addAll(
          launchesToMerge.stream().map(Launch::getAttributes).flatMap(Collection::stream)
              .filter(this::shouldSkipAttribute)
              .peek(it -> it.setLaunch(resultedLaunch)).collect(Collectors.toSet()));
    } else {
      mergedAttributes.addAll(
          launchesToMerge.stream().map(Launch::getAttributes).flatMap(Collection::stream)
              .filter(LaunchAttribute::isSystem)
              .filter(this::shouldSkipAttribute)
              .peek(it -> it.setLaunch(resultedLaunch))
              .collect(Collectors.toSet()));
      mergedAttributes.addAll(
          attributesFromRq.stream().map(FROM_LAUNCH_RESOURCE)
              .peek(attr -> attr.setLaunch(resultedLaunch))
              .collect(Collectors.toSet()));
    }
    resultedLaunch.setAttributes(mergedAttributes);
  }

  private boolean shouldSkipAttribute(LaunchAttribute attribute) {
    return !"rp.cluster.lastRun".equals(attribute.getKey());
  }

  /**
   * Update test-items of specified launches with new LaunchID. All updates are performed with
   * native bulk queries without loading test item entities: unique ids are regenerated if launch
   * name was changed, suites description is extended if required, then items, logs and attachments
   * are moved to the new launch.
   *
   * <p>Bulk updates clear the persistence context, so all previously loaded entities become
   * detached.
   *
   * @param newLaunch         {@link Launch}
   * @param launches          {@link List} of the {@link Launch} to be merged
   * @param extendDescription additional description for suite indicator
   * @param isNameChanged     launch name change indicator
   */
  private void updateChildrenOfLaunches(Launch newLaunch, List<Launch> launches,
      boolean extendDescription, boolean isNameChanged) {
    launches.forEach(launch -> {
      if (isNameChanged) {
        regenerateUniqueIds(newLaunch, launch.getId());
      }
      if (extendDescription) {
        // Add launch reference description for top level items
        testItemRepository.appendDescriptionByLaunchIdAndTypeIn(launch.getId(), SUITE_LEVEL_TYPES,
            Suppliers.formattedSupplier("\r\n@launch '{} #{}'", launch.getName(),
                launch.getNumber()
            ).get()
        );
      }

      testItemRepository.updateLaunchIdByLaunchId(launch.getId(), newLaunch.getId());
      logRepository.updateLaunchIdByLaunchId(launch.getId(), newLaunch.getId());
      attachmentRepository.updateLaunchIdByProjectIdAndLaunchId(newLaunch.getProjectId(),
          launch.getId(), newLaunch.getId()
      );
    });
  }

  /**
   * Regenerates auto-generated unique ids of the launch items using the new launch name. Parent
   * names and parameters are resolved from lightweight projections of the whole launch instead of
   * per-item queries.
   *
   * @param newLaunch {@link Launch} - result of merge
   * @param launchId  id of the merged launch which items are processed
   */
  private void regenerateUniqueIds(Launch newLaunch, Long launchId) {
    List<TestItemIdentity> items = testItemRepository.findItemIdentitiesByLaunchId(launchId);
    Map<Long, String> namesById = items.stream()
        .collect(Collectors.toMap(TestItemIdentity::getItemId, TestItemIdentity::getName));
    Map<Long, Set<Parameter>> parametersByItemId =
        testItemRepository.findItemParametersByLaunchId(launchId).stream()
            .collect(Collectors.groupingBy(TestItemParameter::getItemId,
                Collectors.mapping(AbstractLaunchMergeStrategy::toParameter, Collectors.toSet())
            ));

    List<Long> itemIds = new ArrayList<>();
    List<String> uniqueIds = new ArrayList<>();
    items.stream()
        .filter(item -> identifierGenerator.validate(item.getUniqueId()))
        .forEach(item -> {
          List<String> pathNames = getParentIds(item.getPath()).stream()
              .sorted()
              .map(namesById::get)
              .filter(Objects::nonNull)
              .collect(Collectors.toList());
          itemIds.add(item.getItemId());
          uniqueIds.add(identifierGenerator.generate(newLaunch.getProjectId(), newLaunch.getName(),
              pathNames, item.getName(), parametersByItemId.get(item.getItemId())
          ));
        });

    for (int from = 0; from < itemIds.size(); from += UNIQUE_ID_UPDATE_BATCH_SIZE) {
      int to = Math.min(from + UNIQUE_ID_UPDATE_BATCH_SIZE, itemIds.size());
      testItemRepository.updateUniqueIds(itemIds.subList(from, to).toArray(Long[]::new),
          uniqueIds.subList(from, to).toArray(String[]::new)
      );
    }
  }

  private static List<Long> getParentIds(String path) {
    List<Long> treeIds = IdentityUtil.getItemTreeIds(path);
    return treeIds.subList(0, treeIds.size() - 1);
  }

  private static Parameter toParameter(TestItemParameter itemParameter) {
    Parameter parameter = new Parameter();
    parameter.setKey(itemParameter.getKey());
    parameter.setValue(itemParameter.getValue());
    return parameter;
  }
}
