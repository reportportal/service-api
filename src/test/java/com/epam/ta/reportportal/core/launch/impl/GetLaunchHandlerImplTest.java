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

package com.epam.ta.reportportal.core.launch.impl;

import static com.epam.ta.reportportal.OrganizationUtil.TEST_PROJECT_KEY;
import static com.epam.ta.reportportal.ReportPortalUserUtil.getRpUser;
import static com.epam.ta.reportportal.commons.querygen.constant.LaunchCriteriaConstant.CRITERIA_LAUNCH_STATUS;
import static com.epam.ta.reportportal.core.launch.impl.LaunchTestUtil.getLaunch;
import static com.epam.ta.reportportal.util.MembershipUtils.rpUserToMembership;
import static com.epam.ta.reportportal.util.TestProjectExtractor.extractProjectDetails;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.epam.reportportal.model.launch.cluster.ClusterInfoResource;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.FilterCondition;
import com.epam.ta.reportportal.core.jasper.GetJasperReportHandler;
import com.epam.ta.reportportal.core.jasper.util.JasperDataProvider;
import com.epam.ta.reportportal.core.launch.cluster.GetClusterHandler;
import com.epam.ta.reportportal.dao.ItemAttributeRepository;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.dao.UserRepository;
import com.epam.ta.reportportal.dao.WidgetContentRepository;
import com.epam.ta.reportportal.entity.enums.LaunchModeEnum;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.jasper.ReportFormat;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.organization.MembershipDetails;
import com.epam.ta.reportportal.entity.organization.OrganizationRole;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.model.Page;
import com.epam.ta.reportportal.ws.converter.converters.LaunchConverter;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@ExtendWith(MockitoExtension.class)
class GetLaunchHandlerImplTest {

  @Mock
  private GetClusterHandler getClusterHandler;

  @Mock
  private LaunchRepository launchRepository;

  @Mock
  private ItemAttributeRepository itemAttributeRepository;

  @Mock
  private ProjectRepository projectRepository;

  @Mock
  private WidgetContentRepository widgetContentRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private JasperDataProvider jasperDataProvider;

  @Mock
  private GetJasperReportHandler<Launch> getJasperReportHandler;

  @Mock
  private LaunchConverter launchConverter;

  @InjectMocks
  private GetLaunchHandlerImpl handler;

  @Test
  void getLaunchFromOtherProject() {
    final ReportPortalUser rpUser =
        getRpUser("test", UserRole.ADMINISTRATOR, OrganizationRole.MEMBER, ProjectRole.EDITOR, 2L);
    when(launchRepository.findById(1L)).thenReturn(
        getLaunch(StatusEnum.FAILED, LaunchModeEnum.DEFAULT));

    final ReportPortalException exception = assertThrows(ReportPortalException.class,
        () -> handler.getLaunch("1", rpUserToMembership(rpUser))
    );
    assertEquals("You do not have enough permissions.", exception.getMessage());
  }

  @Test
  @Disabled("waiting for requirements")
  void getDebugLaunchWithCustomerRole() {
    final ReportPortalUser rpUser = getRpUser("test", UserRole.USER, OrganizationRole.MEMBER, ProjectRole.VIEWER, 1L);
    when(launchRepository.findById(1L)).thenReturn(
        getLaunch(StatusEnum.PASSED, LaunchModeEnum.DEBUG));

    final ReportPortalException exception = assertThrows(ReportPortalException.class,
        () -> handler.getLaunch("1", rpUserToMembership(rpUser))
    );
    assertEquals("You do not have enough permissions.", exception.getMessage());
  }

  @Test
  void getLaunchNamesIncorrectInput() {
    final ReportPortalUser rpUser = getRpUser("test", UserRole.ADMINISTRATOR, OrganizationRole.MEMBER, ProjectRole.VIEWER, 1L);

    assertThrows(ReportPortalException.class,
        () -> handler.getLaunchNames(rpUserToMembership(rpUser),
            RandomStringUtils.random(257)
        )
    );
  }

