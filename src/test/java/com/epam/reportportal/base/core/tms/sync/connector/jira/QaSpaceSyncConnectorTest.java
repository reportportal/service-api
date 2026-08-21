package com.epam.reportportal.base.core.tms.sync.connector.jira;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.core.tms.enums.TmsSyncProvider;
import com.epam.reportportal.base.core.tms.sync.dto.RemoteFolder;
import com.epam.reportportal.base.core.tms.sync.dto.RemoteTestCase;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.Integration;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.IntegrationParams;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class QaSpaceSyncConnectorTest {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Mock
  private JiraHttpClient jiraHttpClient;

  private QaSpaceSyncConnector connector;

  @BeforeEach
  void setUp() {
    connector = new QaSpaceSyncConnector(objectMapper, jiraHttpClient);
  }

  @Test
  void shouldReturnQaSpaceProvider() {
    assertEquals(TmsSyncProvider.QA_SPACE, connector.getSupportedProvider());
  }

  @Test
  void shouldValidateConfigSuccessfully() {
    Integration integration = createIntegration("http://jira.local", "token123", "PRJ");
    connector.validateConfig(integration);
  }

  @Test
  void shouldThrowWhenConfigIsMissingParams() {
    Integration integration = new Integration();
    integration.setParams(new IntegrationParams(new HashMap<>()));

    assertThrows(ReportPortalException.class, () -> connector.validateConfig(integration));
  }

  @Test
  void shouldFetchFolderTree() {
    Integration integration = createIntegration("http://jira.local", "token123", "PRJ");
    String jsonResponse = """
        {
          "id": "1",
          "name": "Root Folder",
          "children": [
            {
              "id": "2",
              "name": "Child Folder"
            }
          ]
        }
        """;

    when(jiraHttpClient.get("http://jira.local", "token123", "rest/tm/1.0/folder/list?projectKey=PRJ&folderId=0"))
        .thenReturn(jsonResponse);

    List<RemoteFolder> folders = connector.fetchFolderTree(integration, "0");

    assertNotNull(folders);
    assertEquals(2, folders.size());
    assertEquals("1", folders.get(0).getId());
    assertEquals("Root Folder", folders.get(0).getName());
    assertEquals("2", folders.get(1).getId());
    assertEquals("Child Folder", folders.get(1).getName());
  }

  @Test
  void shouldFetchTestCases() {
    Integration integration = createIntegration("http://jira.local", "token123", "PRJ");
    String folderResponse = """
        {
          "id": "10",
          "name": "Folder 10",
          "testCaseIds": ["PRJ-1", "PRJ-2"]
        }
        """;

    when(jiraHttpClient.get("http://jira.local", "token123", "rest/tm/1.0/folder/list?projectKey=PRJ&folderId=10"))
        .thenReturn(folderResponse);

    String searchResponse = """
        {
          "issues": [
            {
              "key": "PRJ-1",
              "fields": {
                "summary": "TC 1",
                "description": "Desc 1",
                "updated": "2025-01-01T12:00:00.000+0000",
                "customfield_19206": "*Steps*: Do test",
                "customfield_19207": "Pass",
                "attachment": [
                  {
                    "id": "100",
                    "filename": "screenshot.png",
                    "mimeType": "image/png",
                    "size": 2048,
                    "content": "http://jira.local/attachments/100"
                  }
                ]
              }
            }
          ]
        }
        """;

    when(jiraHttpClient.post(eq("http://jira.local"), eq("token123"), eq("rest/api/2/search"), anyString()))
        .thenReturn(searchResponse);

    AtomicInteger totalCount = new AtomicInteger();
    List<RemoteTestCase> fetchedTestCases = new ArrayList<>();

    connector.fetchTestCases(
        integration,
        "10",
        null,
        totalCount::set,
        fetchedTestCases::add
    );

    assertEquals(2, totalCount.get());
    assertEquals(1, fetchedTestCases.size());
    RemoteTestCase tc = fetchedTestCases.get(0);
    assertEquals("PRJ-1", tc.getId());
    assertEquals("TC 1", tc.getName());
    assertEquals("Desc 1", tc.getDescription());
    assertEquals(1, tc.getAttachments().size());
    assertEquals("100", tc.getAttachments().get(0).getId());
    assertEquals("screenshot.png", tc.getAttachments().get(0).getFilename());
  }

  @Test
  void shouldDownloadAttachment() throws Exception {
    Integration integration = createIntegration("http://jira.local", "token123", "PRJ");
    InputStream stream = new ByteArrayInputStream("content".getBytes());

    when(jiraHttpClient.downloadAttachmentStream("token123", "http://jira.local/att/1"))
        .thenReturn(stream);

    InputStream result = connector.downloadAttachment(integration, "http://jira.local/att/1");

    assertNotNull(result);
    verify(jiraHttpClient).downloadAttachmentStream("token123", "http://jira.local/att/1");
  }

  private Integration createIntegration(String url, String bearerPat, String projectKey) {
    Integration integration = new Integration();
    Map<String, Object> params = new HashMap<>();
    params.put("url", url);
    params.put("bearerPat", bearerPat);
    params.put("projectKey", projectKey);
    integration.setParams(new IntegrationParams(params));
    return integration;
  }
}
