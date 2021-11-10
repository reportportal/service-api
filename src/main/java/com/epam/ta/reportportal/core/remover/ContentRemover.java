package com.epam.ta.reportportal.core.remover;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public interface ContentRemover<T> {

	void remove(T entity);
}
