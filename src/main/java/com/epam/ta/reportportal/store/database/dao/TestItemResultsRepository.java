/*
 *
 *  * Copyright (C) 2018 EPAM Systems
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.epam.ta.reportportal.store.database.dao;

import com.epam.ta.reportportal.store.database.entity.item.TestItemResults;
import com.epam.ta.reportportal.store.database.entity.launch.StatisticEntry;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @author Dzianis_Shybeka
 */
public interface TestItemResultsRepository extends Repository<TestItemResults, Long> {

	@Query(value = "select count(issue_group) as count, issue_group as name "
				+ " 	from issue i join issue_type it on i.issue_type = it.id"
				+ " where issue_id = :itemId"
				+ " group by issue_group", nativeQuery = true)
	List<StatisticEntry> issueCounter(@Param("itemId") Long itemId);

	@Query(value = "select count(status) as count, status as name from test_item_results where item_id = :itemId group by status", nativeQuery = true)
	List<StatisticEntry> executionCounter(@Param("itemId") Long itemId);
}
