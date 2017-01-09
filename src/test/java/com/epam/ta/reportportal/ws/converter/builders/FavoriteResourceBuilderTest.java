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

package com.epam.ta.reportportal.ws.converter.builders;

import com.epam.ta.BaseTest;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;

import com.epam.ta.reportportal.database.entity.favorite.FavoriteResource;
import com.epam.ta.reportportal.util.LazyReference;
import com.epam.ta.reportportal.ws.model.favorites.AddFavoriteResourceRQ;
import com.epam.ta.reportportal.ws.model.favorites.FavoriteResourceTypes;

public class FavoriteResourceBuilderTest extends BaseTest {

	@Autowired
	@Qualifier("favoriteResourceBuilder.reference")
	private LazyReference<FavoriteResourceBuilder> favoriteResourceBuilderProvider;

	@Autowired
	private ApplicationContext applicationContext;

	@Test
	public void testNull() {
		FavoriteResource favoriteResource = favoriteResourceBuilderProvider.get().addCreateRQ(null).addUser(null).build();
		Assert.assertNotNull(favoriteResource);
		Assert.assertNull(favoriteResource.getResourceId());
		Assert.assertNull(favoriteResource.getResourceType());
		Assert.assertNull(favoriteResource.getUserName());
		Assert.assertNull(favoriteResource.getId());
	}

	@Test
	public void testValues() {
		AddFavoriteResourceRQ rq = new AddFavoriteResourceRQ();
		rq.setResourceId(BuilderTestsConstants.ID);
		rq.setType(FavoriteResourceTypes.DASHBOARD);
		FavoriteResource actualValue = favoriteResourceBuilderProvider.get().addCreateRQ(rq).addUser(BuilderTestsConstants.USER).build();
		Assert.assertNotNull(actualValue);
		Assert.assertEquals(BuilderTestsConstants.ID, actualValue.getResourceId());
		Assert.assertEquals(FavoriteResourceTypes.DASHBOARD.name(), actualValue.getResourceType());
		Assert.assertEquals(BuilderTestsConstants.USER, actualValue.getUserName());
		Assert.assertNull(actualValue.getId());
	}

	@Test
	public void testBeanScope() {
		Assert.assertTrue("Favorite resource  builder should be prototype bean because it's not stateless",
				applicationContext.isPrototype(applicationContext.getBeanNamesForType(FavoriteResourceBuilder.class)[0]));
	}

}