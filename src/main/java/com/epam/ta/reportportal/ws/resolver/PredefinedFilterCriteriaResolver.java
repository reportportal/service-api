/*
 * Copyright 2016 EPAM Systems
 * 
 * 
 * This file is part of EPAM Report Portal.
 * https://github.com/reportportal/service-api
 * 
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.epam.ta.reportportal.ws.resolver;

import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.core.filter.PredefinedFilters;
import com.epam.ta.reportportal.database.search.CompositeFilter;
import com.epam.ta.reportportal.database.search.Queryable;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.springframework.core.MethodParameter;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Resolves filter parameters in GET requests. All Parameters should start with
 * <b>'filter.'</b> prefix. For example, if you would like to filter some
 * parameter with name 'age' you have to put in request the following:
 * '?filter.age=20'. Resolves parameter only in case argument marked with
 * annotation {@link FilterFor}. <br>
 * By FilterFor value resolves criterias/parameters to the given domain class
 * and resolves them if possible. If there are no criteria/parameter defined for
 * specified class than will throw exception
 *
 * @author Andrei Varabyeu
 */
public class PredefinedFilterCriteriaResolver implements HandlerMethodArgumentResolver {

	/**
	 * Default prefix for filter conditions. Since Request contains a lot of
	 * parameters (some of them may not be related to filtering), we have to
	 * introduce this
	 */
	public static final String FILTER_PARAMETER_NAME = "predefined_filter";

	/**
	 * Returns TRUE only for {@link List} marked with {@link FilterFor}
	 * annotations
	 */
	@Override
	public boolean supportsParameter(MethodParameter methodParameter) {
		return Queryable.class.isAssignableFrom(methodParameter.getParameterType()) && null != methodParameter.getParameterAnnotation(
				FilterFor.class);
	}

	@Override
	public Queryable resolveArgument(MethodParameter methodParameter, ModelAndViewContainer paramModelAndViewContainer,
			NativeWebRequest webRequest, WebDataBinderFactory paramWebDataBinderFactory) {
		Class<?> domainModelType = methodParameter.getParameterAnnotation(FilterFor.class).value();

		List<Queryable> filterConditions = webRequest.getParameterMap()
				.entrySet()
				.stream()
				.filter(parameter -> FILTER_PARAMETER_NAME.equals(parameter.getKey()))
				.map(parameter -> {
					BusinessRule.expect(parameter.getValue(), v -> null != v && v.length == 1)
							.verify(ErrorType.INCORRECT_REQUEST, "Incorrect filter value");

					String filterName = parameter.getValue()[0];

					BusinessRule.expect(PredefinedFilters.hasFilter(filterName), Predicate.isEqual(true))
							.verify(ErrorType.INCORRECT_REQUEST, "Unknown filter '" + filterName + "'");

					final Queryable queryable = PredefinedFilters.buildFilter(filterName, parameter.getValue());
					BusinessRule.expect(queryable.getTarget(), Predicate.isEqual(domainModelType))
							.verify(ErrorType.INCORRECT_REQUEST, "Incorrect filter");

					return queryable;

				})
				.collect(Collectors.toList());
		return filterConditions.isEmpty() ? nop(domainModelType) : new CompositeFilter(filterConditions);
	}

	private Queryable nop(Class<?> type) {
		return new Queryable() {
			@Override
			public List<Criteria> toCriteria() {
				return Collections.emptyList();
			}

			@Override
			public Class<?> getTarget() {
				return type;
			}
		};
	}
}
