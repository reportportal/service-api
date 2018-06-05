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

import com.epam.ta.reportportal.store.database.entity.IdToNameProjection;
import com.epam.ta.reportportal.store.database.entity.item.TestItem;
import com.epam.ta.reportportal.store.database.entity.item.TestItemStructure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * @author Dzianis_Shybeka
 */
public interface TestItemStructureRepository extends JpaRepository<TestItemStructure, Long> {

	@Query(value = "select tis.item_id from test_item_structure tis where tis.parent_id = :parentId limit 1", nativeQuery = true)
	Optional<Long> findTopItemIdByParentId(@Param("parentId") Long parentItemId);

	/**
	 * Return list of [id, name only] representing structure of test item starting from the root.
	 *
	 * @param itemId
	 * @return
	 */
	@Query(value = "WITH RECURSIVE item_structure(parent_id, item_id, name) AS (\n"
			+ "    SELECT parent_id, ti.item_id, ti.name"
			+ "      FROM test_item_structure tis join test_item ti on tis.item_id = ti.item_id"
			+ "    WHERE ti.item_id=:itemId \n"
			+ "    UNION ALL\n"
			+ "    SELECT tis.parent_id, tis.item_id, ti.name\n"
			+ "      FROM item_structure tis_r, test_item_structure tis join test_item ti on tis.item_id = ti.item_id\n"
			+ "    WHERE tis.item_id = tis_r.parent_id)\n"
			+ "    SELECT item_id as id, name FROM item_structure", nativeQuery = true)
	List<IdToNameProjection> findPath(@Param("itemId") Long itemId);

	@Query("select ti from TestItem ti join TestItemStructure tis on ti.itemId = tis.itemId where tis.retryOf = :itemId")
	List<TestItem> findRetriesOf(@Param("itemId") Long itemId);
}
