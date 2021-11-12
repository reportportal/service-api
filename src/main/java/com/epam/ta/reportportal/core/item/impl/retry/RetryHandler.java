package com.epam.ta.reportportal.core.item.impl.retry;

import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.jooq.enums.JStatusEnum;

import java.time.LocalDateTime;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public interface RetryHandler {

	void handleRetries(Launch launch, TestItem newRetryParent, Long previousParent);

	void finishRetries(Long retryParentId, JStatusEnum status, LocalDateTime endTime);
}
