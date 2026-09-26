package com.epam.reportportal.base.core.tms.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsTestCaseFixRoundRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsTestCaseReviewCommentRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.enums.TmsFixRoundStatus;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.enums.TmsReviewCommentState;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TmsTestCaseReadyEligibilityServiceTest {

  @Mock
  private TmsTestCaseReviewCommentRepository commentRepository;
  @Mock
  private TmsTestCaseFixRoundRepository fixRoundRepository;

  @InjectMocks
  private TmsTestCaseReadyEligibilityService sut;

  @Test
  void findCasesThatCannotBecomeReady_ShouldContainCasesWithUnsentCommentsAndCasesTheAgentIsFixing() {
    var ids = List.of(1L, 2L, 3L);
    when(commentRepository.findTestCaseIdsWithState(ids, TmsReviewCommentState.NOT_SENT)).thenReturn(List.of(1L));
    when(fixRoundRepository.findTestCaseIdsWithStatus(ids, TmsFixRoundStatus.PENDING)).thenReturn(List.of(2L));

    assertEquals(Set.of(1L, 2L), sut.findCasesThatCannotBecomeReady(ids));
  }

  @Test
  void findCasesThatCannotBecomeReady_WithoutCases_ShouldBeEmptyWithoutAskingTheDatabase() {
    assertTrue(sut.findCasesThatCannotBecomeReady(List.of()).isEmpty());
  }

  @Test
  void canBecomeReady_WhenNothingBlocksTheCase_ShouldBeTrue() {
    lenient().when(commentRepository.findTestCaseIdsWithState(List.of(1L), TmsReviewCommentState.NOT_SENT))
        .thenReturn(List.of());
    lenient().when(fixRoundRepository.findTestCaseIdsWithStatus(List.of(1L), TmsFixRoundStatus.PENDING))
        .thenReturn(List.of());

    assertTrue(sut.canBecomeReady(1L));
  }

  @Test
  void assertCanBecomeReady_WhileTheAgentIsFixing_ShouldBeAConflict() {
    when(fixRoundRepository.existsByTestCaseIdAndStatus(1L, TmsFixRoundStatus.PENDING)).thenReturn(true);

    var ex = assertThrows(ReportPortalException.class, () -> sut.assertCanBecomeReady(1L));

    assertEquals(ErrorType.TEST_CASE_REVIEW_CONFLICT.getCode(), ex.getErrorType().getCode());
  }

  @Test
  void assertCanBecomeReady_WithUnsentComments_ShouldBeAConflict() {
    when(commentRepository.countByTestCaseIdAndState(1L, TmsReviewCommentState.NOT_SENT)).thenReturn(1L);

    var ex = assertThrows(ReportPortalException.class, () -> sut.assertCanBecomeReady(1L));

    assertEquals(ErrorType.TEST_CASE_REVIEW_CONFLICT.getCode(), ex.getErrorType().getCode());
  }

  @Test
  void assertCanBecomeReady_WithNoCommentsAndNoRound_ShouldPass() {
    assertDoesNotThrow(() -> sut.assertCanBecomeReady(1L));
  }
}
