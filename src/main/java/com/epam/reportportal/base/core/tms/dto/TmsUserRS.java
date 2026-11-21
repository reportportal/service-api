package com.epam.reportportal.base.core.tms.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A person shown next to something they did, e.g. the author of a review comment.
 * <p>
 * Example: {@code {"id": 7, "login": "user1", "fullName": "Ann Smith"}}; {@code fullName} is absent for a user
 * who never set one, the caller then shows the login.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TmsUserRS {

  private Long id;

  private String login;

  private String fullName;
}
