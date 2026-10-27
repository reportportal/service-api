package com.epam.reportportal.base.infrastructure.persistence.dao.custom;

import com.epam.reportportal.base.infrastructure.persistence.entity.log.LogMessage;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.json.JsonMapper;

/**
 * Simple client to work with Elasticsearch.
 *
 * @author <a href="mailto:maksim_antonov@epam.com">Maksim Antonov</a>
 */
@Service
@ConditionalOnProperty(prefix = "rp.searchengine", name = "host")
@Slf4j
public class ElasticSearchClient {

  public static final String INDEX_PREFIX = "logs-reportportal-";
  public static final String CREATE_COMMAND = "{\"create\":{ }}\n";
  public static final String ELASTIC_DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS";
  public static final Integer MAX_RESULT_REQUEST = 5000;
  public static final String LOG_MESSAGE_FIELD_NAME = "message";
  private static final String ITEM_ID_FIELD = "itemId";
  private static final String LAUNCH_ID_FIELD = "launchId";
  private static final String QUERY_FIELD = "query";
  private static final String TERMS_FIELD = "terms";
  private static final String SOURCE_FIELD = "_source";

  private final String host;
  private final RestClient restClient;
  private final JsonMapper jsonMapper;

  public ElasticSearchClient(@Value("${rp.searchengine.host}") String host,
      @Value("${rp.searchengine.username:}") String username,
      @Value("${rp.searchengine.password:}") String password, JsonMapper jsonMapper) {
    RestClient.Builder clientBuilder = RestClient.builder()
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

    if (!username.isEmpty() && !password.isEmpty()) {
      clientBuilder
          .defaultHeaders(headers -> headers.setBasicAuth(username, password));
    }

    this.restClient = clientBuilder.build();
    this.host = host;
    this.jsonMapper = jsonMapper;
  }

  public void save(List<LogMessage> logMessageList) {
    if (CollectionUtils.isEmpty(logMessageList)) {
      return;
    }
    Map<String, String> logsByIndex = new HashMap<>();

    logMessageList.forEach(logMessage -> {
      String indexName = getIndexName(logMessage.getProjectId());
      String logCreateBody = CREATE_COMMAND + toJson(convertToDocument(logMessage)) + "\n";

      if (logsByIndex.containsKey(indexName)) {
        logsByIndex.put(indexName, logsByIndex.get(indexName) + logCreateBody);
      } else {
        logsByIndex.put(indexName, logCreateBody);
      }
    });

    logsByIndex.forEach((indexName, body) -> restClient.put()
        .uri(host + "/" + indexName + "/_bulk?refresh")
        .body(body)
        .retrieve()
        .toBodilessEntity());
  }

  public void deleteLogsByLogIdAndProjectId(Long projectId, Long logId) {
    Map<String, Object> terms = Map.of("id", List.of(logId));

    deleteLogsByTermsAndProjectId(projectId, terms);
  }

  public void deleteLogsByItemSetAndProjectId(Long projectId, Set<Long> itemIds) {
    Map<String, Object> terms = Map.of(ITEM_ID_FIELD, itemIds);

    deleteLogsByTermsAndProjectId(projectId, terms);
  }

  public void deleteLogsByLaunchIdAndProjectId(Long projectId, Long launchId) {
    Map<String, Object> terms = Map.of(LAUNCH_ID_FIELD, List.of(launchId));

    deleteLogsByTermsAndProjectId(projectId, terms);
  }

  public void deleteLogsByLaunchListAndProjectId(Long projectId, List<Long> launches) {
    Map<String, Object> terms = Map.of(LAUNCH_ID_FIELD, launches);

    deleteLogsByTermsAndProjectId(projectId, terms);
  }

  public void deleteLogsByProjectId(Long projectId) {
    String indexName = getIndexName(projectId);
    try {
      restClient.delete()
          .uri(host + "/_data_stream/" + indexName)
          .retrieve()
          .toBodilessEntity();
    } catch (Exception exception) {
      // to avoid checking of exists stream or not
      log.error("DELETE stream from ES {} Project: {} Message: {}", indexName, projectId, exception.getMessage());
    }
  }

