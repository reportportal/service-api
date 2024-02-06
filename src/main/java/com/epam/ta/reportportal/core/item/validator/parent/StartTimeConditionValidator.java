package com.epam.ta.reportportal.core.item.validator.parent;

import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.ws.model.ErrorType.CHILD_START_TIME_EARLIER_THAN_PARENT;

import com.epam.ta.reportportal.commons.Preconditions;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.ws.model.StartTestItemRQ;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Service;

@Service
public class StartTimeConditionValidator implements ParentItemValidator, Ordered {

  @Override
  public void validate(StartTestItemRQ rq, TestItem parent) {
    expect(rq.getStartTime(), Preconditions.sameLocalDateTimeOrLater(parent.getStartTime())).verify(
        CHILD_START_TIME_EARLIER_THAN_PARENT,
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
