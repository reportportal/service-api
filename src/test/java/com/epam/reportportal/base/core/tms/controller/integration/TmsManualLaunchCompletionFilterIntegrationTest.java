package com.epam.reportportal.base.core.tms.controller.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.reportportal.base.ws.BaseMvcTest;
import com.jayway.jsonpath.JsonPath;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;

/**
 * "Launch completion" filter of the manual launches list ({@code filter.eq.completion}). To Run
 * and In Progress executions mean "not executed"; Passed, Failed and Skipped mean executed.
 */
@Sql({"/db/tms/tms-manual-launch/tms-manual-launch-fill.sql",
    "/db/tms/tms-manual-launch/tms-manual-launch-completion-fill.sql"})
class TmsManualLaunchCompletionFilterIntegrationTest extends BaseMvcTest {

  private static final String SUPERADMIN_PROJECT_KEY = "superadmin_personal";

  @Test
  void hasNotExecuted_ShouldReturnLaunchesWithToRunOrInProgressExecutions() throws Exception {
    // 202: only In Progress, 300: To Run + Passed, 301: To Run + In Progress,
    // 303: In Progress + Skipped, 304: To Run + Skipped + Failed
    assertThat(findLaunchIds("has_not_executed"))
        .containsExactlyInAnyOrder(202, 300, 301, 303, 304);
  }

  @Test
  void done_ShouldReturnLaunchesWithoutToRunAndInProgressExecutions() throws Exception {
    // 100, 101, 102, 200: Passed / Failed, 201 and 203: with Skipped, 302: only Skipped.
    // 305 (no executions) is in neither list.
    assertThat(findLaunchIds("done"))
        .containsExactlyInAnyOrder(100, 101, 102, 200, 201, 203, 302);
  }

  @Test
  void completionValue_ShouldBeCaseInsensitive() throws Exception {
    assertThat(findLaunchIds("HAS_NOT_EXECUTED"))
        .containsExactlyInAnyOrder(202, 300, 301, 303, 304);
    assertThat(findLaunchIds("DONE"))
        .containsExactlyInAnyOrder(100, 101, 102, 200, 201, 203, 302);
  }

  private List<Integer> findLaunchIds(String completion) throws Exception {
    var response = mockMvc.perform(
            get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/launch/manual")
                .param("filter.eq.completion", completion)
                .param("page.size", "100")
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();
    return JsonPath.read(response, "$.content[*].id");
  }
}
