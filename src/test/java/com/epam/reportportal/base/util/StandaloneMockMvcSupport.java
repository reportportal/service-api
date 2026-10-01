package com.epam.reportportal.base.util;

import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder;

/**
 * Entry point for standalone {@code MockMvc} setup in controller unit tests.
 *
 * <p>{@code MockMvcBuilders.standaloneSetup(...)} auto-detects every
 * {@code HttpMessageConverter} implementation on the classpath, including XML converters pulled
 * in transitively (e.g. by JasperReports). Without an explicit {@code Accept} header, content
 * negotiation can then resolve to XML instead of JSON, breaking {@code jsonPath} assertions.
 * Use this instead of {@code standaloneSetup(...)} directly to pin the converters to JSON and
 * binary payloads only.
 */
public class StandaloneMockMvcSupport {

  /**
   * Builds a standalone {@code MockMvc} for the given controllers, restricted to JSON and binary
   * message converters so response content negotiation is deterministic.
   */
  public static StandaloneMockMvcBuilder standaloneJsonSetup(Object... controllers) {
    return MockMvcBuilders.standaloneSetup(controllers)
        .setMessageConverters(
            new MappingJackson2HttpMessageConverter(),
            new ResourceHttpMessageConverter(),
            new ByteArrayHttpMessageConverter());
  }

}
