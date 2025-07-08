package com.epam.ta.reportportal.core.item.validator;

import static com.epam.reportportal.rules.commons.validation.BusinessRule.expect;
import static com.epam.reportportal.rules.exception.ErrorType.TEST_ITEM_NOT_FOUND;
import static com.epam.ta.reportportal.commons.Predicates.equalTo;

import com.epam.ta.reportportal.core.item.TestItemService;
import com.epam.ta.reportportal.entity.item.TestItem;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TestItemAccessValidator {

  private final TestItemService testItemService;

  public void checkItemsBelongsToProject(Long projectId, List<TestItem> items) {
    items.forEach(ti -> {
      var itemProjectId = testItemService.getEffectiveLaunch(ti)
          .getProjectId();

      expect(itemProjectId, equalTo(projectId)).verify(TEST_ITEM_NOT_FOUND, ti.getItemId());
    });
  }


}
