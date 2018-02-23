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

package com.epam.ta.reportportal.store.database.entity.launch;

import java.util.Objects;

/**
 * @author Pavel Bortnik
 */
public class ExecutionStatistics {

	private Integer total;

	private Integer passed;

	private Integer failed;

	private Integer skipped;

	public Integer getTotal() {
		return total;
	}

	public void setTotal(Integer total) {
		this.total = total;
	}

	public Integer getPassed() {
		return passed;
	}

	public void setPassed(Integer passed) {
		this.passed = passed;
	}

	public Integer getFailed() {
		return failed;
	}

	public void setFailed(Integer failed) {
		this.failed = failed;
	}

	public Integer getSkipped() {
		return skipped;
	}

	public void setSkipped(Integer skipped) {
		this.skipped = skipped;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		ExecutionStatistics that = (ExecutionStatistics) o;
		return Objects.equals(total, that.total) && Objects.equals(passed, that.passed) && Objects.equals(failed, that.failed)
				&& Objects.equals(skipped, that.skipped);
	}

	@Override
	public int hashCode() {
		return Objects.hash(total, passed, failed, skipped);
	}

	@Override
	public String toString() {
		return "ExecutionStatistics{" + "total=" + total + ", passed=" + passed + ", failed=" + failed + ", skipped=" + skipped + '}';
	}
}
