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

package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.database.entity.user.UserCreationBid;
import com.epam.ta.reportportal.ws.model.user.CreateUserRQ;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Pavel_Bortnik
 */
public class UserCreationBidConverterTest {

	@Test(expected = NullPointerException.class)
	public void testNull() {
		UserCreationBidConverter.TO_USER.apply(null);
	}

	@Test
	public void testConvert() {
		CreateUserRQ rq = new CreateUserRQ();
		rq.setDefaultProject("default_personal");
		rq.setEmail("email@email.com");
		rq.setRole("USER");
		UserCreationBid bid = UserCreationBidConverter.TO_USER.apply(rq);

		Assert.assertEquals(bid.getDefaultProject(), rq.getDefaultProject());
		Assert.assertEquals(bid.getEmail(), rq.getEmail());
		Assert.assertEquals(bid.getRole(), rq.getRole());
		Assert.assertNotNull(bid.getId());
	}

}