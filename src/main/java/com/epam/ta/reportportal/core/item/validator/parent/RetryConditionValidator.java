package com.epam.ta.reportportal.core.item.validator.parent;

import static com.epam.ta.reportportal.commons.Predicates.isNull;
import static com.epam.reportportal.rules.commons.validation.BusinessRule.expect;
import static com.epam.reportportal.rules.exception.ErrorType.UNABLE_TO_SAVE_CHILD_ITEM_FOR_THE_RETRY;

import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.ws.reporting.StartTestItemRQ;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Service;

@Service
public class RetryConditionValidator implements ParentItemValidator, Ordered {

  @Override
  public void validate(StartTestItemRQ rq, TestItem parent) {
    if (rq.isHasStats()) {
      expect(parent.getRetryOf(), isNull()::test).verify(UNABLE_TO_SAVE_CHILD_ITEM_FOR_THE_RETRY,
          parent.getItemId());
    }
  }

  @Override
  public int getOrder() {
    return 2;
  }
}
