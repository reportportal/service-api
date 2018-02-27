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

package com.epam.ta.reportportal.store.database.entity.enums;

import org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import java.util.Optional;

/**
 * List of supported external systems
 *
 * @author Andrei Varabyeu
 */
public enum ExternalSystemType {

	NONE {
		@Override
		public String makeUrl(String base, String id) {
			return null;
		}
	},
	JIRA {
		@Override
		public String makeUrl(String base, String id) {
			return StringUtils.stripEnd(base, "/") + "/browse/" + id;
		}
	},
	TFS {
		@Override
		public String makeUrl(String base, String id) {
			return StringUtils.stripEnd(base, "/") + "/browse/" + id;
		}
	},
	RALLY {
		@Override
		public String makeUrl(String base, String id) {
			return "";
		}
	};

	public static final String ISSUE_MARKER = "#";

	public abstract String makeUrl(String base, String id);

	ExternalSystemType() {

	}

	public static Optional<String> knownIssue(String summary) {
		if (summary.trim().startsWith(ISSUE_MARKER)) {
			return Optional.of(StringUtils.substringAfter(summary, ISSUE_MARKER));
		} else {
			return Optional.empty();
		}
	}

	public static Optional<ExternalSystemType> findByName(String name) {
		return Arrays.stream(ExternalSystemType.values()).filter(type -> type.name().equalsIgnoreCase(name)).findAny();
	}

	public static boolean isPresent(String name) {
		return findByName(name).isPresent();
	}
}