  @Test
  void getNotExistLaunch() {
    ReportPortalUser user = getRpUser("user", UserRole.USER, OrganizationRole.MEMBER, ProjectRole.VIEWER, 1L);
    String launchId = "1";

    when(launchRepository.findById(Long.parseLong(launchId))).thenReturn(Optional.empty());

    ReportPortalException exception = assertThrows(ReportPortalException.class,
        () -> handler.getLaunch(launchId, extractProjectDetails(user, TEST_PROJECT_KEY))
    );
    assertEquals("Launch '1' not found. Did you use correct Launch ID?", exception.getMessage());
  }

  @Test
  void getLaunchByNotExistProjectName() {
    String projectKey = "not_exist";

    when(projectRepository.findByKey(projectKey)).thenReturn(Optional.empty());

    ReportPortalException exception = assertThrows(ReportPortalException.class,
        () -> handler.getLaunchByProjectKey(projectKey, PageRequest.of(0, 10), getDefaultFilter(),
            "user"
        )
    );
    assertEquals("Project 'not_exist' not found. Did you use correct project name?",
        exception.getMessage()
    );
  }

  @Test
  void getLaunchByProjectNameNotFound() {
    String projectKey = "not_exist";

    when(projectRepository.findByKey(projectKey)).thenReturn(Optional.of(new Project()));
    when(launchRepository.findByFilter(any(), any())).thenReturn(null);

    ReportPortalException exception = assertThrows(ReportPortalException.class,
        () -> handler.getLaunchByProjectKey(projectKey, PageRequest.of(0, 10), getDefaultFilter(),
            "user"
        )
    );
    assertEquals("Launch '' not found. Did you use correct Launch ID?", exception.getMessage());
  }

  @Test
  void getLaunchesByNotExistProject() {
    long projectId = 1L;
    ReportPortalUser user =
        getRpUser("user", UserRole.USER, OrganizationRole.MANAGER, ProjectRole.EDITOR,  projectId);

    when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

    ReportPortalException exception = assertThrows(ReportPortalException.class,
        () -> handler.getProjectLaunches(extractProjectDetails(user, TEST_PROJECT_KEY),
            getDefaultFilter(), PageRequest.of(0, 10), "user"
        )
    );
    assertEquals("Project '1' not found. Did you use correct project name?",
        exception.getMessage()
    );
  }

  @Test
  void getLatestLaunchesOnNotExistProject() {
    long projectId = 1L;
    ReportPortalUser user =
        getRpUser("user", UserRole.USER, OrganizationRole.MANAGER, ProjectRole.EDITOR,  projectId);

    when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

    ReportPortalException exception = assertThrows(ReportPortalException.class,
        () -> handler.getLatestLaunches(extractProjectDetails(user, TEST_PROJECT_KEY),
            getDefaultFilter(), PageRequest.of(0, 10)
        )
    );
    assertEquals("Project '1' not found. Did you use correct project name?",
        exception.getMessage()
    );
  }

  @Test
  void getOwnersWrongTerm() {
    long projectId = 1L;
    ReportPortalUser user =
        getRpUser("user", UserRole.USER, OrganizationRole.MANAGER, ProjectRole.EDITOR,  projectId);

    ReportPortalException exception = assertThrows(ReportPortalException.class,
        () -> handler.getOwners(extractProjectDetails(user, TEST_PROJECT_KEY), "qw",
            LaunchModeEnum.DEFAULT.name()
        )
    );
    assertEquals(
        "Incorrect filtering parameters. Length of the filtering string 'qw' is less than 3 symbols",
        exception.getMessage()
    );
  }

  @Test
  void getOwnersWrongMode() {
    long projectId = 1L;
    ReportPortalUser user =
        getRpUser("user", UserRole.USER, OrganizationRole.MANAGER, ProjectRole.EDITOR,  projectId);

    ReportPortalException exception = assertThrows(ReportPortalException.class,
        () -> handler.getOwners(extractProjectDetails(user, TEST_PROJECT_KEY), "qwe", "incorrectMode")
    );
    assertEquals("Incorrect filtering parameters. Mode - incorrectMode doesn't exist.",
        exception.getMessage()
    );
  }

