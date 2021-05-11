package com.epam.ta.reportportal.core.item.impl.rerun;

import com.epam.ta.reportportal.commons.querygen.Queryable;

import java.util.Optional;

public interface RerunSearcher {
	Optional<Long> findItem(Queryable filter);
}
