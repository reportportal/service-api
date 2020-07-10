package com.epam.ta.reportportal.core.item.impl.retry;

import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;

import javax.annotation.Nullable;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public interface RetriesHandler {

	void handleRetries(Launch launch, TestItem newRetryParent, @Nullable String previousParent);
}
