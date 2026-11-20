package com.epam.reportportal.base.core.tms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Sends Ready Test Cases to automation; one case and a bulk selection are the same request.
 * <p>
 * Example: {@code {"testCaseIds": [42, 43], "environment": "beta5", "confirmReautomate": false}}. When 42 is
 * already automated the first answer is 409 {@code ALREADY_AUTOMATED_CONFIRM_REQUIRED} with {@code testCaseIds:
 * [42]}; the user confirms and the request is sent again with {@code "confirmReautomate": true,
 * "confirmedTestCaseIds": [42]}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TmsAutomationStartRQ {

  @NotNull
  @NotEmpty
  private List<Long> testCaseIds;

  @NotBlank
  private String environment;

  private boolean confirmReautomate;

  /**
   * The already automated cases the user saw in the confirmation; with {@code confirmReautomate} it covers
   * exactly them, a case that needs confirmation and is not listed stops the request again.
   */
  private List<Long> confirmedTestCaseIds;
}
