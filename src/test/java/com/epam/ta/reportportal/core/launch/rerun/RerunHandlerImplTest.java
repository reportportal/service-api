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

package com.epam.ta.reportportal.core.launch.rerun;

import static com.epam.ta.reportportal.ReportPortalUserUtil.getRpUser;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.Queryable;
import com.epam.ta.reportportal.core.item.identity.TestCaseHashGenerator;
import com.epam.ta.reportportal.core.item.identity.UniqueIdGenerator;
import com.epam.ta.reportportal.core.item.impl.rerun.RerunSearcher;
import com.epam.ta.reportportal.core.item.impl.retry.RetryHandler;
import com.epam.ta.reportportal.core.item.validator.parent.ParentItemValidator;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.enums.TestItemTypeEnum;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.item.TestItemResults;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.organization.OrganizationRole;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.ws.reporting.ItemAttributesRQ;
import com.epam.ta.reportportal.ws.reporting.ItemCreatedRS;
import com.epam.ta.reportportal.ws.reporting.Mode;
import com.epam.ta.reportportal.ws.reporting.StartLaunchRQ;
import com.epam.ta.reportportal.ws.reporting.StartTestItemRQ;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.util.Pair;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@ExtendWith(MockitoExtension.class)
class RerunHandlerImplTest {

  @Spy
  private ArrayList<ParentItemValidator> parentItemValidators;

  @Mock
  private TestItemRepository testItemRepository;

  @Mock
  private LaunchRepository launchRepository;

  @Mock
  private UniqueIdGenerator uniqueIdGenerator;

  @Mock
  private TestCaseHashGenerator testCaseHashGenerator;

  @Mock
  private ApplicationEventPublisher eventPublisher;

  @Mock
  private RerunSearcher rerunSearcher;

  @Mock
  private RetryHandler retryHandler;

  @InjectMocks
  private RerunHandlerImpl rerunHandler;

  @Test
  void exceptionWhenLaunchIsNotStoredInDbByName() {
    StartLaunchRQ request = new StartLaunchRQ();
    String launchName = "launch";
    long projectId = 1L;
    request.setRerun(true);
    request.setName(launchName);
    ReportPortalUser rpUser = getRpUser("test", UserRole.USER, OrganizationRole.MANAGER, ProjectRole.EDITOR,
        projectId);

    when(launchRepository.findLatestByNameAndProjectId(launchName, projectId)).thenReturn(
        Optional.empty());

    ReportPortalException exception = assertThrows(ReportPortalException.class,
        () -> rerunHandler.handleLaunch(request, projectId, rpUser)
    );
    assertEquals("Launch 'launch' not found. Did you use correct Launch ID?",
        exception.getMessage());
  }

  @Test
  void exceptionWhenLaunchIsNotStoredInDbByUuid() {
    StartLaunchRQ request = new StartLaunchRQ();
    String launchName = "launch";
    String uuid = "uuid";
    long projectId = 1L;
    request.setRerun(true);
    request.setRerunOf(uuid);
    request.setName(launchName);
    ReportPortalUser rpUser = getRpUser("test", UserRole.USER, OrganizationRole.MANAGER, ProjectRole.EDITOR, 
        projectId);

    when(launchRepository.findByUuid(uuid)).thenReturn(Optional.empty());

    ReportPortalException exception = assertThrows(ReportPortalException.class,
        () -> rerunHandler.handleLaunch(request, projectId, rpUser)
    );
    assertEquals("Launch 'uuid' not found. Did you use correct Launch ID?", exception.getMessage());
  }

  @Test
  void happyRerunLaunch() {
    StartLaunchRQ request = new StartLaunchRQ();
    String launchName = "launch";
    long projectId = 1L;
    request.setRerun(true);
    request.setName(launchName);
    request.setMode(Mode.DEFAULT);
    request.setDescription("desc");
    request.setAttributes(Sets.newHashSet(new ItemAttributesRQ("test", "test")));
    ReportPortalUser rpUser = getRpUser("test", UserRole.USER, OrganizationRole.MANAGER, ProjectRole.EDITOR, 
        projectId);

    when(launchRepository.findLatestByNameAndProjectId("launch", projectId)).thenReturn(
        Optional.of(getLaunch("uuid")));

    final Launch launch = rerunHandler.handleLaunch(request, projectId, rpUser);

    assertNotNull(launch.getNumber());
    assertNotNull(launch.getId());

  }

  @Test
  void returnEmptyOptionalWhenRootItemNotFound() {
    StartTestItemRQ request = new StartTestItemRQ();
    request.setLaunchUuid("launch_uuid");
    request.setType("STEP");
    String itemName = "name";
    request.setName(itemName);
    final String testCaseId = "caseId";
    request.setTestCaseId(testCaseId);
    Launch launch = getLaunch("uuid");

    when(rerunSearcher.findItem(any(Queryable.class))).thenReturn(Optional.empty());

    Optional<ItemCreatedRS> rerunCreatedRS = rerunHandler.handleRootItem(request, launch);

    assertFalse(rerunCreatedRS.isPresent());
  }

  @Test
  void happyRerunRootItem() {
    StartTestItemRQ request = new StartTestItemRQ();
    request.setLaunchUuid("launch_uuid");
    request.setType("STEP");
    String itemName = "name";
    request.setName(itemName);
    final String testCaseId = "caseId";
    request.setTestCaseId(testCaseId);
    Launch launch = getLaunch("uuid");

    final TestItem item = getItem(itemName, launch);
    when(rerunSearcher.findItem(any(Queryable.class))).thenReturn(Optional.of(item.getItemId()));
    when(testItemRepository.findById(item.getItemId())).thenReturn(Optional.of(item));

    Optional<ItemCreatedRS> rerunCreatedRS = rerunHandler.handleRootItem(request, launch);

    assertTrue(rerunCreatedRS.isPresent());
  }

