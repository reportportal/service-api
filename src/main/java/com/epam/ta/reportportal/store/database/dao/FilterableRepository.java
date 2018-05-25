package com.epam.ta.reportportal.store.database.dao;

import com.epam.ta.reportportal.store.commons.querygen.Filter;

import java.util.List;

public interface FilterableRepository <T> {

	List<T> findByFilter(Filter filter);
}
