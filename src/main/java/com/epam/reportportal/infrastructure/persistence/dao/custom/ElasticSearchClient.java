package com.epam.reportportal.infrastructure.persistence.dao.custom;

import com.epam.reportportal.infrastructure.persistence.entity.log.LogMessage;
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
import org.apache.commons.collections4.MapUtils;
import org.jooq.tools.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

/**
 * Simple client to work with Elasticsearch.
 *
 * @author <a href="mailto:maksim_antonov@epam.com">Maksim Antonov</a>
 */
@Service
@ConditionalOnProperty(prefix = "rp.searchengine", name = "host")
public class ElasticSearchClient {

  public static final String INDEX_PREFIX = "logs-reportportal-";
  public static final String CREATE_COMMAND = "{\"create\":{ }}\n";
  public static final String ELASTIC_DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS";
  public static final Integer MAX_RESULT_REQUEST = 5000;
  public static final String LOG_MESSAGE_FIELD_NAME = "message";
  protected final Logger LOGGER = LoggerFactory.getLogger(ElasticSearchClient.class);

  private final String host;
  private final RestTemplate restTemplate;

  public ElasticSearchClient(@Value("${rp.searchengine.host}") String host,
      @Value("${rp.searchengine.username:}") String username,
      @Value("${rp.searchengine.password:}") String password) {
    restTemplate = new RestTemplate();

    if (!username.isEmpty() && !password.isEmpty()) {
      restTemplate.getInterceptors().add(new BasicAuthenticationInterceptor(username, password));
    }

    this.host = host;
  }

  public void save(List<LogMessage> logMessageList) {
    if (CollectionUtils.isEmpty(logMessageList)) {
      return;
    }
    Map<String, String> logsByIndex = new HashMap<>();

    logMessageList.forEach(logMessage -> {
      String indexName = getIndexName(logMessage.getProjectId());
      String logCreateBody = CREATE_COMMAND + convertToJson(logMessage) + "\n";

      if (logsByIndex.containsKey(indexName)) {
        logsByIndex.put(indexName, logsByIndex.get(indexName) + logCreateBody);
      } else {
        logsByIndex.put(indexName, logCreateBody);
      }
    });

    logsByIndex.forEach((indexName, body) -> {
      restTemplate.put(host + "/" + indexName + "/_bulk?refresh", getStringHttpEntity(body));
    });
  }

  public void deleteLogsByLogIdAndProjectId(Long projectId, Long logId) {
    JSONObject terms = new JSONObject();
    terms.put("id", List.of(logId));

    deleteLogsByTermsAndProjectId(projectId, terms);
  }

  public void deleteLogsByItemSetAndProjectId(Long projectId, Set<Long> itemIds) {
    JSONObject terms = new JSONObject();
    terms.put("itemId", itemIds);

    deleteLogsByTermsAndProjectId(projectId, terms);
  }

  public void deleteLogsByLaunchIdAndProjectId(Long projectId, Long launchId) {
    JSONObject terms = new JSONObject();
    terms.put("launchId", List.of(launchId));

    deleteLogsByTermsAndProjectId(projectId, terms);
  }

  public void deleteLogsByLaunchListAndProjectId(Long projectId, List<Long> launches) {
    JSONObject terms = new JSONObject();
    terms.put("launchId", launches);

    deleteLogsByTermsAndProjectId(projectId, terms);
  }

