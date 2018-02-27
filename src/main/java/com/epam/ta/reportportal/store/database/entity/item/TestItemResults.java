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

package com.epam.ta.reportportal.store.database.entity.item;

import com.epam.ta.reportportal.store.database.entity.enums.StatusEnum;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author Pavel Bortnik
 */
@Entity
@Table(name = "test_item_results", schema = "public", indexes = {
		@Index(name = "test_item_results_pk", unique = true, columnList = "item_id ASC") })
public class TestItemResults implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "item_id", unique = true, nullable = false, precision = 64)
	private Long itemId;

	@Column(name = "status", nullable = false)
	private StatusEnum status;

	@Column(name = "duration", precision = 24)
	private Float duration;

	public TestItemResults() {
	}

	public Long getItemId() {
		return itemId;
	}

	public void setItemId(Long itemId) {
		this.itemId = itemId;
	}

	public StatusEnum getStatus() {
		return status;
	}

	public void setStatus(StatusEnum status) {
		this.status = status;
	}

	public Float getDuration() {
		return duration;
	}

	public void setDuration(Float duration) {
		this.duration = duration;
	}

}
