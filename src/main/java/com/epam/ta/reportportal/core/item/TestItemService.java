/*
 * Copyright 2019 EPAM Systems
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

package com.epam.ta.reportportal.core.item;

import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static java.util.Optional.ofNullable;

/**
 * @author Konstantin Antipin
 */
@Service
public class TestItemService {

	@Autowired
	TestItemRepository testItemRepository;

	public Launch getEffectiveLaunch(TestItem testItem) {

		if (ofNullable(testItem.getRetryOf()).isPresent()) {
			TestItem retryParent = testItemRepository.findById(testItem.getRetryOf()).orElseThrow(() -> new ReportPortalException(ErrorType.TEST_ITEM_NOT_FOUND, testItem.getRetryOf()));

			return ofNullable(retryParent.getLaunch()).orElseGet(() -> ofNullable(retryParent.getParent()).map(TestItem::getLaunch).orElseThrow(() -> new ReportPortalException(ErrorType.LAUNCH_NOT_FOUND)));
		} else {
			return ofNullable(testItem.getLaunch()).orElseGet(() -> ofNullable(testItem.getParent()).map(TestItem::getLaunch).orElseThrow(() -> new ReportPortalException(ErrorType.LAUNCH_NOT_FOUND)));
		}
	}

}
