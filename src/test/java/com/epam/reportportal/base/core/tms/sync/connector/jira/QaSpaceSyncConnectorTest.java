package com.epam.reportportal.base.core.tms.sync.connector.jira;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.core.tms.sync.dto.RemoteFolder;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.Integration;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.IntegrationParams;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class QaSpaceSyncConnectorTest {

    @Mock
    private JiraHttpClient jiraHttpClient;

    private QaSpaceSyncConnector connector;
    private Integration integration;

    private static final String JSON_RESPONSE = """
            {
              "id": 9822,
              "name": "EPMRPP",
              "children": [
                {
                  "id": 27082,
                  "name": "Administrate. Projects",
                  "children": [
                    {
                      "id": 54972,
                      "name": "Projects page",
                      "children": [],
                      "testCaseIds": [1093566, 1093567]
                    },
                    {
                      "id": 27084,
                      "name": "Add Project",
                      "children": [],
                      "testCaseIds": [1096521]
                    }
                  ]
                },
                {
                  "id": 30000,
                  "name": "Other Folder",
                  "children": []
                }
              ]
            }
            """;

    @BeforeEach
    void setUp() {
        var objectMapper = new ObjectMapper();
        connector = new QaSpaceSyncConnector(jiraHttpClient, objectMapper);

        integration = new Integration();
        var params = new IntegrationParams();
        params.setParams(Map.of(
                "url", "https://jira.example.com",
                "bearerPat", "test-pat",
                "projectKey", "EPMRPP"
        ));
        integration.setParams(params);
    }

    @Test
    void fetchFolderTree_withRootFolderIdZero_returnsFullTree() {
        when(jiraHttpClient.get(eq("https://jira.example.com"), eq("test-pat"), eq("rest/tm/1.0/folder/list?projectKey=EPMRPP&folderId=0")))
                .thenReturn(JSON_RESPONSE);

        List<RemoteFolder> folders = connector.fetchFolderTree(integration, "0");

        assertEquals(5, folders.size());
        assertEquals("9822", folders.get(0).getId());
        assertNull(folders.get(0).getParentId());
        assertEquals("27082", folders.get(1).getId());
        assertEquals("9822", folders.get(1).getParentId());
    }

    @Test
    void fetchFolderTree_withNullOrBlankFolderId_defaultsToZeroParamAndReturnsFullTree() {
        when(jiraHttpClient.get(eq("https://jira.example.com"), eq("test-pat"), eq("rest/tm/1.0/folder/list?projectKey=EPMRPP&folderId=0")))
                .thenReturn(JSON_RESPONSE);

        List<RemoteFolder> foldersNull = connector.fetchFolderTree(integration, null);
        assertEquals(5, foldersNull.size());

        List<RemoteFolder> foldersBlank = connector.fetchFolderTree(integration, "  ");
        assertEquals(5, foldersBlank.size());
    }

    @Test
    void fetchFolderTree_withSpecificSubfolderId_returnsSubtreeOnly() {
        when(jiraHttpClient.get(eq("https://jira.example.com"), eq("test-pat"), eq("rest/tm/1.0/folder/list?projectKey=EPMRPP&folderId=27082")))
                .thenReturn(JSON_RESPONSE);

        List<RemoteFolder> folders = connector.fetchFolderTree(integration, "27082");

        assertEquals(3, folders.size());

        // Root of requested subtree
        RemoteFolder rootSubfolder = folders.get(0);
        assertEquals("27082", rootSubfolder.getId());
        assertEquals("Administrate. Projects", rootSubfolder.getName());
        assertNull(rootSubfolder.getParentId());

        // Children of subtree root
        RemoteFolder child1 = folders.get(1);
        assertEquals("54972", child1.getId());
        assertEquals("27082", child1.getParentId());
        assertEquals(List.of("1093566", "1093567"), child1.getTestCaseIds());

        RemoteFolder child2 = folders.get(2);
        assertEquals("27084", child2.getId());
        assertEquals("27082", child2.getParentId());
        assertEquals(List.of("1096521"), child2.getTestCaseIds());
    }

    @Test
    void fetchFolderTree_withNonExistentFolderId_returnsEmptyList() {
        when(jiraHttpClient.get(eq("https://jira.example.com"), eq("test-pat"), eq("rest/tm/1.0/folder/list?projectKey=EPMRPP&folderId=99999")))
                .thenReturn(JSON_RESPONSE);

        List<RemoteFolder> folders = connector.fetchFolderTree(integration, "99999");

        assertTrue(folders.isEmpty());
    }

    @Test
    void fetchTestCases_withRequirements_parsesCorrectlyUsingBaseUrl() {
        var searchResponse = """
                {
                  "issues": [
                    {
                      "key": "EPMRPP-101",
                      "fields": {
                        "summary": "Sample Test Case",
                        "description": "Sample Description",
                        "priority": { "name": "High" },
                        "updated": "2024-01-15T10:30:00.000+0000",
                        "labels": ["smoke", "regression"],
                        "customfield_19206": "Step 1: Do something",
                        "customfield_19207": "Expected: Result",
                        "customfield_29300": [
                          "Requirement(key=REQ-123, external=false)",
                          "Requirement(key=https://external.example.com/req/456, external=true)",
                          "Plain-Requirement-Key"
                        ]
                      }
                    }
                  ]
                }
                """;

        var folder = RemoteFolder.builder()
                .id("27082")
                .testCaseIds(List.of("EPMRPP-101"))
                .build();

        when(jiraHttpClient.post(eq("https://jira.example.com"), eq("test-pat"), eq("rest/api/2/search"), anyString()))
                .thenReturn(searchResponse);

        var result = connector.fetchTestCases(integration, folder, null, 0, 50);

        assertEquals(1, result.getTotalCount());
        assertEquals(1, result.getTestCases().size());

        var tc = result.getTestCases().get(0);
        assertEquals("EPMRPP-101", tc.getId());
        assertEquals("Sample Test Case", tc.getName());
        assertEquals("High", tc.getPriority());
        assertEquals(List.of("smoke", "regression"), tc.getLabels());
        assertEquals(List.of(
                "https://jira.example.com/browse/REQ-123",
                "https://external.example.com/req/456",
                "Plain-Requirement-Key"
        ), tc.getRequirements());
        assertEquals("Step 1: Do something", tc.getSteps());
        assertEquals("Expected: Result", tc.getExpectedResults());
    }
}