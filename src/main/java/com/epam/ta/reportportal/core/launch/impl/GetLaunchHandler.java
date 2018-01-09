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

import com.epam.ta.reportportal.core.launch.IGetLaunchHandler;
import com.epam.ta.reportportal.core.widget.content.StatisticBasedContentLoader;
import com.epam.ta.reportportal.database.dao.LaunchRepository;
import com.epam.ta.reportportal.database.dao.ProjectRepository;
import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.database.entity.ProjectRole;
import com.epam.ta.reportportal.database.entity.statistics.ExecutionCounter;
import com.epam.ta.reportportal.database.entity.statistics.IssueCounter;
import com.epam.ta.reportportal.database.search.Filter;
import com.epam.ta.reportportal.database.search.FilterCondition;
import com.epam.ta.reportportal.ws.converter.PagedResourcesAssembler;
import com.epam.ta.reportportal.ws.converter.converters.LaunchConverter;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.launch.LaunchResource;
import com.epam.ta.reportportal.ws.model.launch.Mode;
import com.epam.ta.reportportal.ws.model.widget.ChartObject;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.commons.Preconditions.HAS_ANY_MODE;
import static com.epam.ta.reportportal.commons.Predicates.*;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.commons.validation.Suppliers.formattedSupplier;
import static com.epam.ta.reportportal.database.entity.project.ProjectUtils.findUserConfigByLogin;
import static com.epam.ta.reportportal.database.search.Condition.EQUALS;
import static com.epam.ta.reportportal.ws.model.ErrorType.*;
import static com.epam.ta.reportportal.ws.model.launch.Mode.DEBUG;
import static com.epam.ta.reportportal.ws.model.launch.Mode.DEFAULT;

/**
 * Default implementation of {@link IGetLaunchHandler}
 *
 * @author Aliaksei_Makayed
 * @author Andrei_Ramanchuk
 */
@Service
public class GetLaunchHandler extends StatisticBasedContentLoader implements IGetLaunchHandler {

	private ProjectRepository projectRepository;
	private final LaunchRepository launchRepository;

	@Autowired
	public GetLaunchHandler(LaunchRepository launchRepository) {
		this.launchRepository = Preconditions.checkNotNull(launchRepository);
	}

	@Autowired
	public void setProjectRepository(ProjectRepository projectRepository) {
		this.projectRepository = projectRepository;
	}

	@Override
	public LaunchResource getLaunch(String launchId, String userName, String projectName) {
		Launch launch = validate(launchId, projectName);
		if (launch.getMode() == DEBUG) {
			Project project = projectRepository.findOne(projectName);
			final Project.UserConfig userConfig = findUserConfigByLogin(project, userName);
			expect(userConfig.getProjectRole(), not(equalTo(ProjectRole.CUSTOMER))).verify(ACCESS_DENIED);
		}
		return LaunchConverter.TO_RESOURCE.apply(launch);
	}

	@Override
	public LaunchResource getLaunchByName(String project, Pageable pageable, Filter filter, String username) {
		filter.addCondition(new FilterCondition(EQUALS, false, project, Launch.PROJECT));
		Page<Launch> launches = launchRepository.findByFilter(filter, pageable);
		expect(launches, notNull()).verify(LAUNCH_NOT_FOUND);
		return LaunchConverter.TO_RESOURCE.apply(launches.iterator().next());
	}

	@Override
	public Iterable<LaunchResource> getProjectLaunches(String projectName, Filter filter, Pageable pageable, String userName) {
		/*
		 * input filter shouldn't contains any "mode" related filter conditions
		 * "debug mode" conditions are forbidden because user can manipulate
		 * with "debug mode" input filter and see not own debug launches
		 * 'default mode' conditions are forbidden because 'default mode'
		 * condition will be added by server(line 66) Due to limitations of the
		 * com.mongodb.BasicDBObject, user can't add a second 'mode' criteria
		 */
		validateModeConditions(filter);
		filter = addLaunchCommonCriteria(DEFAULT, filter, projectName);
		Page<Launch> launches = launchRepository.findByFilter(filter, pageable);
		return PagedResourcesAssembler.pageConverter(LaunchConverter.TO_RESOURCE).apply(launches);
	}

	/*
	 * Changed logic for this method: It should return DEBUG launches for
	 * project users, for specified user or only owner
	 */
	@Override
	public Iterable<LaunchResource> getDebugLaunches(String projectName, String userName, Filter filter, Pageable pageable) {
		filter = addLaunchCommonCriteria(DEBUG, filter, projectName);
		Page<Launch> launches = launchRepository.findByFilter(filter, pageable);
		return PagedResourcesAssembler.pageConverter(LaunchConverter.TO_RESOURCE).apply(launches);
	}

	@Override
	public com.epam.ta.reportportal.ws.model.Page<LaunchResource> getLatestLaunches(String projectName, Filter filter, Pageable pageable) {
		validateModeConditions(filter);
		addLaunchCommonCriteria(DEFAULT, filter, projectName);
		Page<LaunchResource> resources = launchRepository.findLatestLaunches(filter, pageable).map(LaunchConverter.TO_RESOURCE::apply);
		return new com.epam.ta.reportportal.ws.model.Page<>(resources.getContent(), resources.getSize(), resources.getNumber() + 1,
				resources.getTotalElements(), resources.getTotalPages()
		);
	}

