package com.epam.ta.reportportal.core.item.impl.filter.updater;

import com.epam.ta.reportportal.commons.querygen.Condition;
import com.epam.ta.reportportal.commons.querygen.ConvertibleCondition;
import com.epam.ta.reportportal.commons.querygen.FilterCondition;
import com.epam.ta.reportportal.commons.querygen.Queryable;
import com.epam.ta.reportportal.dao.IssueTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.epam.ta.reportportal.commons.querygen.constant.TestItemCriteriaConstant.CRITERIA_ISSUE_TYPE;
import static com.epam.ta.reportportal.commons.querygen.constant.TestItemCriteriaConstant.CRITERIA_ISSUE_TYPE_ID;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class IssueTypeConditionReplacer implements FilterUpdater {

	private final IssueTypeRepository issueTypeRepository;

	@Autowired
	public IssueTypeConditionReplacer(IssueTypeRepository issueTypeRepository) {
		this.issueTypeRepository = issueTypeRepository;
	}

	@Override
	public void update(Queryable filter) {
		// Added to fix performance issue.
		List<String> issueTypeLocators = filter.getFilterConditions()
				.stream()
				.map(ConvertibleCondition::getAllConditions)
				.flatMap(List::stream)
				.filter(c -> CRITERIA_ISSUE_TYPE.equals(c.getSearchCriteria()) && !c.isNegative() && Condition.IN.equals(c.getCondition()))
				.map(FilterCondition::getValue)
				.flatMap(c -> Stream.of(c.split(",")))
				.collect(Collectors.toList());

		String issueTypeIdsString = issueTypeRepository.getIssueTypeIdsByLocators(issueTypeLocators)
				.stream()
				.map(String::valueOf)
				.collect(Collectors.joining(","));

		FilterCondition oldIssueTypeCondition = new FilterCondition(Condition.IN, false, null, CRITERIA_ISSUE_TYPE);
		FilterCondition issueTypeIdCondition = new FilterCondition(Condition.IN, false, issueTypeIdsString, CRITERIA_ISSUE_TYPE_ID);
		filter.replaceSearchCriteria(oldIssueTypeCondition, issueTypeIdCondition);
	}
}
