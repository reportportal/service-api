/*
 * Copyright 2017 EPAM Systems
 *
 *
 * This file is part of EPAM Report Portal.
 * https://github.com/reportportal/service-api
 *
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.epam.ta.reportportal.util.analyzer;

import com.epam.ta.reportportal.util.analyzer.model.IndexLaunch;
import com.epam.ta.reportportal.util.analyzer.model.IndexRs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

/**
 * Simple HTTP client for log indexing/analysis service.
 *
 * @author Ivan Sharamet
 *
 */
@Service("analyzerServiceClient")
public class AnalyzerServiceClient {

    private static final String INDEX_PATH = "/_index";
    private static final String ANALYZE_PATH = "/_analyze";

    private final RestTemplate restTemplate;
    private final String serviceUrl;

    @Autowired
    public AnalyzerServiceClient(RestTemplate restTemplate, @Value("${rp.analyzer.url}") String serviceUrl) {
        this.restTemplate = restTemplate;
        this.serviceUrl = serviceUrl;
    }

    public IndexRs index(List<IndexLaunch> rq) {
        ResponseEntity<IndexRs> rsEntity = restTemplate.postForEntity(serviceUrl + INDEX_PATH, rq, IndexRs.class);
        return rsEntity.getBody();
    }

    public IndexLaunch analyze(IndexLaunch rq) {
        ResponseEntity<IndexLaunch[]> rsEntity =
                restTemplate.postForEntity(
                        serviceUrl + ANALYZE_PATH, Collections.singletonList(rq), IndexLaunch[].class);
        IndexLaunch[] rs = rsEntity.getBody();
        return rs.length > 0 ? rs[0] : null;
    }
}
