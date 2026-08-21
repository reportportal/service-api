package com.epam.reportportal.base.core.tms.sync.connector.jira;

import com.epam.reportportal.base.core.tms.enums.TmsSyncProvider;
import com.epam.reportportal.base.core.tms.sync.TmsSyncConnector;
import com.epam.reportportal.base.core.tms.sync.dto.RemoteAttachment;
import com.epam.reportportal.base.core.tms.sync.dto.RemoteFolder;
import com.epam.reportportal.base.core.tms.sync.dto.RemoteTestCase;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.Integration;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.springframework.stereotype.Component;

@Component
public class QaSpaceSyncConnector implements TmsSyncConnector<Integration> {

    private static final String URL_PARAM = "url";
    private static final String BEARER_PAT_PARAM = "bearerPat";
    private static final String PROJECT_KEY_PARAM = "projectKey";

    private final ObjectMapper objectMapper;
    private final JiraHttpClient jiraHttpClient;

    public QaSpaceSyncConnector(ObjectMapper objectMapper, JiraHttpClient jiraHttpClient) {
        this.objectMapper = objectMapper;
        this.jiraHttpClient = jiraHttpClient;
    }

    @Override
    public TmsSyncProvider getSupportedProvider() {
        return TmsSyncProvider.QA_SPACE;
    }

    @Override
    public void validateConfig(Integration config) {
        Map<String, Object> params = config.getParams().getParams();
        if (!params.containsKey(URL_PARAM) || !params.containsKey(BEARER_PAT_PARAM) || !params.containsKey(PROJECT_KEY_PARAM)) {
            throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, "Missing required integration parameters for QA Space");
        }
    }

    @Override
    public List<RemoteFolder> fetchFolderTree(Integration config, String rootFolderId) {
        Map<String, Object> params = config.getParams().getParams();
        String url = (String) params.get(URL_PARAM);
        String bearerPat = (String) params.get(BEARER_PAT_PARAM);
        String projectKey = (String) params.get(PROJECT_KEY_PARAM);

        String path = String.format("rest/tm/1.0/folder/list?projectKey=%s&folderId=%s", projectKey, rootFolderId);
        String response = jiraHttpClient.get(url, bearerPat, path);

        List<RemoteFolder> folders = new ArrayList<>();
        try {
            JsonNode rootNode = objectMapper.readTree(response);
            parseFoldersRecursive(rootNode, null, folders);
        } catch (Exception e) {
            throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, "Failed to parse QA Space folder tree", e);
        }

        return folders;
    }

    @Override
    public void fetchTestCases(Integration config, String folderId, Instant since, Consumer<Integer> totalCountConsumer, Consumer<RemoteTestCase> testCaseConsumer) {
        Map<String, Object> params = config.getParams().getParams();
        String url = (String) params.get(URL_PARAM);
        String bearerPat = (String) params.get(BEARER_PAT_PARAM);
        String projectKey = (String) params.get(PROJECT_KEY_PARAM);

        // 1. Fetch folder to get testCaseIds
        String folderPath = String.format("rest/tm/1.0/folder/list?projectKey=%s&folderId=%s", projectKey, folderId);
        String folderResponse = jiraHttpClient.get(url, bearerPat, folderPath);

        List<String> testCaseIds = new ArrayList<>();
        try {
            JsonNode rootNode = objectMapper.readTree(folderResponse);
            if (rootNode.has("testCaseIds")) {
                rootNode.get("testCaseIds").forEach(idNode -> testCaseIds.add(idNode.asText()));
            }
        } catch (Exception e) {
            throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, "Failed to parse QA Space folder test cases", e);
        }

        totalCountConsumer.accept(testCaseIds.size());

        // 2. Fetch details for each test case in batches
        int batchSize = 50;
        for (int i = 0; i < testCaseIds.size(); i += batchSize) {
            List<String> batch = testCaseIds.subList(i, Math.min(i + batchSize, testCaseIds.size()));

            String jql = "issueKey in (" + String.join(",", batch) + ")";

            ObjectNode searchReq = objectMapper.createObjectNode();
            searchReq.put("jql", jql);
            searchReq.putArray("fields")
                .add("summary").add("description").add("updated")
                .add("customfield_19206").add("customfield_19207").add("attachment");
            searchReq.put("maxResults", batchSize);

            String issueResponse = jiraHttpClient.post(url, bearerPat, "rest/api/2/search", searchReq.toString());

            try {
                JsonNode searchNode = objectMapper.readTree(issueResponse);
                if (searchNode.has("issues")) {
                    searchNode.get("issues").forEach(issueNode -> {
                        String testCaseId = issueNode.get("key").asText();
                        JsonNode fields = issueNode.get("fields");

                        Instant updatedAt = Instant.now();
                        if (fields.hasNonNull("updated")) {
                            updatedAt = Instant.from(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ").parse(fields.get("updated").asText()));
                        }

                        if (since != null && updatedAt.isBefore(since)) {
                            return; // Skip if not updated since last sync
                        }

                        List<RemoteAttachment> attachments = new ArrayList<>();
                        if (fields.has("attachment")) {
                            fields.get("attachment").forEach(attNode -> {
                                attachments.add(RemoteAttachment.builder()
                                        .id(attNode.get("id").asText())
                                        .filename(attNode.get("filename").asText())
                                        .mimeType(attNode.get("mimeType").asText())
                                        .size(attNode.get("size").asLong())
                                        .contentUrl(attNode.get("content").asText())
                                        .build());
                            });
                        }

                        RemoteTestCase remoteTestCase = RemoteTestCase.builder()
                                .id(testCaseId)
                                .name(fields.hasNonNull("summary") ? fields.get("summary").asText() : "")
                                .description(fields.hasNonNull("description") ? fields.get("description").asText() : "")
                                .steps(fields.hasNonNull("customfield_19206") ? fields.get("customfield_19206").asText() : "")
                                .expectedResults(fields.hasNonNull("customfield_19207") ? fields.get("customfield_19207").asText() : "")
                                .folderId(folderId)
                                .updatedAt(updatedAt)
                                .attachments(attachments)
                                .build();

                        testCaseConsumer.accept(remoteTestCase);
                    });
                }
            } catch (Exception e) {
                throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, "Failed to parse Jira search response", e);
            }
        }
    }

    @Override
    public InputStream downloadAttachment(Integration config, String contentUrl) {
        String bearerPat = (String) config.getParams().getParams().get(BEARER_PAT_PARAM);
        try {
            return jiraHttpClient.downloadAttachmentStream(bearerPat, contentUrl);
        } catch (IOException e) {
            throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, "Failed to download attachment from Jira", e);
        }
    }

    private void parseFoldersRecursive(JsonNode node, String parentId, List<RemoteFolder> folders) {
        if (node.has("id") && node.has("name")) {
            String id = node.get("id").asText();
            folders.add(RemoteFolder.builder()
                    .id(id)
                    .name(node.get("name").asText())
                    .parentId(parentId)
                    .build());

            if (node.has("children")) {
                node.get("children").forEach(child -> parseFoldersRecursive(child, id, folders));
            }
        }
    }
}
