package com.epam.ta.reportportal.store.commons.querygen;

public class ProjectFilter extends Filter {

	private ProjectFilter(Filter filter, String project) {
		super(filter.getTarget(), filter.getFilterConditions());
		getFilterConditions().add(new FilterCondition(Condition.EQUALS, false, project, "project"));

	}

	public static Filter of(Filter filter, String project) {
		return new ProjectFilter(filter, project);
	}

}
