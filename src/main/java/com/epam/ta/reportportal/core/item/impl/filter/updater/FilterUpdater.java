package com.epam.ta.reportportal.core.item.impl.filter.updater;

import com.epam.ta.reportportal.commons.querygen.Queryable;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public interface FilterUpdater {

	void update(Queryable filter);
}
