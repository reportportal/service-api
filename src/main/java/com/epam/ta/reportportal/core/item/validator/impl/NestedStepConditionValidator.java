package com.epam.ta.reportportal.core.item.validator.impl;

import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.item.validator.ParentItemValidator;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.ws.model.StartTestItemRQ;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Service;

import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.ws.model.ErrorType.BAD_REQUEST_ERROR;

@Service
public class NestedStepConditionValidator implements ParentItemValidator, Ordered {
	@Override
	public void validate(StartTestItemRQ rq, TestItem parent) {
		if (!parent.isHasStats()) {
			expect(rq.isHasStats(), equalTo(Boolean.FALSE)).verify(BAD_REQUEST_ERROR,
					Suppliers.formattedSupplier("Unable to add a not nested step item, because parent item with ID = '{}' is a nested step",
							parent.getItemId()
					)
							.get()
			);
		}
	}

	@Override
	public int getOrder() {
		return 1;
	}
}
