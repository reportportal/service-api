/*
 * Copyright 2018 EPAM Systems
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

package com.epam.ta.reportportal.core.analyzer.impl;

import com.epam.ta.reportportal.core.analyzer.AnalyzerService;
import com.epam.ta.reportportal.core.analyzer.AnalyzerServiceAsync;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.ws.model.project.AnalyzerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Service
public class AnalyzerServiceAsyncImpl implements AnalyzerServiceAsync {

	private final AnalyzerService analyzerService;

	@Autowired
	public AnalyzerServiceAsyncImpl(AnalyzerService analyzerService) {
		this.analyzerService = analyzerService;
	}

	@Override
	public CompletableFuture<Void> analyze(Launch launch, List<Long> itemIds, AnalyzerConfig analyzerConfig) {
		return CompletableFuture.runAsync(() -> analyzerService.runAnalyzers(launch, itemIds, analyzerConfig));
	}

	@Override
	public boolean hasAnalyzers() {
		return analyzerService.hasAnalyzers();
	}

}
