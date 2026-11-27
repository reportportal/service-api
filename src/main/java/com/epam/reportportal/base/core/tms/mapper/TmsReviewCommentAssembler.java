package com.epam.reportportal.base.core.tms.mapper;

import com.epam.reportportal.base.core.tms.dto.TmsReviewCommentRS;
import com.epam.reportportal.base.core.tms.dto.TmsUserRS;
import com.epam.reportportal.base.infrastructure.persistence.dao.UserRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCaseReviewComment;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.User;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Turns review comments into responses that carry their author, so a client shows a name and does not have to look the
 * user up from {@code authorId}.
 * <p>
 * Example: a comment written by user 7 comes back with {@code authorId: 7} and
 * {@code author: {id: 7, login: "user1", fullName: "Ann Smith"}}. Authors are read with one query for the whole list.
 * A user that no longer exists leaves {@code author} out.
 */
@Component
@RequiredArgsConstructor
public class TmsReviewCommentAssembler {

  private final UserRepository userRepository;

  /**
   * Responses for the comments, in the same order, with their authors.
   */
  public List<TmsReviewCommentRS> toRS(Collection<TmsTestCaseReviewComment> comments) {
    if (comments.isEmpty()) {
      return List.of();
    }
    Set<Long> authorIds = comments.stream().map(TmsTestCaseReviewComment::getAuthorId).collect(Collectors.toSet());
    Map<Long, User> authors = userRepository.findAllById(authorIds).stream()
        .collect(Collectors.toMap(User::getId, Function.identity()));
    return comments.stream().map(comment -> toRS(comment, authors.get(comment.getAuthorId()))).toList();
  }

  /**
   * Response for one comment with its author.
   */
  public TmsReviewCommentRS toRS(TmsTestCaseReviewComment comment) {
    return toRS(List.of(comment)).get(0);
  }

  private static TmsReviewCommentRS toRS(TmsTestCaseReviewComment comment, User author) {
    var response = TmsReviewCommentMapper.toRS(comment);
    if (author != null) {
      response.setAuthor(TmsUserRS.builder()
          .id(author.getId())
          .login(author.getLogin())
          .fullName(author.getFullName())
          .build());
    }
    return response;
  }
}
