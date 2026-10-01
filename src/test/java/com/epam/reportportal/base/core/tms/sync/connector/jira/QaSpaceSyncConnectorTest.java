package com.epam.reportportal.base.core.tms.sync.connector.jira;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.core.tms.sync.dto.RemoteFolder;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.Integration;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.IntegrationParams;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class QaSpaceSyncConnectorTest {

    private static final String BASE_URL = "https://jira.example.com";
    private static final String PROJECT_KEY = "EPMRPP";
    private static final String BEARER_PAT = "test-pat";
    private static final String FOLDER_ENDPOINT = "rest/tm/1.0/folder/list?projectKey=EPMRPP&folderId=";

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
        connector = new QaSpaceSyncConnector(jiraHttpClient, new ObjectMapper());
        integration = createIntegration(Map.of(
                "url", BASE_URL,
                "bearerPat", BEARER_PAT,
                "projectKey", PROJECT_KEY,
                "authType", "OAUTH"
        ));
    }

    @Test
    void fetchFolderTree_withRootFolderIdZero_returnsFullTree() {
        when(jiraHttpClient.get(eq(BASE_URL), eq("Bearer " + BEARER_PAT), eq(FOLDER_ENDPOINT + "0"))).thenReturn(JSON_RESPONSE);

        List<RemoteFolder> folders = connector.fetchFolderTree(integration, "0");

        assertEquals(5, folders.size());
        assertEquals("9822", folders.get(0).getId());
        assertNull(folders.get(0).getParentId());
        assertEquals("27082", folders.get(1).getId());
        assertEquals("9822", folders.get(1).getParentId());
    }

    @Test
    void fetchFolderTree_withNullOrBlankFolderId_defaultsToZeroParamAndReturnsFullTree() {
        when(jiraHttpClient.get(eq(BASE_URL), eq("Bearer " + BEARER_PAT), eq(FOLDER_ENDPOINT + "0"))).thenReturn(JSON_RESPONSE);

        List<RemoteFolder> foldersNull = connector.fetchFolderTree(integration, null);
        assertEquals(5, foldersNull.size());

        List<RemoteFolder> foldersBlank = connector.fetchFolderTree(integration, "  ");
        assertEquals(5, foldersBlank.size());
    }

    @Test
    void fetchFolderTree_withSpecificSubfolderId_returnsSubtreeOnly() {
        when(jiraHttpClient.get(eq(BASE_URL), eq("Bearer " + BEARER_PAT), eq(FOLDER_ENDPOINT + "27082"))).thenReturn(JSON_RESPONSE);

        List<RemoteFolder> folders = connector.fetchFolderTree(integration, "27082");

        assertEquals(3, folders.size());

        RemoteFolder rootSubfolder = folders.get(0);
        assertEquals("27082", rootSubfolder.getId());
        assertEquals("Administrate. Projects", rootSubfolder.getName());
        assertNull(rootSubfolder.getParentId());

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
        when(jiraHttpClient.get(eq(BASE_URL), eq("Bearer " + BEARER_PAT), eq(FOLDER_ENDPOINT + "99999"))).thenReturn(JSON_RESPONSE);

        List<RemoteFolder> folders = connector.fetchFolderTree(integration, "99999");

        assertTrue(folders.isEmpty());
    }

    @Test
    void fetchFolderTree_withBasicAuthenticationAndLegacyProject_usesBasicAuthorization() {
        Integration basicIntegration = createIntegration(Map.of(
                "url", BASE_URL,
                "project", PROJECT_KEY,
                "authType", "BASIC",
                "username", "jira-user",
                "password", "jira-password"
        ));
        String expectedAuthorization = "Basic " + Base64.getEncoder()
                .encodeToString("jira-user:jira-password".getBytes(StandardCharsets.UTF_8));

        when(jiraHttpClient.get(eq(BASE_URL), eq(expectedAuthorization), eq(FOLDER_ENDPOINT + "0"))).thenReturn(JSON_RESPONSE);

        List<RemoteFolder> folders = connector.fetchFolderTree(basicIntegration, "0");

        assertEquals(5, folders.size());
        verify(jiraHttpClient).get(eq(BASE_URL), eq(expectedAuthorization), eq(FOLDER_ENDPOINT + "0"));
    }

    @Test
    void validateConfig_withOAuthConfiguration_succeeds() {
        connector.validateConfig(integration);
    }

    @Test
    void validateConfig_withOAuthConfigurationMissingBearerPat_throwsException() {
        Integration oauthIntegrationWithoutBearerPat = createIntegration(Map.of(
                "url", BASE_URL,
                "projectKey", PROJECT_KEY,
                "authType", "OAUTH"
        ));

        assertThrows(ReportPortalException.class, () -> connector.validateConfig(oauthIntegrationWithoutBearerPat));
    }

    @Test
    void validateConfig_withBasicConfigurationMissingPassword_throwsException() {
        Integration basicIntegrationWithoutPassword = createIntegration(Map.of(
                "url", BASE_URL,
                "projectKey", PROJECT_KEY,
                "authType", "BASIC",
                "username", "jira-user"
        ));

        assertThrows(ReportPortalException.class, () -> connector.validateConfig(basicIntegrationWithoutPassword));
    }

    @Test
    void validateConfig_withApiKeyConfiguration_throwsException() {
        Integration apiKeyIntegration = createIntegration(Map.of(
                "url", BASE_URL,
                "projectKey", PROJECT_KEY,
                "authType", "APIKEY",
                "apiKey", "test-api-key"
        ));

        assertThrows(ReportPortalException.class, () -> connector.validateConfig(apiKeyIntegration));
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

        when(jiraHttpClient.post(eq(BASE_URL), eq("Bearer " + BEARER_PAT), eq("rest/api/2/search"), anyString()))
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

    private Integration createIntegration(Map<String, Object> paramsMap) {
        Integration testIntegration = new Integration();
        IntegrationParams params = new IntegrationParams();
        params.setParams(paramsMap);
        testIntegration.setParams(params);
        return testIntegration;
    }
}
