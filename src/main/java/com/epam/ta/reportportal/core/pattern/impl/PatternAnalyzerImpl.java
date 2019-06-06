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

package com.epam.ta.reportportal.core.pattern.impl;

import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.events.activity.PatternMatchedEvent;
import com.epam.ta.reportportal.core.pattern.PatternAnalyzer;
import com.epam.ta.reportportal.core.pattern.selector.PatternAnalysisSelector;
import com.epam.ta.reportportal.dao.IssueGroupRepository;
import com.epam.ta.reportportal.dao.PatternTemplateRepository;
import com.epam.ta.reportportal.entity.enums.TestItemIssueGroup;
import com.epam.ta.reportportal.entity.item.issue.IssueGroup;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.pattern.PatternTemplateTestItemPojo;
import com.epam.ta.reportportal.entity.pattern.PatternTemplateType;
import com.epam.ta.reportportal.ws.converter.converters.PatternTemplateConverter;
import com.epam.ta.reportportal.ws.model.activity.PatternTemplateActivityResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class PatternAnalyzerImpl implements PatternAnalyzer {

	private final IssueGroupRepository issueGroupRepository;

	private final PatternTemplateRepository patternTemplateRepository;

	private final Map<PatternTemplateType, PatternAnalysisSelector> patternAnalysisSelectorMapping;

	private final TaskExecutor patternAnalysisTaskExecutor;

	private final MessageBus messageBus;

	@Autowired
	public PatternAnalyzerImpl(IssueGroupRepository issueGroupRepository, PatternTemplateRepository patternTemplateRepository,
			@Qualifier("patternAnalysisSelectorMapping") Map<PatternTemplateType, PatternAnalysisSelector> patternAnalysisSelectorMapping,
			TaskExecutor patternAnalysisTaskExecutor, MessageBus messageBus) {
		this.issueGroupRepository = issueGroupRepository;
		this.patternTemplateRepository = patternTemplateRepository;
		this.patternAnalysisSelectorMapping = patternAnalysisSelectorMapping;
		this.patternAnalysisTaskExecutor = patternAnalysisTaskExecutor;
		this.messageBus = messageBus;
	}

	@Override
	public void analyzeTestItems(Launch launch) {

		IssueGroup issueGroup = issueGroupRepository.findByTestItemIssueGroup(TestItemIssueGroup.TO_INVESTIGATE);

		patternTemplateRepository.findAllByProjectIdAndEnabled(launch.getProjectId(), true)
				.forEach(patternTemplate -> patternAnalysisTaskExecutor.execute(() -> {
					List<PatternTemplateTestItemPojo> patternTemplateTestItems = patternAnalysisSelectorMapping.get(patternTemplate.getTemplateType())
							.selectItemsByPattern(launch.getId(), issueGroup, patternTemplate);
					patternTemplateRepository.saveInBatch(patternTemplateTestItems);

					PatternTemplateActivityResource patternTemplateActivityResource = PatternTemplateConverter.TO_ACTIVITY_RESOURCE.apply(
							patternTemplate);
					patternTemplateTestItems.forEach(patternItem -> {
						PatternMatchedEvent patternMatchedEvent = new PatternMatchedEvent(launch.getId(),
								patternItem.getTestItemId(),
								patternTemplateActivityResource
						);

						messageBus.publishActivity(patternMatchedEvent);
					});

				}));
	}

}