  @Test
  void returnEmptyOptionalWhenChildItemNotFound() {
    StartTestItemRQ request = new StartTestItemRQ();
    request.setLaunchUuid("launch_uuid");
    request.setType("STEP");
    String itemName = "name";
    request.setName(itemName);
    final String testCaseId = "caseId";
    request.setTestCaseId(testCaseId);
    Launch launch = getLaunch("uuid");
    TestItem parent = new TestItem();
    parent.setItemId(2L);
    parent.setPath("1.2");

    when(rerunSearcher.findItem(any(Queryable.class))).thenReturn(Optional.empty());
    when(testItemRepository.selectPath("uuid")).thenReturn(
        Optional.of(Pair.of(parent.getItemId(), parent.getPath())));

    Optional<ItemCreatedRS> rerunCreatedRS = rerunHandler.handleChildItem(request, launch, "uuid");

    assertFalse(rerunCreatedRS.isPresent());
  }

  @Test
  void happyRerunChildItem() {
    StartTestItemRQ request = new StartTestItemRQ();
    request.setLaunchUuid("launch_uuid");
    request.setType("STEP");
    String itemName = "name";
    request.setName(itemName);
    final String testCaseId = "caseId";
    request.setTestCaseId(testCaseId);
    Launch launch = getLaunch("uuid");
    TestItem parent = new TestItem();
    parent.setItemId(2L);
    parent.setPath("1.2");

    final TestItem item = getItem(itemName, launch);
    when(rerunSearcher.findItem(any(Queryable.class))).thenReturn(Optional.of(item.getItemId()));
    when(testItemRepository.findById(item.getItemId())).thenReturn(Optional.of(item));
    when(testItemRepository.selectPath("uuid")).thenReturn(
        Optional.of(Pair.of(parent.getItemId(), parent.getPath())));
    when(testItemRepository.findIdByUuidForUpdate("uuid")).thenReturn(
        Optional.of(parent.getItemId()));
    when(testItemRepository.getOne(parent.getItemId())).thenReturn(parent);

    Optional<ItemCreatedRS> rerunCreatedRS = rerunHandler.handleChildItem(request, launch, "uuid");

    verify(retryHandler, times(1)).handleRetries(any(), any(), any());

    assertTrue(rerunCreatedRS.isPresent());
  }

  @Test
  void shouldReturnRerunOfUuidWhenProvided() {
    // When
    String rerunOf = "rerun-uuid";
    String launchName = "test-launch";
    Long projectId = 1L;

    // Given
    String result = rerunHandler.getRerunLaunchUuid(rerunOf, launchName, projectId);

    // Then
    assertEquals("rerun-uuid", result);
    verifyNoInteractions(launchRepository);
  }

  @Test
  void shouldThrowExceptionWhenNoRerunOfAndNoLaunchFound() {
    // When
    String rerunOf = null; // No 'rerunOf' provided.
    String launchName = "test-launch";
    Long projectId = 1L;

    when(launchRepository.findLatestByNameAndProjectId(launchName, projectId))
        .thenReturn(Optional.empty()); // Simulate no launch found.

    // Given & Then
    Exception exception = assertThrows(ReportPortalException.class, () -> {
      rerunHandler.getRerunLaunchUuid(rerunOf, launchName, projectId);
    });

    assertEquals(ErrorType.LAUNCH_NOT_FOUND, ((ReportPortalException) exception).getErrorType());
    verify(launchRepository).findLatestByNameAndProjectId(launchName, projectId);
  }

  @Test
  void shouldReturnLatestLaunchUuidWhenNoRerunOfProvided() {
    // When
    String rerunOf = null; // No 'rerunOf' provided.
    String launchName = "test-launch";
    Long projectId = 1L;

    Launch mockLaunch = new Launch();
    mockLaunch.setUuid("latest-uuid");
    when(launchRepository.findLatestByNameAndProjectId(launchName, projectId))
        .thenReturn(Optional.of(mockLaunch)); // Simulate a launch being found.

    // Given
    String result = rerunHandler.getRerunLaunchUuid(rerunOf, launchName, projectId);

    // Then
    assertEquals("latest-uuid", result);
    verify(launchRepository).findLatestByNameAndProjectId(launchName, projectId);
  }

  @Test
  void shouldHandleEmptyRerunOfGracefully() {
    // When
    String rerunOf = ""; // Explicitly set 'rerunOf' to an empty string.
    String launchName = "test-launch";
    Long projectId = 1L;

    when(launchRepository.findLatestByNameAndProjectId(launchName, projectId))
        .thenReturn(Optional.empty()); // Simulate no launch found.

    // Given & Then
    Exception exception = assertThrows(ReportPortalException.class, () -> {
      rerunHandler.getRerunLaunchUuid(rerunOf, launchName, projectId);
    });

    assertEquals(ErrorType.LAUNCH_NOT_FOUND, ((ReportPortalException) exception).getErrorType());
    verify(launchRepository).findLatestByNameAndProjectId(launchName, projectId);
  }

  private TestItem getItem(String name, Launch launch) {
    TestItem item = new TestItem();
    item.setItemId(1L);
    item.setName(name);
    item.setLaunchId(launch.getId());
    item.setDescription("desc");
    item.setType(TestItemTypeEnum.STEP);
    TestItemResults itemResults = new TestItemResults();
    itemResults.setStatus(StatusEnum.PASSED);
    item.setItemResults(itemResults);
    return item;
  }

  private Launch getLaunch(String uuid) {
    Launch launch = new Launch();
    launch.setUuid(uuid);
    launch.setNumber(1L);
    launch.setId(1L);
    return launch;
  }
}
