/*
 * Copyright 2018 EPAM Systems
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
 *
 */

package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.database.entity.AnalyzeMode;
import com.epam.ta.reportportal.database.entity.ProjectAnalyzerConfig;
import com.epam.ta.reportportal.ws.model.project.AnalyzerConfig;

import java.util.Optional;
import java.util.function.Function;

/**
 * @author Pavel Bortnik
 */
public final class AnalyzerConfigConverter {

	private AnalyzerConfigConverter() {
		//static only
	}

	public static final Function<ProjectAnalyzerConfig, AnalyzerConfig> TO_RESOURCE = projectAnalyzerConfig -> {
		AnalyzerConfig analyzerConfig = new AnalyzerConfig();
		analyzerConfig.setAnalyzerMode(
				Optional.ofNullable(projectAnalyzerConfig.getAnalyzerMode()).orElse(AnalyzeMode.BY_LAUNCH_NAME).getValue());
		analyzerConfig.setIsAutoAnalyzerEnabled(projectAnalyzerConfig.getIsAutoAnalyzerEnabled());
		analyzerConfig.setMinDocFreq(projectAnalyzerConfig.getMinDocFreq());
		analyzerConfig.setMinTermFreq(projectAnalyzerConfig.getMinTermFreq());
		analyzerConfig.setMinShouldMatch(projectAnalyzerConfig.getMinShouldMatch());
		analyzerConfig.setNumberOfLogLines(projectAnalyzerConfig.getNumberOfLogLines());
		analyzerConfig.setIndexingRunning(projectAnalyzerConfig.isIndexingRunning());
		return analyzerConfig;
	};
}
