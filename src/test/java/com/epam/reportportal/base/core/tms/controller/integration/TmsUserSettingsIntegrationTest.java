package com.epam.reportportal.base.core.tms.controller.integration;

import static org.hamcrest.Matchers.anEmptyMap;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.reportportal.base.core.tms.dto.TmsUserSettingsRQ;
import com.epam.reportportal.base.ws.BaseMvcTest;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

@Sql({"/db/organization/full_organization_samples.sql",
    "/db/tms/tms-user-settings/tms-user-settings-fill.sql"})
class TmsUserSettingsIntegrationTest extends BaseMvcTest {

  private static final String PROJECT_1 = "internal-org1.test-project-1";
  private static final String PROJECT_2 = "internal-org2.test-project-2";

  private static String url(String projectKey) {
    return "/v1/project/" + projectKey + "/tms/user-settings";
  }

  @Test
  void getUserSettings_WhenNothingStored_ShouldReturnEmptySettings() throws Exception {
    mockMvc.perform(get(url(PROJECT_1)).with(token(editorToken)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.settings", anEmptyMap()));
  }

  @Test
  void updateUserSettings_ShouldMergeKeysAndRemoveNullValues() throws Exception {
    mockMvc.perform(put(url(PROJECT_1)).with(token(editorToken))
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"settings": {"testCaseListPageSize": 50,
                  "testCaseListSorting": {"field": "name", "direction": "ASC"}}}"""))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.settings.testCaseListPageSize").value(50))
        .andExpect(jsonPath("$.settings.testCaseListSorting.field").value("name"));

    mockMvc.perform(put(url(PROJECT_1)).with(token(editorToken))
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"settings": {"testCaseListPageSize": 100, "testCaseListSorting": null,
                  "tmsFolderTreeWidth": 480}}"""))
        .andExpect(status().isOk());

    mockMvc.perform(get(url(PROJECT_1)).with(token(editorToken)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.settings.testCaseListPageSize").value(100))
        .andExpect(jsonPath("$.settings.tmsFolderTreeWidth").value(480))
        .andExpect(jsonPath("$.settings.testCaseListSorting").doesNotExist());
  }

  @Test
  void updateUserSettings_ShouldNotAffectAnotherProject() throws Exception {
    mockMvc.perform(put(url(PROJECT_1)).with(token(managerToken))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"settings\": {\"testCaseListPageSize\": 50}}"))
        .andExpect(status().isOk());

    mockMvc.perform(get(url(PROJECT_2)).with(token(managerToken)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.settings", anEmptyMap()));
  }

  @Test
  void updateUserSettings_ShouldNotAffectAnotherUserAndBeAllowedForViewer() throws Exception {
    mockMvc.perform(put(url(PROJECT_1)).with(token(editorToken))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"settings\": {\"testCaseListPageSize\": 50}}"))
        .andExpect(status().isOk());

    mockMvc.perform(get(url(PROJECT_1)).with(token(viewerToken)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.settings", anEmptyMap()));

    mockMvc.perform(put(url(PROJECT_1)).with(token(viewerToken))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"settings\": {\"testCaseListPageSize\": 20}}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.settings.testCaseListPageSize").value(20));
  }

  @Test
  void updateUserSettings_WhenSettingsMissing_ShouldReturnBadRequest() throws Exception {
    mockMvc.perform(put(url(PROJECT_1)).with(token(editorToken))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void updateUserSettings_WhenTooManyKeys_ShouldReturnBadRequest() throws Exception {
    var settings = IntStream.rangeClosed(1, TmsUserSettingsRQ.MAX_KEYS_PER_REQUEST + 1)
        .mapToObj(i -> "\"key" + i + "\": " + i)
        .collect(Collectors.joining(", ", "{\"settings\": {", "}}"));

    mockMvc.perform(put(url(PROJECT_1)).with(token(editorToken))
            .contentType(MediaType.APPLICATION_JSON)
            .content(settings))
        .andExpect(status().isBadRequest());
  }

  @Test
  void updateUserSettings_WhenKeyTooLong_ShouldReturnBadRequest() throws Exception {
    var key = "k".repeat(TmsUserSettingsRQ.MAX_KEY_LENGTH + 1);

    mockMvc.perform(put(url(PROJECT_1)).with(token(editorToken))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"settings\": {\"" + key + "\": 1}}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void updateUserSettings_WhenKeyBlank_ShouldReturnBadRequest() throws Exception {
    mockMvc.perform(put(url(PROJECT_1)).with(token(editorToken))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"settings\": {\" \": 1}}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void updateUserSettings_WhenValueTooLong_ShouldReturnBadRequest() throws Exception {
    var value = "v".repeat(4097);

    mockMvc.perform(put(url(PROJECT_1)).with(token(editorToken))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"settings\": {\"key\": \"" + value + "\"}}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void getUserSettings_WhenUserHasNoAccessToProject_ShouldReturnForbidden() throws Exception {
    mockMvc.perform(get(url(PROJECT_2)).with(token(editorToken)))
        .andExpect(status().isForbidden());
  }
}
