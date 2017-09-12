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

package com.epam.ta.reportportal.core.launch.meta;

import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.apache.commons.collections.CollectionUtils;

import javax.validation.constraints.NotNull;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Utils for launch metadata.
 *
 * @author Pavel Bortnik
 */
public class LaunchMetadataUtil {

	private static final String BUILD_REGEX = "^build:[1-9][0-9]*|0&";

	private static final int BUILD_TAGS_COUNT = 1;

	/**
	 * Since we have cumulative trend chart there was build tag introduced.
	 * Add build number as metadata if launch contain tag in 'build:{number}' format.
	 *
	 * @param launch
	 * @return
	 */
	public static Launch addBuildNumber(Launch launch) {
		if (doesContainBuildTag(launch.getTags())) {
			Long buildNumber = extract(launch.getTags());
			launch.setMetadata(new Launch.Metadata(buildNumber));
		}
		return launch;
	}

	private static boolean doesContainBuildTag(Set<String> tags) {
		return !CollectionUtils.isEmpty(tags) && tags.stream().anyMatch(it -> it.matches(BUILD_REGEX));
	}

	private static Long extract(@NotNull Set<String> tags) {
		Set<String> buildTags = tags.stream().filter(it -> it.matches(BUILD_REGEX)).collect(Collectors.toSet());
		BusinessRule.expect(buildTags.size(), Predicate.isEqual(BUILD_TAGS_COUNT))
				.verify(ErrorType.INCORRECT_REQUEST, "Could be only one build tag");
		String buildTag = buildTags.iterator().next();
		String number = buildTag.split(":")[1];
		return Long.valueOf(number);
	}

}
