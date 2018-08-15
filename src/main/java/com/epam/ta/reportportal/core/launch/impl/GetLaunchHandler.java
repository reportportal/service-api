/*
 * Copyright 2016 EPAM Systems
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

package com.epam.ta.reportportal.core.launch.impl;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.dao.*;
//import com.epam.ta.reportportal.entity.widget.content.ComparisonStatisticsContent;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.ProjectFilter;
import com.epam.ta.reportportal.entity.enums.LaunchModeEnum;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.ws.converter.PagedResourcesAssembler;
import com.epam.ta.reportportal.ws.converter.converters.LaunchConverter;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.launch.LaunchResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.commons.validation.Suppliers.formattedSupplier;
import static com.epam.ta.reportportal.ws.converter.converters.LaunchConverter.TO_RESOURCE;
import static com.epam.ta.reportportal.ws.model.ErrorType.INCORRECT_FILTER_PARAMETERS;
import static com.google.common.base.Predicates.equalTo;

/**
 * Default implementation of {@link com.epam.ta.reportportal.core.launch.GetLaunchHandler}
 *
 * @author Aliaksei_Makayed
 * @author Andrei_Ramanchuk
 */
@Service
public class GetLaunchHandler /*extends StatisticBasedContentLoader*/ implements com.epam.ta.reportportal.core.launch.GetLaunchHandler {

	private final LaunchRepository launchRepository;
	private final LaunchTagRepository launchTagRepository;
	private final ProjectRepository projectRepository;

	public GetLaunchHandler(LaunchRepository launchRepository, LaunchTagRepository launchTagRepository,
			ProjectRepository projectRepository) {
		this.launchRepository = launchRepository;
		this.launchTagRepository = launchTagRepository;
		this.projectRepository = projectRepository;
	}

	@Override
	public LaunchResource getLaunch(Long launchId, ReportPortalUser.ProjectDetails projectDetails) {
		//TODO: fix this
		return launchRepository.findById(launchId)
				.map(TO_RESOURCE)
				.orElseThrow(() -> new ReportPortalException(ErrorType.LAUNCH_NOT_FOUND, launchId));
	}

	//		public LaunchResource getLaunchByName(String project, Pageable pageable, Filter filter, String username) {
	////			filter.addCondition(new FilterCondition(EQUALS, false, project, Launch.PROJECT));
	//	//		Page<Launch> launches = launchRepository.findByFilter(filter, pageable);
	//	//		expect(launches, notNull()).verify(LAUNCH_NOT_FOUND);
	//	//		return LaunchConverter.TO_RESOURCE.apply(launches.iterator().next());
	//			return null;
	//		}

	public Iterable<LaunchResource> getProjectLaunches(ReportPortalUser.ProjectDetails projectDetails, Filter filter, Pageable pageable,
			String userName) {
		Project project = projectRepository.findById(projectDetails.getProjectId())
				.orElseThrow(() -> new ReportPortalException(ErrorType.USER_NOT_FOUND, projectDetails.getProjectId()));
		Page<Launch> launches = launchRepository.findByFilter(ProjectFilter.of(filter, project.getName()), pageable);
		return PagedResourcesAssembler.pageConverter(LaunchConverter.TO_RESOURCE).apply(launches);
	}

	public com.epam.ta.reportportal.ws.model.Page<LaunchResource> getLatestLaunches(ReportPortalUser.ProjectDetails projectDetails,
			Filter filter, Pageable pageable) {
		Project project = projectRepository.findById(projectDetails.getProjectId())
				.orElseThrow(() -> new ReportPortalException(ErrorType.USER_NOT_FOUND, projectDetails.getProjectId()));
		Page<Launch> launches = launchRepository.findByFilter(ProjectFilter.of(filter, project.getName()), pageable);
		return PagedResourcesAssembler.pageConverter(LaunchConverter.TO_RESOURCE).apply(launches);
	}

	@Override
	public List<String> getTags(ReportPortalUser.ProjectDetails projectDetails, String value) {
		return launchTagRepository.getTags(projectDetails.getProjectId(), value);
	}

	@Override
	public List<String> getLaunchNames(ReportPortalUser.ProjectDetails projectDetails, String value) {
		expect(value.length() > 2, it -> Objects.equals(it, true)).verify(INCORRECT_FILTER_PARAMETERS,
				formattedSupplier("Length of the launch name string '{}' is less than 3 symbols", value)
		);
		return launchRepository.getLaunchNames(projectDetails.getProjectId(), value, LaunchModeEnum.DEBUG);
	}

