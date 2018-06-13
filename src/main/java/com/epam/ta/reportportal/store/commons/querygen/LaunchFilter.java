package com.epam.ta.reportportal.store.commons.querygen;

import com.epam.ta.reportportal.store.database.entity.enums.LaunchModeEnum;

/**
 * @author Yauheni_Martynau
 */
public class LaunchFilter extends Filter {

	private LaunchFilter(Filter filter, LaunchModeEnum modeEnum) {
		super(filter.getTarget(), filter.getFilterConditions());
		getFilterConditions().add(new FilterCondition(Condition.EQUALS, false, modeEnum.name(), "mode"));

	}

	public static Filter of(Filter filter, LaunchModeEnum modeEnum) {
		return new LaunchFilter(filter, modeEnum);
	}
}
