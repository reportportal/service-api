package com.epam.ta.reportportal.core.item.impl.retry;

import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;

import java.util.Optional;

public interface RetrySearcher {

	Optional<Long> findPreviousRetry(Launch launch, TestItem newItem, TestItem parentItem);

}
