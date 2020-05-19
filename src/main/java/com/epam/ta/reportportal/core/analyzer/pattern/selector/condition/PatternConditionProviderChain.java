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

package com.epam.ta.reportportal.core.analyzer.pattern.selector.condition;

import com.epam.ta.reportportal.commons.querygen.CompositeFilterCondition;
import com.epam.ta.reportportal.commons.querygen.ConvertibleCondition;
import com.epam.ta.reportportal.core.analyzer.auto.strategy.analyze.AnalyzeItemsMode;
import com.epam.ta.reportportal.core.analyzer.pattern.selector.condition.impl.AutoAnalyzedPatternConditionProvider;
import com.epam.ta.reportportal.core.analyzer.pattern.selector.condition.impl.ManualPatternConditionProvider;
import com.epam.ta.reportportal.core.analyzer.pattern.selector.condition.impl.ToInvestigatePatternConditionProvider;
import com.epam.ta.reportportal.dao.IssueGroupRepository;
import com.epam.ta.reportportal.entity.enums.TestItemIssueGroup;
import com.epam.ta.reportportal.entity.item.issue.IssueGroup;
import com.google.common.collect.Sets;
import org.jooq.Operator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Component
public class PatternConditionProviderChain {

	private final Set<PatternConditionProvider> patternConditionProviders;

	@Autowired
	public PatternConditionProviderChain(IssueGroupRepository issueGroupRepository) {
		Supplier<IssueGroup> issueGroupSupplier = () -> issueGroupRepository.findByTestItemIssueGroup(TestItemIssueGroup.TO_INVESTIGATE);
		this.patternConditionProviders = Sets.newHashSet(new AutoAnalyzedPatternConditionProvider(AnalyzeItemsMode.AUTO_ANALYZED),
				new ManualPatternConditionProvider(AnalyzeItemsMode.MANUALLY_ANALYZED, issueGroupSupplier),
				new ToInvestigatePatternConditionProvider(AnalyzeItemsMode.TO_INVESTIGATE,
						issueGroupSupplier
				)
		);
	}

	public Optional<ConvertibleCondition> provideCondition(Set<AnalyzeItemsMode> analyzeItemsModes) {
		List<ConvertibleCondition> convertibleConditions = patternConditionProviders.stream()
				.map(provider -> provider.provideCondition(analyzeItemsModes))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(Collectors.toList());

		return convertibleConditions.isEmpty() ?
				Optional.empty() :
				Optional.of(new CompositeFilterCondition(convertibleConditions, Operator.AND));
	}
}
