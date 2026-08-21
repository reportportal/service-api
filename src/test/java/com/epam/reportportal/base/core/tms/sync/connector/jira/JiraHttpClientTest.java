package com.epam.reportportal.base.core.tms.sync.connector.jira;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class JiraHttpClientTest {

  @Test
  void shouldInstantiateJiraHttpClient() {
    JiraHttpClient client = new JiraHttpClient();
    assertNotNull(client);
  }
}