  @Test
  void exportLaunchNotFound() {
    long launchId = 1L;
    ReportPortalUser user = getRpUser("user", UserRole.USER, OrganizationRole.MEMBER, ProjectRole.VIEWER, 1L);
    MembershipDetails membershipDetails = new MembershipDetails();
    membershipDetails.setProjectId(1L);

    when(launchRepository.findById(launchId)).thenReturn(Optional.empty());

    ReportPortalException exception = assertThrows(ReportPortalException.class,
        () -> handler.exportLaunch(launchId, ReportFormat.PDF, null, user, membershipDetails)
    );
    assertEquals("Launch '1' not found. Did you use correct Launch ID?", exception.getMessage());
  }

  @Test
  void exportLaunchUserNotFound() {
    long launchId = 1L;
    ReportPortalUser user = getRpUser("user", UserRole.USER, OrganizationRole.MEMBER, ProjectRole.VIEWER, 1L);
    MembershipDetails membershipDetails = new MembershipDetails();
    membershipDetails.setProjectId(1L);

    Launch launch = new Launch();
    launch.setProjectId(1L);
    launch.setStatus(StatusEnum.FAILED);
    when(launchRepository.findById(launchId)).thenReturn(Optional.of(launch));
    when(userRepository.findById(1L)).thenReturn(Optional.empty());

    ReportPortalException exception = assertThrows(ReportPortalException.class,
        () -> handler.exportLaunch(launchId, ReportFormat.PDF, null, user, membershipDetails)
    );
    assertEquals("User '1' not found.", exception.getMessage());
  }

  @Test
  @Disabled("waiting for requirements")
  void getLaunchInDebugModeByCustomer() {
    long projectId = 1L;
    ReportPortalUser user = getRpUser("user", UserRole.USER, OrganizationRole.MEMBER, ProjectRole.VIEWER, projectId);
    String launchId = "1";

    Launch launch = new Launch();
    launch.setProjectId(projectId);
    launch.setMode(LaunchModeEnum.DEBUG);
    when(launchRepository.findById(Long.parseLong(launchId))).thenReturn(Optional.of(launch));

    ReportPortalException exception = assertThrows(ReportPortalException.class,
        () -> handler.getLaunch(launchId, extractProjectDetails(user, TEST_PROJECT_KEY))
    );
    assertEquals("You do not have enough permissions.", exception.getMessage());
  }

  @Test
  void getClusterInfo() {
    long projectId = 1L;
    ReportPortalUser user = getRpUser("user", UserRole.USER, OrganizationRole.MEMBER, ProjectRole.VIEWER, projectId);
    String launchId = "1";

    Launch launch = new Launch();
    launch.setProjectId(projectId);
    launch.setMode(LaunchModeEnum.DEBUG);
    when(launchRepository.findById(Long.parseLong(launchId))).thenReturn(Optional.of(launch));

    final Pageable pageable = PageRequest.of(1, 2);

    final Page<ClusterInfoResource> expected =
        new Page<>(List.of(new ClusterInfoResource(), new ClusterInfoResource()), 2, 1, 10);

    when(getClusterHandler.getResources(launch, pageable)).thenReturn(expected);

    final Iterable<ClusterInfoResource> result =
        handler.getClusters(launchId, extractProjectDetails(user, TEST_PROJECT_KEY), pageable);

    final Page<ClusterInfoResource> castedResult = (Page<ClusterInfoResource>) result;

    assertEquals(expected.getPage().getNumber(), castedResult.getPage().getNumber());
    assertEquals(expected.getPage().getSize(), castedResult.getPage().getSize());
    assertEquals(expected.getPage().getTotalElements(), castedResult.getPage().getTotalElements());

    assertEquals(10, castedResult.getPage().getTotalElements());
    assertEquals(1, castedResult.getPage().getNumber());
    assertEquals(2, castedResult.getPage().getSize());

    assertEquals(2, castedResult.getContent().size());
  }

  private Filter getDefaultFilter() {
    return Filter.builder().withTarget(Launch.class)
        .withCondition(FilterCondition.builder().eq(CRITERIA_LAUNCH_STATUS, "PASSED").build())
        .build();
  }
}
