package com.epam.reportportal.base.core.tms.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.infrastructure.persistence.dao.UserRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCaseReviewComment;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.User;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TmsReviewCommentAssemblerTest {

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private TmsReviewCommentAssembler sut;

  private static TmsTestCaseReviewComment comment(long id, long authorId) {
    var comment = new TmsTestCaseReviewComment();
    comment.setId(id);
    comment.setAuthorId(authorId);
    comment.setCommentText("Folder A should be in bold");
    return comment;
  }

  private static User user(long id, String login, String fullName) {
    var user = new User();
    user.setId(id);
    user.setLogin(login);
    user.setFullName(fullName);
    return user;
  }

  @Test
  void toRS_ShouldAddTheAuthorsNameAndKeepTheAuthorId() {
    when(userRepository.findAllById(Set.of(7L, 8L)))
        .thenReturn(List.of(user(7, "user1", "Ann Smith"), user(8, "user2", null)));

    var result = sut.toRS(List.of(comment(1, 7), comment(2, 8), comment(3, 7)));

    assertEquals(3, result.size());
    assertEquals(7L, result.get(0).getAuthorId());
    assertEquals("Ann Smith", result.get(0).getAuthor().getFullName());
    assertEquals("user1", result.get(0).getAuthor().getLogin());
    assertEquals("user2", result.get(1).getAuthor().getLogin());
    assertNull(result.get(1).getAuthor().getFullName());
    assertEquals("Ann Smith", result.get(2).getAuthor().getFullName());
  }

  @Test
  void toRS_ForAUserThatNoLongerExists_ShouldLeaveTheAuthorOutAndKeepTheId() {
    when(userRepository.findAllById(Set.of(9L))).thenReturn(List.of());

    var result = sut.toRS(comment(1, 9));

    assertEquals(9L, result.getAuthorId());
    assertNull(result.getAuthor());
  }

  @Test
  void toRS_WithNoComments_ShouldNotAskForUsers() {
    assertEquals(List.of(), sut.toRS(List.<TmsTestCaseReviewComment>of()));

    verify(userRepository, never()).findAllById(org.mockito.ArgumentMatchers.any());
  }
}
