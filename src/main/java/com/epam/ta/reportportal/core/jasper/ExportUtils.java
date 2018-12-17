/*
 * Copyright 2018 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.core.jasper;

import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.statistics.Statistics;
import org.apache.commons.lang3.StringUtils;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
class ExportUtils {
	private static final String SHIFT_PREFIX = "    ";
	static final String COMMENT_PREFIX = "\r\n" + " DEFECT COMMENT: ";
	static final String DESCRIPTION_PREFIX = "\r\n" + " ITEM DESCRIPTION: ";

	static int getStatisticsCounter(Set<Statistics> statistics, String statisticsFieldName) {
		return statistics.stream()
				.filter(it -> it.getStatisticsField().getName().equals(statisticsFieldName))
				.mapToInt(Statistics::getCounter)
				.findAny()
				.orElse(0);
	}

	/**
	 * Add right shifting for child items depends on depth level
	 *
	 * @param input - target {@see TestItem}
	 * @return String - updated test item name with shifted name
	 */
	static String adjustName(TestItem input) {
		/* Sync buffer instead builder! */
		return new StringBuilder(StringUtils.repeat(SHIFT_PREFIX, input.getPath().split("\\.").length)).append(input.getName()).toString();
	}

	/**
	 * Format launch duration from long to human readable format.
	 *
	 * @param duration - input duration
	 * @return String - formatted output
	 */
	static String durationToShortDHMS(Duration duration) {
		long days = duration.toDays();
		long hours = duration.toHours() - TimeUnit.DAYS.toHours(days);
		long minutes = duration.toMinutes() - TimeUnit.HOURS.toMinutes(hours);
		long seconds = duration.getSeconds() - TimeUnit.MINUTES.toSeconds(minutes);
		return days == 0 ?
				String.format("%02d:%02d:%02d", hours, minutes, seconds) :
				String.format("%dd%02d:%02d:%02d", days, hours, minutes, seconds);
	}
}
