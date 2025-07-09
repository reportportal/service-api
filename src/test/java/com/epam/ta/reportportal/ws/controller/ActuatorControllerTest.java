package com.epam.ta.reportportal.ws.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.ta.reportportal.ws.BaseMvcTest;
import org.junit.jupiter.api.Test;

public class ActuatorControllerTest extends BaseMvcTest {

  @Test
  void getPrometheusMetrics() throws Exception {
    mockMvc.perform(get("/prometheus")
            .with(token(oAuthHelper.getDefaultToken())))
        .andExpect(status().isOk());
  }
}
