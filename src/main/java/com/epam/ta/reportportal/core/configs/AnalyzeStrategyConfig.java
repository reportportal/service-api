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

package com.epam.ta.reportportal.core.configs;

import com.epam.ta.reportportal.core.analyzer.ILogIndexer;
import com.epam.ta.reportportal.core.analyzer.strategy.*;
import com.epam.ta.reportportal.database.dao.TestItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Pavel Bortnik
 */
@Configuration
public class AnalyzeStrategyConfig {

	@Autowired
	private TestItemRepository testItemRepository;

	@Autowired
	private ILogIndexer logIndexer;

	@Bean
	public Map<AnalyzeItemsMode, AnalyzeItemsStrategy> analyzerModeMapping() {
		Map<AnalyzeItemsMode, AnalyzeItemsStrategy> mapping = new HashMap<>();
		mapping.put(AnalyzeItemsMode.TO_INVESTIGATE, new ToInvestigateStrategy(testItemRepository));
		mapping.put(AnalyzeItemsMode.AUTO_ANALYZED, new AutoAnalyzedStrategy(testItemRepository, logIndexer));
		mapping.put(AnalyzeItemsMode.MANUALLY_ANALYZED, new ManuallyAnalyzedStrategy(testItemRepository, logIndexer));
		return mapping;
	}

	@Bean
	public AnalyzeStrategyFactory analyzeStrategyFactory() {
		return new AnalyzeStrategyFactory(analyzerModeMapping());
	}
}
