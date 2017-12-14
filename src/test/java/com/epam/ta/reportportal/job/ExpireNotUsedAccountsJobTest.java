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

package com.epam.ta.reportportal.job;

import com.epam.ta.reportportal.auth.ExpirationPolicy;
import com.epam.ta.reportportal.database.dao.UserRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Date;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Created by Andrey_Ivanov1 on 01-Jun-17.
 */
@RunWith(MockitoJUnitRunner.class)
public class ExpireNotUsedAccountsJobTest {

	@InjectMocks
	private ExpireNotUsedAccountsJob expireNotUsedAccountsJob = new ExpireNotUsedAccountsJob();
	@Mock
	private ExpirationPolicy expirationPolicy;
	@Mock
	private UserRepository userRepository;

	@Test
	public void runTest() {
		expireNotUsedAccountsJob.execute(null);
		verify(expirationPolicy, times(1)).getExpirationDate();
		verify(userRepository, times(1)).expireUsersLoggedOlderThan(any(Date.class));

	}

}