  public void deleteLogsByProjectId(Long projectId) {
    String indexName = getIndexName(projectId);
    try {
      restTemplate.delete(host + "/_data_stream/" + indexName);
    } catch (Exception exception) {
      // to avoid checking of exists stream or not
      LOGGER.error("DELETE stream from ES " + indexName + " Project: " + projectId + " Message: "
          + exception.getMessage());
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
    JSONObject filterTerms = new JSONObject();
    filterTerms.put("id", logIds);
    List<String> sourceFields = List.of("itemId");

    JSONObject searchJson = getSearchStringJson(string, filterTerms, sourceFields, logIds.size());

    return searchTestItemIdsByConditions(projectId, searchJson);
  }

  public List<Long> searchTestItemIdsByLogIdsAndRegexp(Long projectId, Collection<Long> logIds,
      String pattern) {
    JSONObject filterTerms = new JSONObject();
    filterTerms.put("id", logIds);
    List<String> sourceFields = List.of("itemId");

    JSONObject searchJson = getSearchRegexpJson(pattern, filterTerms, sourceFields, logIds.size());

    return searchTestItemIdsByConditions(projectId, searchJson);
  }

  /**
   * Search LogIds by logIds and conditions. LogIds instead of logs was used due to optimization.
   *
   * @param projectId
   * @param searchJson
   * @return
   */
  private List<Long> searchTestItemIdsByConditions(Long projectId, JSONObject searchJson) {
    String indexName = getIndexName(projectId);

    Set<Long> testItemIds = new HashSet<>();

    try {
      HttpEntity<String> searchRequest = getStringHttpEntity(searchJson.toString());

      LinkedHashMap<String, Object> result =
          restTemplate.postForObject(host + "/" + indexName + "/_search", searchRequest,
              LinkedHashMap.class
          );

      List<LinkedHashMap<String, Object>> hits =
          (List<LinkedHashMap<String, Object>>) ((LinkedHashMap<String, Object>) result.get(
              "hits")).get("hits");

      if (org.apache.commons.collections.CollectionUtils.isNotEmpty(hits)) {
        for (LinkedHashMap<String, Object> hit : hits) {
          Map<String, Object> source = (Map<String, Object>) hit.get("_source");
          Long testItemId = ((Integer) source.get("itemId")).longValue();
          testItemIds.add(testItemId);
        }

      }

    } catch (Exception exception) {
      LOGGER.error("Search error " + indexName + " SearchJson: " + searchJson + " Message: "
          + exception.getMessage());
    }

    return new ArrayList<>(testItemIds);
  }

  private Map<Long, LogMessage> getLogMessageMapBatch(Long projectId, List<Long> logIds,
      Integer size) {
    String indexName = getIndexName(projectId);

    JSONObject terms = new JSONObject();
    terms.put("id", logIds);
    Map<Long, LogMessage> logMessageMap = new HashMap<>();

    try {
      JSONObject searchJson = getSearchJson(terms, size);
      HttpEntity<String> searchRequest = getStringHttpEntity(searchJson.toString());

      LinkedHashMap<String, Object> result =
          restTemplate.postForObject(host + "/" + indexName + "/_search", searchRequest,
              LinkedHashMap.class
          );

      List<LinkedHashMap<String, Object>> hits =
          (List<LinkedHashMap<String, Object>>) ((LinkedHashMap<String, Object>) result.get(
              "hits")).get("hits");

      if (org.apache.commons.collections.CollectionUtils.isNotEmpty(hits)) {
        for (LinkedHashMap<String, Object> hit : hits) {
          Map<String, Object> source = (Map<String, Object>) hit.get("_source");
          LogMessage logMessage = convertElasticDataToLogMessage(projectId, source);
          logMessageMap.put(logMessage.getId(), logMessage);
        }

      }

    } catch (Exception exception) {
      LOGGER.error(
          "Search error " + indexName + " Terms: " + terms + " Message: " + exception.getMessage());
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
        (String) source.get("message"), ((Integer) source.get("itemId")).longValue(),
        ((Integer) source.get("launchId")).longValue(), projectId
    );
  }

  private void deleteLogsByTermsAndProjectId(Long projectId, JSONObject terms) {
    String indexName = getIndexName(projectId);
    try {
      JSONObject deleteByLaunch = getDeleteJson(terms);
      HttpEntity<String> deleteRequest = getStringHttpEntity(deleteByLaunch.toString());

      restTemplate.postForObject(host + "/" + indexName + "/_delete_by_query", deleteRequest,
          JSONObject.class
      );
    } catch (Exception exception) {
      // to avoid checking of exists stream or not
      LOGGER.error(
          "DELETE logs from stream ES error " + indexName + " Terms: " + terms + " Message: "
              + exception.getMessage());
    }
  }

  private String getIndexName(Long projectId) {
    return INDEX_PREFIX + projectId;
  }

  private JSONObject getDeleteJson(JSONObject terms) {
    JSONObject query = new JSONObject();
    query.put("terms", terms);

    JSONObject deleteByLaunch = new JSONObject();
    deleteByLaunch.put("query", query);

    return deleteByLaunch;
  }

  private JSONObject getSearchJson(JSONObject terms, Integer size) {
    JSONObject query = new JSONObject();
    query.put("terms", terms);

    JSONObject searchJson = new JSONObject();
    searchJson.put("query", query);
    searchJson.put("size", size);

    return searchJson;
  }

  private JSONObject getSearchStringJson(String string, JSONObject filterTerms,
      List<String> sourceFields, Integer size) {
    JSONObject postFilter = new JSONObject();
    postFilter.put("terms", filterTerms);

    JSONObject matchPhrase = new JSONObject();
    matchPhrase.put(LOG_MESSAGE_FIELD_NAME, string);

    JSONObject query = new JSONObject();
    query.put("match_phrase", matchPhrase);

    JSONObject searchJson = new JSONObject();
    searchJson.put("_source", sourceFields);
    searchJson.put("query", query);
    searchJson.put("post_filter", postFilter);
    searchJson.put("size", size);

    return searchJson;
  }

  // Separated from getSearchStringJson, because possibly will be added some
  // specific configuration for regexp optimization.
  private JSONObject getSearchRegexpJson(String pattern, JSONObject filterTerms,
      List<String> sourceFields, Integer size) {
    JSONObject postFilter = new JSONObject();
    postFilter.put("terms", filterTerms);

    JSONObject regexp = new JSONObject();
    regexp.put(LOG_MESSAGE_FIELD_NAME, pattern);

    JSONObject query = new JSONObject();
    query.put("regexp", regexp);

    JSONObject searchJson = new JSONObject();
    searchJson.put("_source", sourceFields);
    searchJson.put("query", query);
    searchJson.put("post_filter", postFilter);
    searchJson.put("size", size);

    return searchJson;
  }

  private JSONObject convertToJson(LogMessage logMessage) {
    JSONObject personJsonObject = new JSONObject();
    personJsonObject.put("id", logMessage.getId());
    personJsonObject.put("message", logMessage.getLogMessage());
    personJsonObject.put("itemId", logMessage.getItemId());
    personJsonObject.put("@timestamp", logMessage.getLogTime());
    personJsonObject.put("launchId", logMessage.getLaunchId());

    return personJsonObject;
  }

  private HttpEntity<String> getStringHttpEntity(String body) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    return new HttpEntity<>(body, headers);
  }

}
