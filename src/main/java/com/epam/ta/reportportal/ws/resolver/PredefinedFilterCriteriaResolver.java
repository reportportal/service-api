/*
 * Copyright 2018 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.ws.resolver;

import com.epam.ta.reportportal.commons.querygen.CompositeFilter;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.Queryable;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.core.filter.PredefinedFilters;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.MethodParameter;
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
	public static final String FILTER_PARAMETER_NAME = "predefinedFilter";

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
				.filter(parameter -> StringUtils.contains(parameter.getKey(), FILTER_PARAMETER_NAME))
				.map(parameter -> {
					BusinessRule.expect(parameter.getValue(), v -> null != v && v.length == 1)
							.verify(ErrorType.INCORRECT_REQUEST, "Incorrect filter value");

					String filterName = parameter.getKey().split("\\.")[1];
					String[] filterParameters = parameter.getValue()[0].split(",");

					BusinessRule.expect(PredefinedFilters.hasFilter(filterName), Predicate.isEqual(true))
							.verify(ErrorType.INCORRECT_REQUEST, "Unknown filter '" + filterName + "'");

					final Queryable queryable = PredefinedFilters.buildFilter(filterName, filterParameters);
					BusinessRule.expect(queryable.getTarget().getClazz(), Predicate.isEqual(domainModelType))
							.verify(ErrorType.INCORRECT_REQUEST, "Incorrect filter target class type");

					return queryable;

				})
				.collect(Collectors.toList());
		return filterConditions.isEmpty() ? nop(domainModelType) : new CompositeFilter(filterConditions);
	}

	private Queryable nop(Class<?> type) {
		return new Filter(type, Collections.emptySet());
	}
}
