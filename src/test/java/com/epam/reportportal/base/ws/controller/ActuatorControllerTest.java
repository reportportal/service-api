package com.epam.reportportal.base.ws.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.reportportal.base.core.plugin.Plugin;
import com.epam.reportportal.base.infrastructure.persistence.dao.ServerSettingsRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.ServerSettings;
import com.epam.reportportal.base.ws.BaseMvcTest;
import com.epam.reportportal.extension.common.ExtensionPoint;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class ActuatorControllerTest extends BaseMvcTest {

  private static final String PRIVATE_SETTING = "server.info.test.private";
  private static final String PASSWORD_MIN_LENGTH = "server.password.min.length";
  private static final String PLUGIN_ID = "info-test-plugin";

  @Autowired
  private ServerSettingsRepository serverSettingsRepository;

  @BeforeEach
  void setUpInfo() {
    serverSettingsRepository.saveAndFlush(new ServerSettings(PRIVATE_SETTING, "secret"));
    if (serverSettingsRepository.findByKey(PASSWORD_MIN_LENGTH).isEmpty()) {
      serverSettingsRepository.saveAndFlush(new ServerSettings(PASSWORD_MIN_LENGTH, "12"));
    }
    when(pluginBox.getPlugins()).thenReturn(List.of(new Plugin(PLUGIN_ID, ExtensionPoint.BTS)));
  }

  @Test
  void getPrometheusMetrics() throws Exception {
    mockMvc.perform(get("/prometheus")
            .with(token(oAuthHelper.getDefaultToken())))
        .andExpect(status().isOk());
  }

  @Test
  void anonymousInfoContainsOnlyPreLoginSubset() throws Exception {
    mockMvc.perform(get("/info"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.build.name").value("API Service"))
        .andExpect(jsonPath("$.build.version").exists())
        .andExpect(jsonPath("$.build.branch").doesNotExist())
        .andExpect(jsonPath("$.build.repo").doesNotExist())
        .andExpect(jsonPath("$.build.group").doesNotExist())
        .andExpect(jsonPath("$.build.description").doesNotExist())
        .andExpect(jsonPath("$.extensions.result['" + PASSWORD_MIN_LENGTH + "']").exists())
        .andExpect(jsonPath("$.extensions.result['" + PRIVATE_SETTING + "']").doesNotExist())
        .andExpect(jsonPath("$.extensions.extension").doesNotExist())
        .andExpect(jsonPath("$.extensions.analyzers").doesNotExist())
        .andExpect(jsonPath("$.metadata").doesNotExist())
        .andExpect(jsonPath("$.jobs").doesNotExist())
        .andExpect(jsonPath("$.jobsInfo").doesNotExist());
  }

  @Test
  void authenticatedInfoContainsEverything() throws Exception {
    mockMvc.perform(get("/info").with(token(oAuthHelper.getDefaultToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.build.name").value("API Service"))
        .andExpect(jsonPath("$.build.repo").exists())
        .andExpect(jsonPath("$.extensions.result['" + PASSWORD_MIN_LENGTH + "']").exists())
        .andExpect(jsonPath("$.extensions.result['" + PRIVATE_SETTING + "']").value("secret"))
        .andExpect(jsonPath("$.extensions.extension[0]").value(PLUGIN_ID))
        .andExpect(jsonPath("$.extensions.analyzers").exists())
        .andExpect(jsonPath("$.metadata").exists())
        .andExpect(jsonPath("$.environment").exists());
  }

  @Test
  void infoWithInvalidTokenIsRejected() throws Exception {
    // the bearer token is processed on this permit-all path: a token that does not verify is refused rather than
    // silently downgraded to the anonymous view
    mockMvc.perform(get("/info").with(token("not-a-valid-token")))
        .andExpect(status().isUnauthorized());
  }
}
