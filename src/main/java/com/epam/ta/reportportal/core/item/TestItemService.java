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
			TestItem retryParent = testItemRepository.findById(testItem.getRetryOf())
					.orElseThrow(() -> new ReportPortalException(ErrorType.TEST_ITEM_NOT_FOUND, testItem.getRetryOf()));

			return ofNullable(retryParent.getLaunch()).orElseGet(() -> ofNullable(retryParent.getParent()).map(TestItem::getLaunch)
					.orElseThrow(() -> new ReportPortalException(ErrorType.LAUNCH_NOT_FOUND)));
		} else {
			return ofNullable(testItem.getLaunch()).orElseGet(() -> ofNullable(testItem.getParent()).map(TestItem::getLaunch)
					.orElseThrow(() -> new ReportPortalException(ErrorType.LAUNCH_NOT_FOUND)));
		}
	}

}
