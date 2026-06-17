/*
 * Copyright 2019 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.core.analyzer.auto.client;

import com.epam.reportportal.model.analyzer.IndexLaunch;
import com.epam.ta.reportportal.core.analyzer.auto.client.impl.AnalyzerUtils;
import com.epam.ta.reportportal.core.analyzer.auto.client.model.SuggestInfo;
import com.epam.ta.reportportal.core.analyzer.auto.client.model.SuggestRq;
import com.epam.ta.reportportal.core.analyzer.auto.client.model.cluster.ClusterData;
import com.epam.ta.reportportal.core.analyzer.auto.client.model.cluster.GenerateClustersRq;
import com.epam.ta.reportportal.model.analyzer.SearchRq;
import com.epam.ta.reportportal.model.analyzer.SearchRs;
import java.util.List;

/**
 * Rabbit client for all log indexing/analysis services. Such services are those that have tag
 * {@link AnalyzerUtils#ANALYZER_KEY} in service's metadata.
 * <p>
 * To define that service indexes/collecting data it should be indicated by tag
 * {@link AnalyzerUtils#ANALYZER_INDEX} with <code>true</code> in metadata. If tag is not provided
 * it is <code>false</code> by default
 * <p>
 * Analysis requests are dispatched to all analyzers simultaneously and asynchronously. Priority is
 * no longer resolved: every result delivered to the reply queue is treated as the actual one and
 * overwrites the previous issue for the item (last-write-wins).
 *
 * @author Ivan Sharamet
 * @author Pavel Bortnik
 */
public interface AnalyzerServiceClient {

  /**
   * Checks if any client is available
   *
   * @return <code>true</code> if some exists
   */
  boolean hasClients();

  /**
   * Dispatches the launch for analysis to every analyzer asynchronously (fire-and-forget). The
   * request is sent to all analyzer exchanges simultaneously; results are delivered back through
   * the reply queue and handled by a dedicated listener.
   *
   * @param rq Launch
   */
  void analyze(IndexLaunch rq);

  /**
   * Searches logs with similar log message
   *
   * @param rq {@link SearchRq} request
   * @return {@link List} of {@link SearchRs} of log ids
   */
  List<SearchRs> searchLogs(SearchRq rq);

  /**
   * Removes suggest index
   *
   * @param projectId Project/index id
   */
  void removeSuggest(Long projectId);

  /**
   * Searches suggests in analyzer for provided item
   *
   * @param rq {@link SuggestRq} request
   * @return {@link List} of {@link SuggestInfo} - list of founded suggests
   */
  List<SuggestInfo> searchSuggests(SuggestRq rq);

  /**
   * Sends to analyzer info about user choice from suggests
   *
   * @param suggestInfos Info about user suggests
   */
  void handleSuggestChoice(List<SuggestInfo> suggestInfos);

  ClusterData generateClusters(GenerateClustersRq generateClustersRq);
}
