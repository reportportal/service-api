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

package com.epam.ta.reportportal.database.dao;

import com.epam.ta.reportportal.database.entity.Log;
import com.epam.ta.reportportal.database.entity.LogLevel;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

import static org.hamcrest.Matchers.*;

/**
 * Several tests to be sure converter for {@link LogLevel} works correctly
 *
 * @author Andrei Varabyeu
 */
public class LogLevelConverterTest extends BaseDaoContextTest {

	@Autowired
	private MongoOperations mongoOperations;

	@Test
	public void saveLogWithLevel() {
		Log log = new Log();
		log.setLevel(LogLevel.INFO);
		mongoOperations.save(log);

		// List<Log> logs = mongoOperations.findAll(Log.class);
		final Log id = mongoOperations.findOne(new Query().addCriteria(Criteria.where("_id").is(log.getId())), Log.class);
		Assert.assertNotNull(id);
		Assert.assertThat(id.getLevel(), is(LogLevel.INFO));
	}

	@Test
	public void saveLogWithNoLevel() {
		Log log = new Log();
		mongoOperations.save(log);

		final Log log1 = mongoOperations.findOne(new Query().addCriteria(Criteria.where("_id").is(log.getId())), Log.class);
		Assert.assertNotNull(log1);
		Assert.assertThat(log1.getLevel(), is(nullValue()));
	}

	@Test
	public void testComparasion() {
		Log log1 = new Log();
		log1.setLevel(LogLevel.ERROR);
		mongoOperations.save(log1);

		Log log2 = new Log();
		log2.setLevel(LogLevel.DEBUG);
		mongoOperations.save(log2);

		Query q = Query.query(Criteria.where("level").gt(LogLevel.INFO));

		List<Log> logs = mongoOperations.find(q, Log.class);
		Assert.assertThat(logs, is(not(empty())));
		Assert.assertThat(logs.size(), is(1));
		Assert.assertThat(logs.get(0).getLevel(), is(LogLevel.ERROR));
	}
}