  public LogMessage getLogMessageByProjectIdAndId(Long projectId, Long id) {
    Map<Long, LogMessage> result = getLogMessagesByProjectIdAndIds(projectId, List.of(id));
    return MapUtils.isEmpty(result) ? null : result.get(id);
  }

  public Map<Long, LogMessage> getLogMessagesByProjectIdAndIds(Long projectId, List<Long> logIds) {
    Map<Long, LogMessage> logMessageMap = new HashMap<>();
    int i = 0;
    while (true) {
      int i2 = Math.min(logIds.size(), i + MAX_RESULT_REQUEST);
      List<Long> logIdsBatch = logIds.subList(i, i2);
      logMessageMap.putAll(getLogMessageMapBatch(projectId, logIdsBatch, MAX_RESULT_REQUEST));
      if (i2 == logIds.size()) {
        break;
      }
      i += MAX_RESULT_REQUEST;
    }

    return logMessageMap;
  }

  public List<Long> searchTestItemIdsByLogIdsAndString(Long projectId, Collection<Long> logIds,
      String string) {
    Map<String, Object> filterTerms = Map.of("id", logIds);
    List<String> sourceFields = List.of(ITEM_ID_FIELD);

    Map<String, Object> searchJson = getSearchStringJson(string, filterTerms, sourceFields, logIds.size());

    return searchTestItemIdsByConditions(projectId, searchJson);
  }

  public List<Long> searchTestItemIdsByLogIdsAndRegexp(Long projectId, Collection<Long> logIds,
      String pattern) {
    Map<String, Object> filterTerms = Map.of("id", logIds);
    List<String> sourceFields = List.of(ITEM_ID_FIELD);

    Map<String, Object> searchJson = getSearchRegexpJson(pattern, filterTerms, sourceFields, logIds.size());

    return searchTestItemIdsByConditions(projectId, searchJson);
  }

  /**
   * Search LogIds by logIds and conditions. LogIds instead of logs was used due to optimization.
   *
   * @param projectId
   * @param searchJson
   * @return
   */
  private List<Long> searchTestItemIdsByConditions(Long projectId, Map<String, Object> searchJson) {
    String indexName = getIndexName(projectId);

    Set<Long> testItemIds = new HashSet<>();

    try {
      LinkedHashMap<String, Object> result = search(indexName, searchJson);

      List<LinkedHashMap<String, Object>> hits =
          (List<LinkedHashMap<String, Object>>) ((LinkedHashMap<String, Object>) result.get(
              "hits")).get("hits");

      if (CollectionUtils.isNotEmpty(hits)) {
        for (LinkedHashMap<String, Object> hit : hits) {
          Map<String, Object> source = (Map<String, Object>) hit.get(SOURCE_FIELD);
          Long testItemId = ((Integer) source.get(ITEM_ID_FIELD)).longValue();
          testItemIds.add(testItemId);
        }

      }

    } catch (Exception exception) {
      log.error("Search error {} SearchJson: {} Message: {}", indexName, searchJson, exception.getMessage());
    }

    return new ArrayList<>(testItemIds);
  }

  private Map<Long, LogMessage> getLogMessageMapBatch(Long projectId, List<Long> logIds, Integer size) {
    String indexName = getIndexName(projectId);

    Map<String, Object> terms = Map.of("id", logIds);
    Map<Long, LogMessage> logMessageMap = new HashMap<>();

    try {
      Map<String, Object> searchJson = getSearchJson(terms, size);

      LinkedHashMap<String, Object> result = search(indexName, searchJson);

      List<LinkedHashMap<String, Object>> hits =
          (List<LinkedHashMap<String, Object>>) ((LinkedHashMap<String, Object>) result.get(
              "hits")).get("hits");

      if (CollectionUtils.isNotEmpty(hits)) {
        for (LinkedHashMap<String, Object> hit : hits) {
          Map<String, Object> source = (Map<String, Object>) hit.get(SOURCE_FIELD);
          LogMessage logMessage = convertElasticDataToLogMessage(projectId, source);
          logMessageMap.put(logMessage.getId(), logMessage);
        }

      }

    } catch (Exception exception) {
      log.error("Search error {} Terms: {} Message: {}", indexName, terms, exception.getMessage());
    }

    return logMessageMap;
  }

