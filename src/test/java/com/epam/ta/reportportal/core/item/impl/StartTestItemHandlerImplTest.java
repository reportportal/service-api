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

package com.epam.ta.reportportal.core.item.impl;

import static com.epam.ta.reportportal.OrganizationUtil.TEST_PROJECT_KEY;
import static com.epam.ta.reportportal.ReportPortalUserUtil.getRpUser;
import static com.epam.ta.reportportal.util.MembershipUtils.rpUserToMembership;
import static com.epam.ta.reportportal.util.TestProjectExtractor.extractProjectDetails;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.reportportal.rules.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.item.validator.parent.ParentItemValidator;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.item.TestItemResults;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.organization.OrganizationRole;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.ta.reportportal.ws.reporting.StartTestItemRQ;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@ExtendWith(MockitoExtension.class)
class StartTestItemHandlerImplTest {

  private final ParentItemValidator validator = mock(ParentItemValidator.class);

  @Mock
  private LaunchRepository launchRepository;

  @Mock
  private TestItemRepository testItemRepository;

  @InjectMocks
  private StartTestItemHandlerImpl handler;

  @Spy
  private ArrayList<ParentItemValidator> parentItemValidators = new ArrayList<>();

  @BeforeEach
  public void setup() throws Exception {
    parentItemValidators.clear();
    parentItemValidators.add(validator);
  }

  @Test
  void startRootItemUnderNotExistedLaunch() {
    final ReportPortalUser rpUser = getRpUser("test", UserRole.USER, OrganizationRole.MEMBER, ProjectRole.VIEWER, 1L);

    when(launchRepository.findByUuid("1")).thenReturn(Optional.empty());
    final StartTestItemRQ rq = new StartTestItemRQ();
    rq.setLaunchUuid("1");

    final ReportPortalException exception = assertThrows(ReportPortalException.class,
        () -> handler.startRootItem(rpUser, rpUserToMembership(rpUser), rq)
    );
    assertEquals("Launch '1' not found. Did you use correct Launch ID?", exception.getMessage());
  }

  @Test
  void startRootItemUnderLaunchFromAnotherProject() {
    final ReportPortalUser rpUser = getRpUser("test", UserRole.USER, OrganizationRole.MEMBER, ProjectRole.VIEWER, 1L);
    StartTestItemRQ startTestItemRQ = new StartTestItemRQ();
    startTestItemRQ.setLaunchUuid("1");
    startTestItemRQ.setStartTime(Instant.now());

    final Launch launch = getLaunch(2L, StatusEnum.IN_PROGRESS);
    launch.setStartTime(Instant.now().minus(1, ChronoUnit.HOURS));
    when(launchRepository.findByUuid("1")).thenReturn(Optional.of(launch));

    final ReportPortalException exception = assertThrows(ReportPortalException.class,
        () -> handler.startRootItem(rpUser, rpUserToMembership(rpUser),
            startTestItemRQ)
    );
    assertEquals("You do not have enough permissions.", exception.getMessage());
  }

  @Test
  @Disabled
  void startRootItemUnderFinishedLaunch() {
    final ReportPortalUser rpUser = getRpUser("test", UserRole.USER, OrganizationRole.MEMBER, ProjectRole.VIEWER, 1L);
    StartTestItemRQ startTestItemRQ = new StartTestItemRQ();
    startTestItemRQ.setLaunchUuid("1");

    when(launchRepository.findByUuid("1")).thenReturn(
        Optional.of(getLaunch(1L, StatusEnum.PASSED)));

    final ReportPortalException exception = assertThrows(ReportPortalException.class,
        () -> handler.startRootItem(rpUser, rpUserToMembership(rpUser),
            startTestItemRQ)
    );
    assertEquals("Start test item is not allowed. Launch '1' is not in progress",
        exception.getMessage());
  }

  @Test
  void startRootItemEarlierThanLaunch() {
    final ReportPortalUser rpUser = getRpUser("test", UserRole.USER, OrganizationRole.MEMBER, ProjectRole.VIEWER, 1L);
    StartTestItemRQ startTestItemRQ = new StartTestItemRQ();
    startTestItemRQ.setLaunchUuid("1");
    startTestItemRQ.setStartTime(Instant.now());

    final Launch launch = getLaunch(1L, StatusEnum.IN_PROGRESS);
    launch.setStartTime(Instant.now().plus(1, ChronoUnit.HOURS));
    when(launchRepository.findByUuid("1")).thenReturn(Optional.of(launch));

    assertThrows(ReportPortalException.class,
        () -> handler.startRootItem(rpUser, rpUserToMembership(rpUser),
            startTestItemRQ)
    );
  }

