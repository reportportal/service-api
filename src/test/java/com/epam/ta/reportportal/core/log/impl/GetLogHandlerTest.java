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
package com.epam.ta.reportportal.core.log.impl;

import com.epam.ta.BaseTest;
import com.epam.ta.reportportal.database.dao.LaunchRepository;
import com.epam.ta.reportportal.database.dao.LogRepository;
import com.epam.ta.reportportal.database.dao.TestItemRepository;
import com.epam.ta.reportportal.database.entity.Log;
import com.epam.ta.reportportal.database.entity.LogLevel;
import com.epam.ta.reportportal.database.search.Condition;
import com.epam.ta.reportportal.database.search.Filter;
import com.epam.ta.reportportal.database.search.FilterCondition;
import com.epam.ta.reportportal.ws.converter.LogResourceAssembler;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import javax.inject.Inject;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Andrei Varabyeu
 */
@Ignore
//ignore until fongo supports all aggregation methods
public class GetLogHandlerTest extends BaseTest {

	public static final String ITEM_ID = RandomStringUtils.randomAlphabetic(10);
	public static final String PROJECT_ID = "test-project";
	public static final int ITEMS_COUNT = 10;

	@Inject
	private LogRepository logRepository;

	@Test
	public void testGetLogFirstPage() {
		final List<Log> logs = generateLogs(logRepository);
		//first log
		final Log logToFind = logs.get(0);
		long pageNumber = prepareHandler().getPageNumber(logToFind.getId(), PROJECT_ID, Filter.builder()
						.withCondition(FilterCondition.builder()
								.withCondition(Condition.EQUALS)
								.withSearchCriteria("item")
								.withValue(ITEM_ID)
								.build())
						.withTarget(Log.class)
						.build(),

				new PageRequest(0, 2)
		);

		Assert.assertThat(pageNumber, Matchers.equalTo(1L));
	}

	@Test
	public void testGetLogPageNumberAsc() {
		final List<Log> logs = generateLogs(logRepository);
		final Log logToFind = logs.get(3);
		long pageNumber = prepareHandler().getPageNumber(logToFind.getId(), PROJECT_ID, Filter.builder()
						.withCondition(FilterCondition.builder()
								.withCondition(Condition.EQUALS)
								.withSearchCriteria("item")
								.withValue(ITEM_ID)
								.build())
						.withTarget(Log.class)
						.build(),

				new PageRequest(0, 2)
		);

		Assert.assertThat(pageNumber, Matchers.equalTo(2L));
	}

	@Test
	public void testGetLogPageNumberDesc() {
		final PageRequest pageRequest = new PageRequest(0, 2, new Sort(Sort.Direction.DESC, "logTime"));

		final List<Log> logs = generateLogs(logRepository);
		final Log logToFind = logs.get(9);

		long pageNumber = prepareHandler().getPageNumber(logToFind.getId(), PROJECT_ID, Filter.builder()
						.withCondition(FilterCondition.builder()
								.withCondition(Condition.EQUALS)
								.withSearchCriteria("item")
								.withValue(ITEM_ID)
								.build())
						.withTarget(Log.class)
						.build(),

				pageRequest
		);

		Assert.assertThat(pageNumber, Matchers.equalTo(1L));
	}

	private GetLogHandler prepareHandler() {

		final String userId = "user1";
		final GetLogHandler getLogHandler = new GetLogHandler();

		final LaunchRepository launchRepository = DeleteLogHandlerTest.launchRepositoryMock("one", PROJECT_ID, userId);
		getLogHandler.setLaunchRepository(launchRepository);

		final TestItemRepository testItemRepository = DeleteLogHandlerTest.itemRepositoryMock("someID", "one", ITEM_ID);
		getLogHandler.setTestItemRepository(testItemRepository);

		getLogHandler.setLogRepository(logRepository);

		final LogResourceAssembler logResourceAssembler = new LogResourceAssembler();

		getLogHandler.setLogResourceAssembler(logResourceAssembler);
		return getLogHandler;

	}

	private List<Log> generateLogs(LogRepository logRepository) {
		return IntStream.range(0, ITEMS_COUNT).mapToObj(i -> {
			Log log = new Log();
			log.setLevel(LogLevel.ERROR);
			log.setLogMsg(RandomStringUtils.random(10));
			log.setTestItemRef(ITEM_ID);

			final Date now = Calendar.getInstance().getTime();
			log.setLastModified(now);
			log.setLogTime(now);
			logRepository.save(log);

			return log;
		}).collect(Collectors.toList());

	}

	@After
	public void cleanLogs() {
		logRepository.deleteAll();
	}

}
