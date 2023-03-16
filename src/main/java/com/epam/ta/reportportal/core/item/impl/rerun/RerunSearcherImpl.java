package com.epam.ta.reportportal.core.item.impl.rerun;

import static com.epam.ta.reportportal.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_START_TIME;

import com.epam.ta.reportportal.commons.querygen.Queryable;
import com.epam.ta.reportportal.dao.TestItemRepository;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class RerunSearcherImpl implements RerunSearcher {

  private final TestItemRepository testItemRepository;

  public RerunSearcherImpl(TestItemRepository testItemRepository) {
    this.testItemRepository = testItemRepository;
  }

  @Override
  public Optional<Long> findItem(Queryable filter) {
    return testItemRepository.findIdByFilter(filter, Sort.by(Sort.Order.desc(CRITERIA_START_TIME)));
  }
}
