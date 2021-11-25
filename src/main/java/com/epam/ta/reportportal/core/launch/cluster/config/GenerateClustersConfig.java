/*
 * Copyright 2021 EPAM Systems
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

package com.epam.ta.reportportal.core.launch.cluster.config;

import com.epam.ta.reportportal.ws.model.project.AnalyzerConfig;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class GenerateClustersConfig {

	private ClusterEntityContext entityContext;

	private AnalyzerConfig analyzerConfig;

	private boolean forUpdate;
	private boolean cleanNumbers;

	public GenerateClustersConfig() {
	}

	public ClusterEntityContext getEntityContext() {
		return entityContext;
	}

	public void setEntityContext(ClusterEntityContext entityContext) {
		this.entityContext = entityContext;
	}

	public AnalyzerConfig getAnalyzerConfig() {
		return analyzerConfig;
	}

	public void setAnalyzerConfig(AnalyzerConfig analyzerConfig) {
		this.analyzerConfig = analyzerConfig;
	}

	public boolean isForUpdate() {
		return forUpdate;
	}

	public void setForUpdate(boolean forUpdate) {
		this.forUpdate = forUpdate;
	}

	public boolean isCleanNumbers() {
		return cleanNumbers;
	}

	public void setCleanNumbers(boolean cleanNumbers) {
		this.cleanNumbers = cleanNumbers;
	}
}