	@Override
	public List<String> getTags(String project, String value) {
		return launchRepository.findDistinctValues(project, value, "tags");
	}

	@Override
	public List<String> getLaunchNames(String project, String value) {
		expect(value.length() > 2, equalTo(true)).verify(INCORRECT_FILTER_PARAMETERS,
				formattedSupplier("Length of the launch name string '{}' is less than 3 symbols", value)
		);
		return launchRepository.findValuesWithMode(project, value, "name", DEFAULT.name());
	}

	@Override
	public List<String> getOwners(String project, String value, String field, String mode) {
		expect(value.length() > 2, equalTo(true)).verify(INCORRECT_FILTER_PARAMETERS,
				formattedSupplier("Length of the filtering string '{}' is less than 3 symbols", value)
		);
		return launchRepository.findValuesWithMode(project, value, field, mode);
	}

	@Override
	public Map<String, List<ChartObject>> getLaunchesComparisonInfo(String projectName, String[] ids) {
		//@formatter:off
		List<Launch> launches = launchRepository.find(Arrays.asList(ids));
		List<ChartObject> objects = new ArrayList<>(launches.size());
		launches.forEach(launch -> {
			ChartObject object = new ChartObject();
			object.setName(launch.getName());
			object.setStartTime(String.valueOf(launch.getStartTime().getTime()));
			object.setNumber(String.valueOf(launch.getNumber()));
			object.setId(launch.getId());

			IssueCounter issueCounter = launch.getStatistics().getIssueCounter();
			Map<String, Integer> issuesData = ImmutableMap.<String, Integer>builder()
					.put("statistics$defects$product_bug$total", issueCounter.getProductBugTotal())
					.put("statistics$defects$system_issue$total", issueCounter.getSystemIssueTotal())
					.put("statistics$defects$automation_bug$total", issueCounter.getAutomationBugTotal())
					.put("statistics$defects$to_investigate$total", issueCounter.getToInvestigateTotal())
					.put("statistics$defects$no_defect$total", issueCounter.getNoDefectTotal())
					.build();

			ExecutionCounter executionCounter = launch.getStatistics().getExecutionCounter();
			Map<String, Integer> executionData = ImmutableMap.<String, Integer>builder()
					.put("statistics$executions$failed", executionCounter.getFailed())
					.put("statistics$executions$passed", executionCounter.getPassed())
					.put("statistics$executions$skipped", executionCounter.getSkipped())
					.build();

			Map<String, String> computedStatistics = computeFraction(issuesData);
			computedStatistics.putAll(computeFraction(executionData));
			object.setValues(computedStatistics);
			objects.add(object);
		});
		return Collections.singletonMap(RESULT, objects);
		//@formatter:off
	}

	@Override
	public Map<String, String> getStatuses(String projectName, String[] ids) {
		return launchRepository.find(Arrays.asList(ids)).stream().filter(launch -> launch.getProjectRef().equals(projectName))
				.collect(Collectors.toMap(Launch::getId, launch -> launch.getStatus().toString()));
	}

	/**
	 * Add to filter project and mode criteria
	 *
	 * @param filter Filter to update
	 * @return Updated filter
	 */
	private Filter addLaunchCommonCriteria(Mode mode, Filter filter, String projectName) {
		if (null != filter) {
			filter.addCondition(new FilterCondition(EQUALS, false, mode.toString(), Launch.MODE_CRITERIA));
			filter.addCondition(new FilterCondition(EQUALS, false, projectName, Project.PROJECT));
		}
		return filter;
	}

    private Map<String, String> computeFraction(Map<String, Integer> data) {
		final int total = data.values().stream().mapToInt(Integer::intValue).sum();
		return data.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> countPercentage(entry.getValue(), total)));
	}

	private String countPercentage(int value, int total) {
		if (total == 0) {
			return "0";
		}
		BigDecimal bigDecimal = new BigDecimal((double) value / total * 100);
		return bigDecimal.setScale(2, BigDecimal.ROUND_HALF_EVEN).toString();
	}

	/**
	 * Validate if filter doesn't contain any "mode" related conditions.
	 *
	 * @param filter
	 */
	private void validateModeConditions(Filter filter) {
		expect(filter.getFilterConditions().stream().anyMatch(HAS_ANY_MODE), equalTo(false))
				.verify(INCORRECT_FILTER_PARAMETERS, "Filters for 'mode' aren't applicable for project's launches.");
	}

	/**
	 * Validate launch reference to specified project ID
	 *
	 * @param launchId
	 *            - validating launch ID
	 * @param projectName
	 *            - specified project name
	 * @return Launch - validated launch object if not BusinessRule exceptions
	 */
	private Launch validate(String launchId, String projectName) {
		Launch launch = launchRepository.findOne(launchId);
		expect(launch, notNull()).verify(LAUNCH_NOT_FOUND, launchId);

		expect(launch.getProjectRef(), equalTo(projectName)).verify(ErrorType.FORBIDDEN_OPERATION,
				formattedSupplier("Specified launch with id '{}' not referenced to specified project '{}'", launchId, projectName));

		return launch;
	}
}