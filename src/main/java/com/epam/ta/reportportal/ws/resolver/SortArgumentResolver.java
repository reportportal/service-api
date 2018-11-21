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

import com.epam.ta.reportportal.commons.Preconditions;
import com.epam.ta.reportportal.commons.querygen.CriteriaHolder;
import com.epam.ta.reportportal.commons.querygen.FilterTarget;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortHandlerMethodArgumentResolver;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.Optional;
import java.util.stream.StreamSupport;

import static com.epam.ta.reportportal.commons.querygen.QueryBuilder.STATISTICS_KEY;
import static java.util.stream.Collectors.toList;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
public class SortArgumentResolver extends SortHandlerMethodArgumentResolver {

	@Override
	public Sort resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest,
			WebDataBinderFactory binderFactory) {

		/*
		 * Resolve sort argument in default way
		 */
		Sort defaultSort = super.resolveArgument(parameter, mavContainer, webRequest, binderFactory);

		/*
		 * Try to find parameter to be sorted in internal-external mapping
		 */
		if (null != parameter.getParameterAnnotation(SortFor.class) && null != defaultSort) {

			Class<?> domainModelType = parameter.getParameterAnnotation(SortFor.class).value();
			FilterTarget filterTarget = FilterTarget.findByClass(domainModelType);

			/*
			 * Build Sort with search criteria from internal domain model
			 */
			return Sort.by(StreamSupport.stream(defaultSort.spliterator(), false).map(order -> {
				if (!order.getProperty().startsWith(STATISTICS_KEY)) {
					Optional<CriteriaHolder> criteriaHolder = filterTarget.getCriteriaByFilter(order.getProperty());

					BusinessRule.expect(criteriaHolder, Preconditions.IS_PRESENT)
							.verify(ErrorType.INCORRECT_SORTING_PARAMETERS, order.getProperty());
					return new Sort.Order(order.getDirection(), criteriaHolder.get().getQueryCriteria());
				} else {
					return new Sort.Order(order.getDirection(), order.getProperty());
				}
			}).collect(toList()));
		} else {
			/*
			 * Return default sort in case there are no SortFor annotation
			 */
			return defaultSort;
		}

	}

}
