package com.epam.reportportal.base.core.tms.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.core.events.domain.tms.TestCaseStatusChangedEvent;
import com.epam.reportportal.base.infrastructure.persistence.dao.UserRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsTestCaseRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.EventSubject;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.Project;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCase;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.enums.TmsLifecycleActorType;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.enums.TmsTestCaseStatus;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.User;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class TmsTestCaseLifecycleServiceTest {

  @Mock
  private ApplicationEventPublisher eventPublisher;
  @Mock
  private UserRepository userRepository;
  @Mock
  private TmsTestCaseRepository testCaseRepository;

  @InjectMocks
  private TmsTestCaseLifecycleService sut;

  private static TmsTestCase testCase(long id, TmsTestCaseStatus status) {
    var project = new Project();
    project.setId(1L);
    project.setOrganizationId(3L);
    var testCase = new TmsTestCase();
    testCase.setId(id);
    testCase.setName("Delete Folder modal. Layout");
    testCase.setStatus(status);
    testCase.setProject(project);
    return testCase;
  }

  private static User user(long id, String login, String fullName) {
    var user = new User();
    user.setId(id);
    user.setLogin(login);
    user.setFullName(fullName);
    return user;
  }

  private TestCaseStatusChangedEvent publishedEvent() {
    var event = ArgumentCaptor.forClass(TestCaseStatusChangedEvent.class);
    verify(eventPublisher).publishEvent(event.capture());
    return event.getValue();
  }

  @Test
  void onStatusChanged_ApprovedByAUser_ShouldMarkWhoMadeItReadyAndPublishTheChange() {
    var testCase = testCase(5, TmsTestCaseStatus.READY);

    sut.onStatusChanged(testCase, TmsTestCaseStatus.DRAFT,
        TmsTestCaseLifecycleService.Actor.user(7L, "user1", "Approved"));

    assertEquals(TmsLifecycleActorType.USER, testCase.getReadyByType());
    assertEquals(7L, testCase.getReadyByUserId());
    assertTrue(testCase.getReadyAt() != null);
    var event = publishedEvent();
    assertEquals("DRAFT", event.getOldStatus());
    assertEquals("READY", event.getNewStatus());
    assertEquals("Approved", event.getReason());
    assertEquals(EventSubject.USER, event.getSubjectType());
    assertEquals("user1", event.getSubjectName());
    assertEquals(7L, event.getSubjectId());
    assertEquals(3L, event.getOrganizationId());
    assertEquals(5L, event.getAfter().getId());
    assertEquals(1L, event.getAfter().getProjectId());
  }

  @Test
  void onStatusChanged_ByAutoReady_ShouldBeARuleWithTheScoreInTheReasonAndNoUser() {
    var testCase = testCase(5, TmsTestCaseStatus.READY);

    sut.onStatusChanged(testCase, TmsTestCaseStatus.DRAFT, TmsTestCaseLifecycleService.Actor.autoReady(93, 90));

    assertEquals(TmsLifecycleActorType.AUTO_READY, testCase.getReadyByType());
    assertNull(testCase.getReadyByUserId());
    var event = publishedEvent();
    assertEquals(EventSubject.RULE, event.getSubjectType());
    assertEquals("Auto-Ready", event.getSubjectName());
    assertEquals("Auto-Ready (93 >= 90)", event.getReason());
    assertNull(event.getSubjectId());
  }

  @Test
  void onStatusChanged_BackToDraft_ShouldClearMadeReadyByAndNameTheAgentAsAnApplication() {
    var testCase = testCase(5, TmsTestCaseStatus.DRAFT);
    testCase.setReadyByType(TmsLifecycleActorType.USER);
    testCase.setReadyByUserId(7L);
    testCase.setReadyAt(Instant.now());

    sut.onStatusChanged(testCase, TmsTestCaseStatus.READY, TmsTestCaseLifecycleService.Actor.agent("Agent fix, round 1"));

    assertNull(testCase.getReadyByType());
    assertNull(testCase.getReadyByUserId());
    assertNull(testCase.getReadyAt());
    var event = publishedEvent();
    assertEquals(EventSubject.APPLICATION, event.getSubjectType());
    assertEquals("Agent", event.getSubjectName());
    assertEquals("READY", event.getOldStatus());
    assertEquals("DRAFT", event.getNewStatus());
  }

  @Test
  void onStatusChanged_WhenTheStatusDidNotChange_ShouldDoNothing() {
    var testCase = testCase(5, TmsTestCaseStatus.DRAFT);

    sut.onStatusChanged(testCase, TmsTestCaseStatus.DRAFT, TmsTestCaseLifecycleService.Actor.agent("Agent fix, round 1"));

    verify(eventPublisher, never()).publishEvent(org.mockito.ArgumentMatchers.any(Object.class));
    assertNull(testCase.getReadyByType());
  }

  @Test
  void madeReadyBy_ShouldAnswerForReadyCasesWithARecordedActorAndTheUsersName() {
    var byUser = testCase(5, TmsTestCaseStatus.READY);
    byUser.setReadyByType(TmsLifecycleActorType.USER);
    byUser.setReadyByUserId(7L);
    byUser.setReadyAt(Instant.parse("2026-11-21T10:00:00Z"));
    var byRule = testCase(6, TmsTestCaseStatus.READY);
    byRule.setReadyByType(TmsLifecycleActorType.AUTO_READY);
    byRule.setReadyAt(Instant.parse("2026-11-21T11:00:00Z"));
    var unknown = testCase(8, TmsTestCaseStatus.READY);
    var draft = testCase(9, TmsTestCaseStatus.DRAFT);
    when(testCaseRepository.findAllById(List.of(5L, 6L, 8L, 9L))).thenReturn(List.of(byUser, byRule, unknown, draft));
    when(userRepository.findAllById(Set.of(7L))).thenReturn(List.of(user(7, "user1", "Ann Smith")));

    var result = sut.madeReadyBy(List.of(5L, 6L, 8L, 9L));

    assertEquals(2, result.size());
    assertEquals(TmsLifecycleActorType.USER, result.get(5L).getType());
    assertEquals("Ann Smith", result.get(5L).getUser().getFullName());
    assertEquals(Instant.parse("2026-11-21T10:00:00Z"), result.get(5L).getAt());
    assertEquals(TmsLifecycleActorType.AUTO_READY, result.get(6L).getType());
    assertNull(result.get(6L).getUser());
    assertNull(result.get(8L));
    assertNull(result.get(9L));
  }

  @Test
  void madeReadyBy_WithNoCases_ShouldNotQuery() {
    assertTrue(sut.madeReadyBy(List.of()).isEmpty());

    verifyNoInteractions(testCaseRepository, userRepository);
  }
}