	@Override
	public List<String> getOwners(ReportPortalUser.ProjectDetails projectDetails, String value, String mode) {
		expect(value.length() > 2, equalTo(true)).verify(INCORRECT_FILTER_PARAMETERS,
				formattedSupplier("Length of the filtering string '{}' is less than 3 symbols", value)
		);
		return launchRepository.getOwnerNames(projectDetails.getProjectId(), value, mode);
	}

//	@Override
//	public List<ComparisonStatisticsContent> getLaunchesComparisonInfo(ReportPortalUser.ProjectDetails projectDetails, Long[] ids) {
//		//@formatter:off
//		//TODO: implement after Ivan's response
////				List<String> contentFields = Arrays.stream(JIssueGroupEnum.values()).map(JIssueGroupEnum::getLiteral).collect(Collectors.toList());
////				contentFields.addAll(Lists.newArrayList(JStatusEnum.FAILED, JStatusEnum.SKIPPED, JStatusEnum.PASSED)
////						.stream()
////						.map(JStatusEnum::getLiteral)
////						.collect(Collectors.toList()));
////				Set<FilterCondition> filterConditions = new HashSet<>();
////				filterConditions.add(new FilterCondition(Condition.IN,
////						false,
////						Arrays.stream(ids).map(String::valueOf).collect(Collectors.joining(",")),
////						GeneralCriteriaConstant.PROJECT_ID
////				));
////				filterConditions.add(new FilterCondition(Condition.EQUALS, false, projectName, GeneralCriteriaConstant.PROJECT));
////				Filter filter = new Filter(Launch.class, filterConditions);
////				return comparisonContentLoader.loadContent(contentFields, filter, Collections.emptySet(), ids.length).forEach((result, values) -> {
////					((Map<Integer, Map<String, Double>>) values).forEach();
////				});
//
//		//TODO: remove old implementation
//		//				widgetContentRepository.launchesComparisonStatistics(new Filter(Launch.class, Condition.IN, ), contentFields, ids);
//		//		List<Launch> launches = launchRepository.findByIdIn(Arrays.asList(ids));
//		//		List<ChartObject> objects = new ArrayList<>(launches.size());
//		//		launches.forEach(launch -> {
//		//			ChartObject object = new ChartObject();
//		//			object.setName(launch.getName());
//		//			object.setStartTime(String.valueOf(launch.getStartTime()));
//		//			object.setNumber(String.valueOf(launch.getNumber()));
//		//			object.setId(launch.getId().toString());
//		//
//		//			EnumMap<TestItemIssueGroup, Integer> issueCounter = new EnumMap<TestItemIssueGroup, Integer>(TestItemIssueGroup.class);
//		//			launch.getIssueStatistics().forEach(issue ->
//		//					issueCounter.put(issue.getIssueType().getIssueGroup().getTestItemIssueGroup(), issue.getCounter())
//		//			);
//		//			Map<String, Integer> issuesData = ImmutableMap.<String, Integer>builder()
//		//					.put("statistics$defects$product_bug$total", issueCounter.get(TestItemIssueGroup.PRODUCT_BUG))
//		//					.put("statistics$defects$system_issue$total", issueCounter.get(TestItemIssueGroup.SYSTEM_ISSUE))
//		//					.put("statistics$defects$automation_bug$total", issueCounter.get(TestItemIssueGroup.AUTOMATION_BUG))
//		//                    .put("statistics$defects$to_investigate$total", issueCounter.get(TestItemIssueGroup.TO_INVESTIGATE))
//		//					.put("statistics$defects$no_defect$total", issueCounter.get(TestItemIssueGroup.NO_DEFECT))
//		//					.build();
//		//
//		//			Map<String, Integer> executionCounter = new HashMap<>();
//		//			launch.getExecutionStatistics().forEach(execution ->
//		//					executionCounter.put(execution.getStatus().toUpperCase(), execution.getCounter())
//		//			);
//		//			Map<String, Integer> executionData = ImmutableMap.<String, Integer>builder()
//		//					.put("statistics$executions$failed", executionCounter.get("FAILED"))
//		//					.put("statistics$executions$passed", executionCounter.get("PASSED"))
//		//					.put("statistics$executions$skipped", executionCounter.get("SKIPPED"))
//		//					.build();
//		//
//		//			Map<String, String> computedStatistics = computeFraction(issuesData);
//		//			computedStatistics.putAll(computeFraction(executionData));
//		//			object.setValues(computedStatistics);
//		//			objects.add(object);
//		//		});
//		//		return Collections.singletonMap(LoadContentStrategy.RESULT, objects);
//		//@formatter:on
//		throw new UnsupportedOperationException("Comparing is not implemented.");
//	}

	@Override
	public Map<String, String> getStatuses(ReportPortalUser.ProjectDetails projectDetails, Long[] ids) {
		return launchRepository.getStatuses(projectDetails.getProjectId(), ids);
	}

	//	private Map<String, String> computeFraction(Map<String, Integer> data) {
	//		final int total = data.values().stream().mapToInt(Integer::intValue).sum();
	//		return data.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> countPercentage(entry.getValue(), total)));
	//	}

	//	private String countPercentage(int value, int total) {
	//		if (total == 0) {
	//			return "0";
	//		}
	//		BigDecimal bigDecimal = new BigDecimal((double) value / total * 100);
	//		return bigDecimal.setScale(2, BigDecimal.ROUND_HALF_EVEN).toString();
	//	}

	//	/**
	//	 * Validate if filter doesn't contain any "mode" related conditions.
	//	 *
	//	 * @param filter
	//	 */
	//	private void validateModeConditions(Filter filter) {
	//		expect(filter.getFilterConditions().stream().anyMatch(HAS_ANY_MODE), equalTo(false))
	//				.verify(INCORRECT_FILTER_PARAMETERS, "Filters for 'mode' aren't applicable for project's launches.");
	//	}

}