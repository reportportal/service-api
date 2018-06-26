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

package com.epam.ta.reportportal.store.database.entity.item.issue;

import com.epam.ta.reportportal.store.database.entity.enums.TestItemIssueGroup;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author Pavel Bortnik
 */
@Entity
@Table(name = "issue_group", schema = "public")
public class IssueGroup implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "issue_gorup_id")
	private Integer id;

	@Column(name = "issue_group")
	@Enumerated(EnumType.STRING)
	@Type(type = "pqsql_enum")
	private TestItemIssueGroup testItemIssueGroup;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public TestItemIssueGroup getTestItemIssueGroup() {
		return testItemIssueGroup;
	}

	public void setTestItemIssueGroup(TestItemIssueGroup testItemIssueGroup) {
		this.testItemIssueGroup = testItemIssueGroup;
	}
}
