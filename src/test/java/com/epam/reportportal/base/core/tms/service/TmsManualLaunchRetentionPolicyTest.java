package com.epam.reportportal.base.core.tms.service;

import static com.epam.reportportal.base.core.settings.ImportantLaunchSettingHandler.IMPORTANT_SETTINGS_KEY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.core.item.TestItemService;
import com.epam.reportportal.base.core.launch.DeleteLaunchHandler;
import com.epam.reportportal.base.core.launch.attribute.impl.RetentionPolicyAttributeHandler;
import com.epam.reportportal.base.core.project.ProjectService;
import com.epam.reportportal.base.core.settings.ServerSettingsService;
import com.epam.reportportal.base.core.tms.dto.CreateTmsManualLaunchRQ;
import com.epam.reportportal.base.core.tms.dto.CreateTmsManualLaunchRS;
import com.epam.reportportal.base.core.tms.dto.TmsManualLaunchRQ;
import com.epam.reportportal.base.core.tms.dto.TmsManualLaunchRS;
import com.epam.reportportal.base.core.tms.mapper.TmsManualLaunchMapper;
import com.epam.reportportal.base.core.user.GetUserHandler;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.dao.LaunchAttributeRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.LaunchRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.filterable.TmsManualLaunchFilterableRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.RetentionPolicyEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.launch.Launch;
import com.epam.reportportal.base.infrastructure.persistence.entity.launch.LaunchAttribute;
import com.epam.reportportal.base.reporting.ItemAttributesRQ;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class TmsManualLaunchRetentionPolicyTest {

  private static final long PROJECT_ID = 1L;
  private static final long LAUNCH_ID = 5L;

  @Mock
  private LaunchRepository launchRepository;
  @Mock
  private TmsManualLaunchFilterableRepository tmsManualLaunchFilterableRepository;
  @Mock
  private TmsManualLaunchMapper tmsManualLaunchMapper;
  @Mock
  private TestItemService testItemService;
  @Mock
  private DeleteLaunchHandler deleteLaunchHandler;
  @Mock
  private TestFolderItemServiceImpl testFolderItemService;
  @Mock
  private TmsStepExecutionService tmsStepExecutionService;
  @Mock
  private GetUserHandler getUserHandler;
  @Mock
  private TmsTestPlanService tmsTestPlanService;
  @Mock
  private TmsTestCaseExecutionService tmsTestCaseExecutionService;
  @Mock
  private LaunchAttributeRepository launchAttributeRepository;
  @Mock
  private ApplicationEventPublisher eventPublisher;
  @Mock
  private ProjectService projectService;
  @Mock
  private ServerSettingsService serverSettingsService;

  private TmsManualLaunchServiceImpl service;

  @BeforeEach
  void setUp() {
    var attributeService = new TmsManualLaunchAttributeServiceImpl(launchAttributeRepository);
    var handler = new RetentionPolicyAttributeHandler(eventPublisher, projectService,
        serverSettingsService);
    service = new TmsManualLaunchServiceImpl(launchRepository, tmsManualLaunchFilterableRepository,
        tmsManualLaunchMapper, attributeService, testItemService, deleteLaunchHandler,
        testFolderItemService, tmsStepExecutionService, getUserHandler, tmsTestPlanService,
        handler);
    service.setTmsTestCaseExecutionService(tmsTestCaseExecutionService);
    lenient().when(launchRepository.save(any(Launch.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    lenient().when(launchAttributeRepository.saveAll(any())).thenAnswer(invocation ->
        StreamSupport.stream(((Iterable<?>) invocation.getArgument(0)).spliterator(), false)
            .toList());
  }

  @Test
  void create_systemImportant_setsColumnAndKeepsSystemAttribute() {
    var launch = new Launch();
    var request = new CreateTmsManualLaunchRQ();
    request.setName("Manual");
    request.setTestCaseIds(List.of(1L));
    request.setAttributes(List.of(
        new ItemAttributesRQ("retentionPolicy", "IMPORTANT", true),
        new ItemAttributesRQ("team", "qa")
    ));
    when(tmsManualLaunchMapper.convertFromCreateTmsManualLaunchRQ(eq(PROJECT_ID), any(),
        eq(request)))
        .thenReturn(launch);
    when(tmsManualLaunchMapper.convertToCreateTmsManualLaunchRS(eq(launch), any()))
        .thenReturn(new CreateTmsManualLaunchRS());

    service.create(PROJECT_ID, mock(ReportPortalUser.class), request);

    assertEquals(RetentionPolicyEnum.IMPORTANT, launch.getRetentionPolicy());
    assertTrue(launch.getAttributes().stream().anyMatch(attribute ->
        Boolean.TRUE.equals(attribute.isSystem())
            && "retentionPolicy".equals(attribute.getKey())
            && "IMPORTANT".equals(attribute.getValue())));
    assertTrue(launch.getAttributes().stream().anyMatch(attribute ->
        "team".equals(attribute.getKey()) && !Boolean.TRUE.equals(attribute.isSystem())));
    verifyNoInteractions(eventPublisher);
  }

  @Test
  void create_withoutRetention_staysRegular() {
    var launch = new Launch();
    var request = new CreateTmsManualLaunchRQ();
    request.setName("Manual");
    request.setTestCaseIds(List.of(1L));
    request.setAttributes(List.of(new ItemAttributesRQ("team", "qa")));
    when(tmsManualLaunchMapper.convertFromCreateTmsManualLaunchRQ(eq(PROJECT_ID), any(),
        eq(request)))
        .thenReturn(launch);
    when(tmsManualLaunchMapper.convertToCreateTmsManualLaunchRS(eq(launch), any()))
        .thenReturn(new CreateTmsManualLaunchRS());

    service.create(PROJECT_ID, mock(ReportPortalUser.class), request);

    assertEquals(RetentionPolicyEnum.REGULAR, launch.getRetentionPolicy());
  }

  @Test
  void create_featureOff_ignoresImportant() {
    when(serverSettingsService.checkServerSettingsState(IMPORTANT_SETTINGS_KEY, "false"))
        .thenReturn(true);
    var launch = new Launch();
    var request = new CreateTmsManualLaunchRQ();
    request.setName("Manual");
    request.setTestCaseIds(List.of(1L));
    request.setAttributes(List.of(new ItemAttributesRQ("retentionPolicy", "IMPORTANT", true)));
    when(tmsManualLaunchMapper.convertFromCreateTmsManualLaunchRQ(eq(PROJECT_ID), any(),
        eq(request)))
        .thenReturn(launch);
    when(tmsManualLaunchMapper.convertToCreateTmsManualLaunchRS(eq(launch), any()))
        .thenReturn(new CreateTmsManualLaunchRS());

    service.create(PROJECT_ID, mock(ReportPortalUser.class), request);

    assertEquals(RetentionPolicyEnum.REGULAR, launch.getRetentionPolicy());
  }

  @Test
  void patch_userAttributes_doNotChangeRetention() {
    var launch = importantLaunch();
    when(launchRepository.findManualLaunchByIdAndProjectId(LAUNCH_ID, PROJECT_ID))
        .thenReturn(Optional.of(launch));
    when(tmsManualLaunchMapper.convert(eq(launch), any())).thenReturn(new TmsManualLaunchRS());

    var request = TmsManualLaunchRQ.builder()
        .attributes(List.of(new ItemAttributesRQ("environment", "staging")))
        .build();

    service.patch(PROJECT_ID, LAUNCH_ID, request);

    assertEquals(RetentionPolicyEnum.IMPORTANT, launch.getRetentionPolicy());
    assertTrue(launch.getAttributes().stream().anyMatch(attribute ->
        Boolean.TRUE.equals(attribute.isSystem()) && "important".equals(attribute.getValue())));
    assertTrue(launch.getAttributes().stream().anyMatch(attribute ->
        "environment".equals(attribute.getKey())));
  }

  @Test
  void patch_systemRegular_clearsImportance() {
    var launch = importantLaunch();
    when(launchRepository.findManualLaunchByIdAndProjectId(LAUNCH_ID, PROJECT_ID))
        .thenReturn(Optional.of(launch));
    when(tmsManualLaunchMapper.convert(eq(launch), any())).thenReturn(new TmsManualLaunchRS());

    var request = TmsManualLaunchRQ.builder()
        .attributes(List.of(new ItemAttributesRQ("retentionPolicy", "REGULAR", true)))
        .build();

    service.patch(PROJECT_ID, LAUNCH_ID, request);

    assertEquals(RetentionPolicyEnum.REGULAR, launch.getRetentionPolicy());
    assertTrue(launch.getAttributes().stream().anyMatch(attribute ->
        Boolean.TRUE.equals(attribute.isSystem()) && "REGULAR".equals(attribute.getValue())));
  }

  @Test
  void patch_withoutAttributes_keepsRetention() {
    var launch = importantLaunch();
    when(launchRepository.findManualLaunchByIdAndProjectId(LAUNCH_ID, PROJECT_ID))
        .thenReturn(Optional.of(launch));
    when(tmsManualLaunchMapper.convert(eq(launch), any())).thenReturn(new TmsManualLaunchRS());

    service.patch(PROJECT_ID, LAUNCH_ID, TmsManualLaunchRQ.builder().name("Renamed").build());

    assertEquals(RetentionPolicyEnum.IMPORTANT, launch.getRetentionPolicy());
    assertEquals(1, launch.getAttributes().size());
  }

  private static Launch importantLaunch() {
    var launch = new Launch();
    launch.setId(LAUNCH_ID);
    launch.setProjectId(PROJECT_ID);
    launch.setRetentionPolicy(RetentionPolicyEnum.IMPORTANT);
    launch.getAttributes().add(new LaunchAttribute("retentionPolicy", "important", true));
    return launch;
  }
}
