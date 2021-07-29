package com.epam.ta.reportportal.core.item.validator;

import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.ws.model.StartTestItemRQ;

public interface ParentItemValidator {

	/**
	 * Verifies if the start of a child item is allowed.
	 *
	 * @param rq     Start child item request
	 * @param parent Parent item
	 */
	void validate(StartTestItemRQ rq, TestItem parent);
}
