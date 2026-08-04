package com.epam.reportportal.base.core.tms.sync.connector.jira;

import com.epam.reportportal.base.core.tms.enums.TmsSyncProvider;
import com.epam.reportportal.base.core.tms.sync.TmsSyncConnector;
import com.epam.reportportal.base.core.tms.sync.dto.FetchTestCasesResult;
import com.epam.reportportal.base.core.tms.sync.dto.RemoteAttachment;
import com.epam.reportportal.base.core.tms.sync.dto.RemoteFolder;
import com.epam.reportportal.base.core.tms.sync.dto.RemoteTestCase;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.AuthType;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.Integration;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QaSpaceSyncConnector implements TmsSyncConnector<Integration> {

    private static final String URL_PARAM = "url";
    private static final String AUTH_TYPE_PARAM = "authType";
    private static final String USERNAME_PARAM = "username";
    private static final String PASSWORD_PARAM = "password";
    private static final String BEARER_PAT_PARAM = "bearerPat";
    private static final String PROJECT_KEY_PARAM = "projectKey";
    private static final String LEGACY_PROJECT_PARAM = "project";
    private static final Pattern REQUIREMENT_PATTERN = Pattern.compile("Requirement\\(key=(.+?),\\s*external=(true|false)\\)");

    private final JiraHttpClient jiraHttpClient;
    private final ObjectMapper objectMapper;

    @Override
    public TmsSyncProvider getSupportedProvider() {
        return TmsSyncProvider.QA_SPACE;
    }

    @Override
    public void validateConfig(Integration config) {
        requiredParam(config, URL_PARAM);
        projectKey(config);
        authorizationHeader(config);
    }

    @Override
    public List<RemoteFolder> fetchFolderTree(Integration config, String rootFolderId) {
        var url = requiredParam(config, URL_PARAM);
        var projectKey = projectKey(config);

        var folderIdParam = (rootFolderId != null && !rootFolderId.isBlank()) ? rootFolderId : "0";
        var path = String.format("rest/tm/1.0/folder/list?projectKey=%s&folderId=%s", projectKey, folderIdParam);
        var response = jiraHttpClient.get(url, authorizationHeader(config), path);

        var folders = new ArrayList<RemoteFolder>();
        try {
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode targetNode;
            if (rootFolderId == null || rootFolderId.isBlank() || "0".equals(rootFolderId)) {
                targetNode = rootNode;
            } else {
                targetNode = findFolderById(rootNode, rootFolderId);
            }
            if (targetNode != null) {
                parseFoldersRecursive(targetNode, null, folders);
            }
        } catch (Exception e) {
            throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, "Failed to parse QA Space folder tree", e);
        }

        return folders;
    }

    @Override
    public FetchTestCasesResult fetchTestCases(Integration config, RemoteFolder folder, Instant since, int offset, int limit) {
        var url = requiredParam(config, URL_PARAM);

        var testCaseIds = folder != null && folder.getTestCaseIds() != null
                ? folder.getTestCaseIds()
                : List.<String>of();

        var totalCount = testCaseIds.size();
        var fromIndex = Math.min(offset, totalCount);
        var toIndex = Math.min(fromIndex + limit, totalCount);
        var batch = testCaseIds.subList(fromIndex, toIndex);
        var hasMore = toIndex < totalCount;

        var testCases = new ArrayList<RemoteTestCase>();
        if (!batch.isEmpty()) {
            var jql = "issueKey in (" + String.join(",", batch) + ")";

            var searchReq = objectMapper.createObjectNode();
            searchReq.put("jql", jql);
            searchReq.putArray("fields")
                    .add("summary").add("description").add("updated")
                    .add("priority").add("labels").add("components")
                    .add("customfield_19206").add("customfield_19207").add("customfield_29300").add("attachment");
            searchReq.put("maxResults", batch.size());

            var issueResponse = jiraHttpClient.post(url, authorizationHeader(config), "rest/api/2/search", searchReq.toString());

            try {
                var searchNode = objectMapper.readTree(issueResponse);
                if (searchNode.has("issues")) {
                    searchNode.get("issues").forEach(issueNode -> {
                        var testCaseId = issueNode.get("key").asText();
                        var fields = issueNode.get("fields");

                        var updatedAt = Instant.now();
                        if (fields.hasNonNull("updated")) {
                            updatedAt = Instant.from(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ").parse(fields.get("updated").asText()));
                        }

                        if (since != null && updatedAt.isBefore(since)) {
                            return;
                        }

                        var attachments = new ArrayList<RemoteAttachment>();
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

                        var labels = new ArrayList<String>();
                        if (fields.has("labels") && fields.get("labels").isArray()) {
                            fields.get("labels").forEach(labelNode -> labels.add(labelNode.asText()));
                        }

                        var requirements = new ArrayList<String>();
                        if (fields.has("customfield_29300") && fields.get("customfield_29300").isArray()) {
                            fields.get("customfield_29300").forEach(reqNode -> {
                                var rawText = reqNode.asText();
                                var matcher = REQUIREMENT_PATTERN.matcher(rawText);
                                if (matcher.find()) {
                                    var key = matcher.group(1);
                                    var external = Boolean.parseBoolean(matcher.group(2));
                                    if (external) {
                                        requirements.add(key);
                                    } else {
                                        var baseUrl = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
                                        requirements.add(baseUrl + "/browse/" + key);
                                    }
                                } else {
                                    requirements.add(rawText);
                                }
                            });
                        }

                        var remoteTestCase = RemoteTestCase.builder()
                                .id(testCaseId)
                                .name(fields.hasNonNull("summary") ? fields.get("summary").asText() : "")
                                .description(fields.hasNonNull("description") ? fields.get("description").asText() : "")
                                .priority(fields.hasNonNull("priority") ? fields.get("priority").get("name").asText() : "")
                                .labels(labels)
                                .requirements(requirements)
                                .steps(fields.hasNonNull("customfield_19206") ? fields.get("customfield_19206").asText() : "")
                                .expectedResults(fields.hasNonNull("customfield_19207") ? fields.get("customfield_19207").asText() : "")
                                .folderId(folder != null ? folder.getId() : null)
                                .updatedAt(updatedAt)
                                .attachments(attachments)
                                .build();

                        testCases.add(remoteTestCase);
                    });
                }
            } catch (Exception e) {
                throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, "Failed to parse Jira search response", e);
            }
        }

        return FetchTestCasesResult.builder()
                .totalCount(testCases.size())
                .testCases(testCases)
                .hasMore(hasMore)
                .build();
    }

    @Override
    public InputStream downloadAttachment(Integration config, String contentUrl) {
        try {
            return jiraHttpClient.downloadAttachmentStream(authorizationHeader(config), contentUrl);
        } catch (IOException e) {
            throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, "Failed to download attachment from Jira", e);
        }
    }

    private String authorizationHeader(Integration config) {
        var authType = authType(config);
        if (authType == AuthType.BASIC) {
            var username = requiredParam(config, USERNAME_PARAM);
            var password = requiredParam(config, PASSWORD_PARAM);
            var credentials = username + ":" + password;
            return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        }
        if (authType == AuthType.OAUTH) {
            return "Bearer " + requiredParam(config, BEARER_PAT_PARAM);
        }
        throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, "Unsupported authentication type");
    }

    private AuthType authType(Integration config) {
        var authTypeName = requiredParam(config, AUTH_TYPE_PARAM);
        return AuthType.findByName(authTypeName.toUpperCase(Locale.ROOT))
                .filter(authType -> authType == AuthType.BASIC || authType == AuthType.OAUTH)
                .orElseThrow(() -> new ReportPortalException(
                        ErrorType.BAD_REQUEST_ERROR, "Unsupported authentication type: " + authTypeName
                ));
    }

    private String projectKey(Integration config) {
        var projectKey = stringParam(config, PROJECT_KEY_PARAM);
        if (!isBlank(projectKey)) {
            return projectKey;
        }

        var legacyProject = stringParam(config, LEGACY_PROJECT_PARAM);
        if (!isBlank(legacyProject)) {
            return legacyProject;
        }

        throw new ReportPortalException(
                ErrorType.BAD_REQUEST_ERROR,
                "Missing required integration parameter: " + PROJECT_KEY_PARAM + " or " + LEGACY_PROJECT_PARAM
        );
    }

    private String requiredParam(Integration config, String paramName) {
        var value = stringParam(config, paramName);
        if (isBlank(value)) {
            throw new ReportPortalException(
                    ErrorType.BAD_REQUEST_ERROR, "Missing required integration parameter: " + paramName
            );
        }
        return value;
    }

    private String stringParam(Integration config, String paramName) {
        if (config == null || config.getParams() == null || config.getParams().getParams() == null) {
            return null;
        }
        var value = config.getParams().getParams().get(paramName);
        return value instanceof String stringValue ? stringValue : null;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private JsonNode findFolderById(JsonNode node, String targetId) {
        if (node == null || !node.isObject()) {
            return null;
        }
        if (node.has("id") && node.get("id").asText().equals(targetId)) {
            return node;
        }
        if (node.has("children") && node.get("children").isArray()) {
            for (JsonNode child : node.get("children")) {
                JsonNode found = findFolderById(child, targetId);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private void parseFoldersRecursive(JsonNode node, String parentId, List<RemoteFolder> folders) {
        if (node != null && node.has("id") && node.has("name")) {
            var id = node.get("id").asText();
            var testCaseIds = new ArrayList<String>();
            if (node.has("testCaseIds") && node.get("testCaseIds").isArray()) {
                node.get("testCaseIds").forEach(idNode -> testCaseIds.add(idNode.asText()));
            }
            folders.add(RemoteFolder.builder()
                    .id(id)
                    .name(node.get("name").asText())
                    .parentId(parentId)
                    .testCaseIds(testCaseIds)
                    .build());

            if (node.has("children") && node.get("children").isArray()) {
                node.get("children").forEach(child -> parseFoldersRecursive(child, id, folders));
            }
        }
    }
}