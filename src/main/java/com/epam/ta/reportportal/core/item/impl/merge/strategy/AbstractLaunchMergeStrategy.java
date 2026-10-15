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

package com.epam.ta.reportportal.core.item.impl.merge.strategy;

import static com.epam.reportportal.rules.commons.validation.BusinessRule.expect;
import static com.epam.reportportal.rules.exception.ErrorType.FINISH_TIME_EARLIER_THAN_START_TIME;
import static com.epam.ta.reportportal.entity.enums.StatusEnum.IN_PROGRESS;
import static com.epam.ta.reportportal.ws.converter.converters.ItemAttributeConverter.FROM_RESOURCE;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;

import com.epam.reportportal.rules.commons.validation.Suppliers;
import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.item.identity.IdentityUtil;
import com.epam.ta.reportportal.core.item.identity.TestItemUniqueIdGenerator;
import com.epam.ta.reportportal.core.item.merge.LaunchMergeStrategy;
import com.epam.ta.reportportal.dao.AttachmentRepository;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.LogRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.ItemAttribute;
import com.epam.ta.reportportal.entity.enums.TestItemTypeEnum;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.ws.converter.builders.LaunchBuilder;
import com.epam.ta.reportportal.ws.reporting.ItemAttributeResource;
import com.epam.ta.reportportal.ws.reporting.MergeLaunchesRQ;
import com.epam.ta.reportportal.ws.reporting.Mode;
import com.epam.ta.reportportal.ws.reporting.StartLaunchRQ;
import com.google.common.collect.Sets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public abstract class AbstractLaunchMergeStrategy implements LaunchMergeStrategy {

  private static final Set<TestItemTypeEnum> SUITE_LEVEL_TYPES = Arrays.stream(
          TestItemTypeEnum.values())
      .filter(type -> type.sameLevel(TestItemTypeEnum.SUITE))
      .collect(Collectors.toUnmodifiableSet());

  protected final LaunchRepository launchRepository;

  private final TestItemRepository testItemRepository;
  private final LogRepository logRepository;
  private final AttachmentRepository attachmentRepository;
  private final TestItemUniqueIdGenerator identifierGenerator;

  protected AbstractLaunchMergeStrategy(LaunchRepository launchRepository,
      TestItemRepository testItemRepository, LogRepository logRepository,
      AttachmentRepository attachmentRepository, TestItemUniqueIdGenerator identifierGenerator) {
    this.launchRepository = launchRepository;
    this.testItemRepository = testItemRepository;
    this.logRepository = logRepository;
    this.attachmentRepository = attachmentRepository;
    this.identifierGenerator = identifierGenerator;
  }

  protected Launch createNewLaunch(ReportPortalUser.ProjectDetails projectDetails,
      ReportPortalUser user, MergeLaunchesRQ rq, List<Launch> launchesList) {
    Launch newLaunch =
        createResultedLaunch(projectDetails.getProjectId(), user.getUserId(), rq, launchesList);
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

  private void mergeAttributes(Set<ItemAttributeResource> attributesFromRq,
      List<Launch> launchesToMerge, Launch resultedLaunch) {
    Set<ItemAttribute> mergedAttributes = Sets.newHashSet();

    if (attributesFromRq == null) {
      mergedAttributes.addAll(
          launchesToMerge.stream().map(Launch::getAttributes).flatMap(Collection::stream)
              .filter(this::shouldSkipAttribute)
              .peek(it -> it.setLaunch(resultedLaunch)).collect(Collectors.toSet()));
    } else {
      mergedAttributes.addAll(
          launchesToMerge.stream().map(Launch::getAttributes).flatMap(Collection::stream)
              .filter(ItemAttribute::isSystem)
              .filter(this::shouldSkipAttribute)
              .peek(it -> it.setLaunch(resultedLaunch))
              .collect(Collectors.toSet()));
      mergedAttributes.addAll(
          attributesFromRq.stream().map(FROM_RESOURCE).peek(attr -> attr.setLaunch(resultedLaunch))
              .collect(Collectors.toSet()));
    }
    resultedLaunch.setAttributes(mergedAttributes);
  }

  private boolean shouldSkipAttribute(ItemAttribute attribute) {
    return !"rp.cluster.lastRun".equals(attribute.getKey());
  }

  /**
   * Update test-items of specified launches with new LaunchID. Items are moved with a single bulk
   * update per launch, only items that require additional changes are loaded and saved: unique id
   * regeneration if launch name was changed and suite description extension.
   *
   * <p>Bulk update clears the persistence context, so all previously loaded entities become
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
      List<TestItem> changedItems =
          updateItemsBeforeMove(newLaunch, launch, extendDescription, isNameChanged);
      testItemRepository.saveAll(changedItems);

      testItemRepository.updateLaunchIdByLaunchId(launch.getId(), newLaunch.getId());
      logRepository.updateLaunchIdByLaunchId(launch.getId(), newLaunch.getId());
      attachmentRepository.updateLaunchIdByProjectIdAndLaunchId(newLaunch.getProjectId(),
          launch.getId(), newLaunch.getId()
      );
    });
  }

  private List<TestItem> updateItemsBeforeMove(Launch newLaunch, Launch launch,
      boolean extendDescription, boolean isNameChanged) {
    List<TestItem> testItems;
    if (isNameChanged) {
      testItems = testItemRepository.findTestItemsByLaunchId(launch.getId());
    } else if (extendDescription) {
      testItems = testItemRepository.findTestItemsByLaunchIdAndTypeIn(launch.getId(),
          SUITE_LEVEL_TYPES);
    } else {
      return Collections.emptyList();
    }

    List<TestItem> changedItems = new ArrayList<>();
    testItems.forEach(testItem -> {
      boolean changed = false;
      if (isNameChanged && identifierGenerator.validate(testItem.getUniqueId())) {
        testItem.setUniqueId(
            identifierGenerator.generate(testItem, IdentityUtil.getParentIds(testItem), newLaunch));
        changed = true;
      }
      if (extendDescription && testItem.getType().sameLevel(TestItemTypeEnum.SUITE)) {
        // Add launch reference description for top level items
        Supplier<String> newDescription = Suppliers.formattedSupplier(
            ofNullable(testItem.getDescription()).orElse("") + "\r\n@launch '{} #{}'",
            launch.getName(), launch.getNumber()
        );
        testItem.setDescription(newDescription.get());
        changed = true;
      }
      if (changed) {
        changedItems.add(testItem);
      }
    });
    return changedItems;
  }
}