  private LogMessage convertElasticDataToLogMessage(Long projectId, Map<String, Object> source) {
    String timestampString = (String) source.get("@timestamp");
    if (timestampString.lastIndexOf(".") > -1) {
      int millsNumber = timestampString.length() - timestampString.lastIndexOf(".") - 1;
      if (millsNumber < 6) {
        timestampString += "0".repeat(6 - millsNumber);
      }
    } else {
      timestampString += "." + "0".repeat(6);
    }
    var dateTime = LocalDateTime.parse(timestampString,
            DateTimeFormatter.ofPattern(ELASTIC_DATETIME_FORMAT))
        .toInstant(ZoneOffset.UTC);
    return new LogMessage(((Integer) source.get("id")).longValue(), dateTime,
        (String) source.get(LOG_MESSAGE_FIELD_NAME), ((Integer) source.get(ITEM_ID_FIELD)).longValue(),
        ((Integer) source.get(LAUNCH_ID_FIELD)).longValue(), projectId
    );
  }

  private void deleteLogsByTermsAndProjectId(Long projectId, Map<String, Object> terms) {
    String indexName = getIndexName(projectId);
    try {
      Map<String, Object> deleteByLaunch = getDeleteJson(terms);

      restClient.post()
          .uri(host + "/" + indexName + "/_delete_by_query")
          .body(toJson(deleteByLaunch))
          .retrieve()
          .toBodilessEntity();
    } catch (Exception exception) {
      // to avoid checking of exists stream or not
      log.error("DELETE logs from stream ES error {} Terms: {} Message: {}", indexName, terms, exception.getMessage());
    }
  }

  private String getIndexName(Long projectId) {
    return INDEX_PREFIX + projectId;
  }

  private Map<String, Object> getDeleteJson(Map<String, Object> terms) {
    return Map.of(QUERY_FIELD, Map.of(TERMS_FIELD, terms));
  }

  private Map<String, Object> getSearchJson(Map<String, Object> terms, Integer size) {
    return Map.of(
        QUERY_FIELD, Map.of(TERMS_FIELD, terms),
        "size", size
    );
  }

  private Map<String, Object> getSearchStringJson(String string, Map<String, Object> filterTerms,
      List<String> sourceFields, Integer size) {
    return Map.of(
        SOURCE_FIELD, sourceFields,
        QUERY_FIELD, Map.of("match_phrase", Map.of(LOG_MESSAGE_FIELD_NAME, string)),
        "post_filter", Map.of(TERMS_FIELD, filterTerms),
        "size", size
    );
  }

  // Separated from getSearchStringJson, because possibly will be added some
  // specific configuration for regexp optimization.
  private Map<String, Object> getSearchRegexpJson(String pattern, Map<String, Object> filterTerms,
      List<String> sourceFields, Integer size) {
    return Map.of(
        SOURCE_FIELD, sourceFields,
        QUERY_FIELD, Map.of("regexp", Map.of(LOG_MESSAGE_FIELD_NAME, pattern)),
        "post_filter", Map.of(TERMS_FIELD, filterTerms),
        "size", size
    );
  }

  private Map<String, Object> convertToDocument(LogMessage logMessage) {
    Map<String, Object> document = new LinkedHashMap<>();
    document.put("id", logMessage.getId());
    document.put(LOG_MESSAGE_FIELD_NAME, logMessage.getLogMessage());
    document.put(ITEM_ID_FIELD, logMessage.getItemId());
    document.put("@timestamp", logMessage.getLogTime());
    document.put(LAUNCH_ID_FIELD, logMessage.getLaunchId());
    return document;
  }

  private String toJson(Map<String, Object> document) {
    return jsonMapper.writeValueAsString(document);
  }

  private LinkedHashMap<String, Object> search(String indexName, Map<String, Object> searchJson) {
    return restClient.post()
        .uri(host + "/" + indexName + "/_search")
        .body(toJson(searchJson))
        .retrieve()
        .body(new ParameterizedTypeReference<>() {
        });
  }

}
