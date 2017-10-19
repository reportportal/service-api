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

import com.epam.ta.reportportal.commons.Preconditions;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.database.search.CriteriaHolder;
import com.epam.ta.reportportal.database.search.CriteriaMap;
import com.epam.ta.reportportal.database.search.CriteriaMapFactory;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.web.SortHandlerMethodArgumentResolver;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.Optional;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;

/**
 * Argument resolver for sort argument
 *
 * @author Andrei Varabyeu
 */
public class SortArgumentResolver extends SortHandlerMethodArgumentResolver {

	@Autowired
	private CriteriaMapFactory criteriaMapFactory;

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
			CriteriaMap<?> map = criteriaMapFactory.getCriteriaMap(domainModelType);

            /*
			 * Build Sort with search criteria from internal domain model
			 */
			return new Sort(StreamSupport.stream(defaultSort.spliterator(), false).map(order -> {
				Optional<CriteriaHolder> criteriaHolder = map.getCriteriaHolderUnchecked(order.getProperty());

				BusinessRule.expect(criteriaHolder, Preconditions.IS_PRESENT)
						.verify(ErrorType.INCORRECT_SORTING_PARAMETERS, order.getProperty());
				//noinspection ConstantConditions
				return new Order(order.getDirection(), criteriaHolder.get().getQueryCriteria());
			}).collect(toList()));
		} else {
			/*
			 * Return default sort in case there are no SortFor annotation
			 */
			return defaultSort;
		}

	}

}
