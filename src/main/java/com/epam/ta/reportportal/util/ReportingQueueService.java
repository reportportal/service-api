/*
 *
 *  * Copyright 2019 EPAM Systems
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

package com.epam.ta.reportportal.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Component
public class ReportingQueueService {

	@Value("${rp.amqp.queues}")
	public int QUEUE_AMOUNT;

	/**
	 * Mapping launchId to reporting queue key.
	 * Not sure if uniform distribution will be produced, intuitively would be uniform with random UUID input.
	 * As {@link UUID#hashCode} may return negative int,
	 * take absolute value by trimming high sign bit of complement representation
	 *
	 * @param launchUuid
	 * @return
	 */
	public String getReportingQueueKey(String launchUuid) {
		int value = UUID.fromString(launchUuid).hashCode();
		value = value & 0x7fffffff;
		return String.valueOf(value % QUEUE_AMOUNT);
	}


}
