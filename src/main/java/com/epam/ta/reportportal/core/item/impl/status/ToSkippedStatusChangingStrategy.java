/*
 * Copyright 2020 EPAM Systems
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

package com.epam.ta.reportportal.core.item.impl.status;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.analyzer.auto.LogIndexer;
import com.epam.ta.reportportal.core.analyzer.auto.impl.AnalyzerUtils;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.item.impl.IssueTypeHandler;
import com.epam.ta.reportportal.dao.*;
import com.epam.ta.reportportal.entity.ItemAttribute;
import com.epam.ta.reportportal.entity.enums.LogLevel;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.project.Project;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static com.epam.ta.reportportal.commons.Preconditions.statusIn;
import static com.epam.ta.reportportal.entity.enums.StatusEnum.FAILED;
import static com.epam.ta.reportportal.ws.model.ErrorType.INCORRECT_REQUEST;
import static java.util.Optional.ofNullable;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Service
public class ToSkippedStatusChangingStrategy extends AbstractStatusChangingStrategy {

	public static final String SKIPPED_ISSUE_KEY = "skippedIssue";

	private final ItemAttributeRepository itemAttributeRepository;

	@Autowired
	protected ToSkippedStatusChangingStrategy(ProjectRepository projectRepository, LaunchRepository launchRepository,
			IssueTypeHandler issueTypeHandler, MessageBus messageBus, IssueEntityRepository issueEntityRepository,
			LogRepository logRepository, LogIndexer logIndexer, ItemAttributeRepository itemAttributeRepository) {
		super(projectRepository, launchRepository, issueTypeHandler, messageBus, issueEntityRepository, logRepository, logIndexer);
		this.itemAttributeRepository = itemAttributeRepository;
	}

	@Override
	protected void updateStatus(Project project, Launch launch, TestItem testItem, StatusEnum providedStatus, ReportPortalUser user) {
		BusinessRule.expect(providedStatus, statusIn(StatusEnum.SKIPPED))
				.verify(INCORRECT_REQUEST,
						Suppliers.formattedSupplier("Incorrect status - '{}', only '{}' is allowed", providedStatus, StatusEnum.SKIPPED)
								.get()
				);

		testItem.getItemResults().setStatus(providedStatus);
		Optional<ItemAttribute> skippedIssueAttribute = itemAttributeRepository.findByLaunchIdAndKeyAndSystem(testItem.getLaunchId(),
				SKIPPED_ISSUE_KEY,
				true
		);

		boolean issueRequired = skippedIssueAttribute.isPresent() && BooleanUtils.toBoolean(skippedIssueAttribute.get().getValue());

		if (issueRequired) {
			if (testItem.getItemResults().getIssue() == null && testItem.isHasStats()) {
				addToInvestigateIssue(testItem, project.getId());
			}
		} else {
			ofNullable(testItem.getItemResults().getIssue()).map(issue -> {
				issue.setTestItemResults(null);
				testItem.getItemResults().setIssue(null);
				return issue.getIssueId();
			}).ifPresent(issueEntityRepository::deleteById);
		}

		List<Long> itemsToReindex = changeParentsStatuses(testItem, launch, true, user);
		itemsToReindex.add(testItem.getItemId());
		logIndexer.cleanIndex(project.getId(),
				logRepository.findIdsUnderTestItemByLaunchIdAndTestItemIdsAndLogLevelGte(testItem.getLaunchId(),
						itemsToReindex,
						LogLevel.ERROR.toInt()
				)
		);

		if (!issueRequired) {
			itemsToReindex.remove(itemsToReindex.size() - 1);
		}

		logIndexer.indexItemsLogs(project.getId(), launch.getId(), itemsToReindex, AnalyzerUtils.getAnalyzerConfig(project));
	}

	@Override
	protected StatusEnum evaluateParentItemStatus(TestItem parentItem, TestItem childItem) {
		return FAILED;
	}
}
