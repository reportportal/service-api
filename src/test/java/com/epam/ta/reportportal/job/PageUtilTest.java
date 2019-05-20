/*
 * Copyright 2018 EPAM Systems
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
package com.epam.ta.reportportal.job;

import com.epam.ta.reportportal.database.dao.BaseDaoContextTest;
import com.epam.ta.reportportal.database.dao.LogRepository;
import com.epam.ta.reportportal.database.entity.Log;
import com.epam.ta.reportportal.database.fixture.SpringFixture;
import com.epam.ta.reportportal.database.fixture.SpringFixtureRule;
import org.assertj.core.api.Assertions;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SpringFixture("logRepositoryTests")
public class PageUtilTest extends BaseDaoContextTest {

	@Rule
	@Autowired
	public SpringFixtureRule dfRule;

	@Autowired
	private LogRepository logRepository;

	@Test
	public void testIterateOverPages() {
		List<Log> logs = logRepository.findAll();
		List<Log> logs2 = new ArrayList<>();
		PageUtil.iterateOverPages(pageable -> logRepository.findAll(pageable), logs2::addAll);
		Assertions.assertThat(logs2).isEqualTo(logs);
	}

	@Test
	public void testDeleteOverPages() {
		PageUtil.deleteOverPages(2, pageable -> logRepository.findAll(pageable), it -> logRepository.delete(it));
		Assertions.assertThat(logRepository.findAll()).isEmpty();
	}

}