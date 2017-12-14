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

package com.epam.ta.reportportal.core.job;

import com.epam.ta.BaseTest;
import com.epam.ta.reportportal.database.dao.UserRepository;
import com.epam.ta.reportportal.database.entity.project.EntryType;
import com.epam.ta.reportportal.database.entity.user.User;
import com.epam.ta.reportportal.database.entity.user.UserRole;
import com.epam.ta.reportportal.database.entity.user.UserType;
import com.epam.ta.reportportal.database.fixture.SpringFixture;
import com.epam.ta.reportportal.database.fixture.SpringFixtureRule;
import com.epam.ta.reportportal.job.ExpireNotUsedAccountsJob;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Calendar;

import static org.hamcrest.Matchers.*;

/**
 * Tests for {@link ExpireNotUsedAccountsJob}
 *
 * @author Andrei Varabyeu
 */
@SpringFixture("authTests")
public class ExpireNotUsedAccountsJobTest extends BaseTest {

	@Rule
	@Autowired
	public SpringFixtureRule dfRule;

	@Autowired
	private ExpireNotUsedAccountsJob expireNotUsedAccountsJob;

	@Autowired
	private UserRepository userRepository;

	@Before
	public void prepareAccount() {
		User demoUser = new User();
		String random = RandomStringUtils.randomAlphabetic(5);
		demoUser.setLogin(random);
		demoUser.setPassword(random);
		demoUser.setType(UserType.UPSA);
		demoUser.setIsExpired(false);
		demoUser.setEmail(random + "epam.com");

		// YEAR Before
		demoUser.getMetaInfo().setLastLogin(DateUtils.addYears(Calendar.getInstance().getTime(), -1));
		demoUser.setRole(UserRole.ADMINISTRATOR);
		userRepository.save(demoUser);
	}

	@Test
	public void textExpirationJob() {

		expireNotUsedAccountsJob.execute(null);

		Page<User> user = userRepository.findByTypeAndIsExpired(EntryType.UPSA, true, new PageRequest(0, 1));
		Assert.assertThat(user, not(emptyIterable()));
		Assert.assertThat(user.getContent().get(0).getIsExpired(), is(true));
	}
}
