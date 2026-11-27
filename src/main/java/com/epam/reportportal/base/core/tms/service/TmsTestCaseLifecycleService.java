package com.epam.reportportal.base.core.tms.service;

import com.epam.reportportal.base.core.events.domain.tms.TestCaseStatusChangedEvent;
import com.epam.reportportal.base.core.tms.dto.TmsMadeReadyByRS;
import com.epam.reportportal.base.core.tms.dto.TmsUserRS;
import com.epam.reportportal.base.infrastructure.persistence.dao.UserRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsTestCaseRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.EventSubject;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCase;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.enums.TmsLifecycleActorType;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.enums.TmsTestCaseStatus;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.User;
import com.epam.reportportal.base.model.activity.TestCaseActivityResource;
import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Records who changes the Draft/Ready status of a test case and why, and tells "Made Ready by" afterwards.
 * <p>
 * The history of status changes goes to the project activity ({@link TestCaseStatusChangedEvent}); the person or rule
 * that made a case Ready is also kept on the case itself (the {@code ready_by_*} columns), so a page of cases can
 * show it at once and independently of the activity retention.
 * <p>
 * Example: a user approves a Draft case: {@code onStatusChanged(case, DRAFT, Actor.user(7, "user1", "Approved"))} sets
 * "made Ready by user 7 now" on the case and adds "Updated status: Draft to Ready, Approved" to the activity.
 * Auto-Ready does the same with {@code Actor.autoReady(93, 90)} and the case then says {@code AUTO_READY}.
 */
@Service
@RequiredArgsConstructor
public class TmsTestCaseLifecycleService {

  static final String AUTO_READY_NAME = "Auto-Ready";
  static final String AGENT_NAME = "Agent";
  static final String PIPELINE_NAME = "Pipeline";

  private final ApplicationEventPublisher eventPublisher;
  private final UserRepository userRepository;
  private final TmsTestCaseRepository testCaseRepository;

  /**
   * Who changed the status and why.
   *
   * @param type   the kind of actor
   * @param userId the user, for {@code USER} only
   * @param login  the login of the user, for {@code USER} only
   * @param reason shown next to the change in the activity
   */
  public record Actor(TmsLifecycleActorType type, Long userId, String login, String reason) {

    /** A person approved the case or set its status by hand. */
    public static Actor user(Long userId, String login, String reason) {
      return new Actor(TmsLifecycleActorType.USER, userId, login, reason);
    }

    /** Auto-Ready promoted the case because {@code score >= threshold}. */
    public static Actor autoReady(int score, int threshold) {
      return new Actor(TmsLifecycleActorType.AUTO_READY, null, null,
          "Auto-Ready (%d >= %d)".formatted(score, threshold));
    }

    /** The agent fixed the case in a fix round. */
    public static Actor agent(String reason) {
      return new Actor(TmsLifecycleActorType.AGENT, null, null, reason);
    }

    /** The pipeline uploaded the case. */
    public static Actor pipeline(String reason) {
      return new Actor(TmsLifecycleActorType.SYSTEM, null, null, reason);
    }
  }

  /**
   * Called after the status of the case changed from {@code from}: keeps "made Ready by" on the case (set on
   * Draft to Ready, cleared when the case goes back to Draft) and publishes the change to the project activity. The
   * caller saves the case. Does nothing when the status did not change.
   */
  @Transactional
  public void onStatusChanged(TmsTestCase testCase, TmsTestCaseStatus from, Actor actor) {
    var to = testCase.getStatus();
    if (from == to) {
      return;
    }
    if (to == TmsTestCaseStatus.READY && from == TmsTestCaseStatus.DRAFT) {
      testCase.setReadyByType(actor.type());
      testCase.setReadyByUserId(actor.userId());
      testCase.setReadyAt(Instant.now());
    } else if (to == TmsTestCaseStatus.DRAFT) {
      testCase.setReadyByType(null);
      testCase.setReadyByUserId(null);
      testCase.setReadyAt(null);
    }
    var project = testCase.getProject();
    var resource = TestCaseActivityResource.builder()
        .id(testCase.getId())
        .projectId(project == null ? null : project.getId())
        .name(testCase.getName())
        .build();
    eventPublisher.publishEvent(new TestCaseStatusChangedEvent(resource, from == null ? null : from.name(), to.name(),
        actor.reason(), subjectTypeOf(actor), actor.userId(), subjectNameOf(actor),
        project == null ? null : project.getOrganizationId()));
  }

  /**
   * Who made each of the given Ready cases Ready. A Draft case, and a Ready case that was created Ready or made Ready
   * before this was recorded, is not in the result.
   */
  @Transactional(readOnly = true)
  public Map<Long, TmsMadeReadyByRS> madeReadyBy(Collection<Long> testCaseIds) {
    if (testCaseIds.isEmpty()) {
      return Map.of();
    }
    var cases = testCaseRepository.findAllById(testCaseIds).stream()
        .filter(testCase -> testCase.getStatus() == TmsTestCaseStatus.READY && testCase.getReadyByType() != null)
        .toList();
    Set<Long> userIds = cases.stream().map(TmsTestCase::getReadyByUserId).filter(Objects::nonNull)
        .collect(Collectors.toCollection(HashSet::new));
    Map<Long, User> users = userIds.isEmpty() ? Map.of()
        : userRepository.findAllById(userIds).stream().collect(Collectors.toMap(User::getId, Function.identity()));
    var result = new HashMap<Long, TmsMadeReadyByRS>();
    cases.forEach(testCase -> {
      var user = testCase.getReadyByUserId() == null ? null : users.get(testCase.getReadyByUserId());
      result.put(testCase.getId(), TmsMadeReadyByRS.builder()
          .type(testCase.getReadyByType())
          .user(user == null ? null
              : TmsUserRS.builder().id(user.getId()).login(user.getLogin()).fullName(user.getFullName()).build())
          .at(testCase.getReadyAt())
          .build());
    });
    return result;
  }

  private static EventSubject subjectTypeOf(Actor actor) {
    return switch (actor.type()) {
      case USER -> EventSubject.USER;
      case AUTO_READY -> EventSubject.RULE;
      case AGENT, SYSTEM -> EventSubject.APPLICATION;
    };
  }

  private static String subjectNameOf(Actor actor) {
    return switch (actor.type()) {
      case USER -> actor.login();
      case AUTO_READY -> AUTO_READY_NAME;
      case AGENT -> AGENT_NAME;
      case SYSTEM -> PIPELINE_NAME;
    };
  }
}