  @Test
  void startChildItemUnderNotExistedParent() {
    final ReportPortalUser rpUser = getRpUser("test", UserRole.USER, OrganizationRole.MEMBER, ProjectRole.VIEWER, 1L);

    StartTestItemRQ rq = new StartTestItemRQ();
    rq.setLaunchUuid("1");

    when(launchRepository.findByUuid("1")).thenReturn(
        Optional.of(getLaunch(1L, StatusEnum.IN_PROGRESS)));
    when(testItemRepository.findByUuid("1")).thenReturn(Optional.empty());

    final ReportPortalException exception = assertThrows(ReportPortalException.class,
        () -> handler.startChildItem(rpUser, rpUserToMembership(rpUser), rq, "1")
    );
    assertEquals("Test Item '1' not found. Did you use correct Test Item ID?",
        exception.getMessage());
  }

  @Test
  void startChildItemEarlierThanParent() {

    final ReportPortalUser rpUser = getRpUser("test", UserRole.USER, OrganizationRole.MEMBER, ProjectRole.VIEWER, 1L);
    StartTestItemRQ startTestItemRQ = new StartTestItemRQ();
    startTestItemRQ.setLaunchUuid("1");
    startTestItemRQ.setStartTime(Instant.now());

    TestItem item = new TestItem();
    item.setStartTime(Instant.now().plus(1, ChronoUnit.HOURS));
    when(launchRepository.findByUuid("1")).thenReturn(
        Optional.of(getLaunch(1L, StatusEnum.IN_PROGRESS)));
    when(testItemRepository.findByUuid("1")).thenReturn(Optional.of(item));
    doThrow(new ReportPortalException(ErrorType.BAD_REQUEST_ERROR)).when(validator)
        .validate(any(StartTestItemRQ.class), any(TestItem.class));

    assertThrows(ReportPortalException.class,
        () -> handler.startChildItem(rpUser, rpUserToMembership(rpUser),
            startTestItemRQ, "1")
    );
  }

  @Test
  void startChildItemUnderFinishedParent() {
    final ReportPortalUser rpUser = getRpUser("test", UserRole.USER, OrganizationRole.MEMBER, ProjectRole.VIEWER, 1L);
    StartTestItemRQ startTestItemRQ = new StartTestItemRQ();
    startTestItemRQ.setLaunchUuid("1");
    startTestItemRQ.setStartTime(Instant.now());

    TestItem item = new TestItem();
    item.setItemId(1L);
    TestItemResults results = new TestItemResults();
    results.setStatus(StatusEnum.FAILED);
    item.setItemResults(results);
    item.setStartTime(Instant.now());
    when(launchRepository.findByUuid("1")).thenReturn(
        Optional.of(getLaunch(1L, StatusEnum.IN_PROGRESS)));
    when(testItemRepository.findByUuid("1")).thenReturn(Optional.of(item));
    doThrow(new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
        Suppliers.formattedSupplier(
                "Unable to add a not nested step item, because parent item with ID = '{}' is a nested step",
                1L)
            .get()
    )).when(validator).validate(any(StartTestItemRQ.class), any(TestItem.class));

    final ReportPortalException exception = assertThrows(ReportPortalException.class,
        () -> handler.startChildItem(rpUser, rpUserToMembership(rpUser),
            startTestItemRQ, "1")
    );
    assertEquals("Error in handled Request. Please, check specified parameters: "
            + "'Unable to add a not nested step item, because parent item with ID = '1' is a nested step'",
        exception.getMessage());
  }

  @Test
  void startChildItemWithNotExistedLaunch() {
    ReportPortalUser rpUser = getRpUser("test", UserRole.USER, OrganizationRole.MEMBER, ProjectRole.VIEWER, 1L);
    StartTestItemRQ startTestItemRQ = new StartTestItemRQ();
    startTestItemRQ.setLaunchUuid("1");
    startTestItemRQ.setStartTime(Instant.now());
    startTestItemRQ.setLaunchUuid("1");

    when(launchRepository.findByUuid("1")).thenReturn(Optional.empty());

    ReportPortalException exception = assertThrows(ReportPortalException.class,
        () -> handler.startChildItem(rpUser, rpUserToMembership(rpUser),
            startTestItemRQ, "1")
    );

    assertEquals("Launch '1' not found. Did you use correct Launch ID?", exception.getMessage());
  }

  private Launch getLaunch(Long projectId, StatusEnum status) {
    Launch launch = new Launch();
    launch.setProjectId(projectId);
    launch.setStatus(status);
    return launch;
  }
}
