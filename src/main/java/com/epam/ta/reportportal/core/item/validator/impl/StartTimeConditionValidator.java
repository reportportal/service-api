package com.epam.ta.reportportal.core.item.validator.impl;

import com.epam.ta.reportportal.commons.Preconditions;
import com.epam.ta.reportportal.core.item.validator.ParentItemValidator;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.ws.model.StartTestItemRQ;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Service;

import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.ws.model.ErrorType.CHILD_START_TIME_EARLIER_THAN_PARENT;

@Service
public class StartTimeConditionValidator implements ParentItemValidator, Ordered {
	@Override
	public void validate(StartTestItemRQ rq, TestItem parent) {
		expect(rq.getStartTime(), Preconditions.sameTimeOrLater(parent.getStartTime())).verify(CHILD_START_TIME_EARLIER_THAN_PARENT,
				rq.getStartTime(),
				parent.getStartTime(),
				parent.getItemId()
		);
	}

	@Override
	public int getOrder() {
		return 3;
	}
}
